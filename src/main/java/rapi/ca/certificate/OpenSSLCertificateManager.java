package rapi.ca.certificate;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rapi.ca.certificate.model.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author gdr
 * 
 */
@Service("opensslManager")
public class OpenSSLCertificateManager implements CertificateManager {
	private Path opensslConfTemplate;
	private Path serialPath;
	private Path _3rdPartySigner;
	private Path _3rdPartyKey;
	private Path _3rdPartyCertChain;
	@Value("${certficateAuthoritySupport}")
	private boolean certficateAuthoritySupport;

	@Value("${spring.profiles.active}")
	private String envName;

	@Value("${spring.config.location}")
	private String configDir;

	final static Logger logger = LoggerFactory.getLogger(OpenSSLCertificateManager.class);

    public OpenSSLCertificateManager(){}

	private void deletePath(Path aPath) {
		try {
			Files.delete(aPath);
		} catch (IOException e) {
// throw new RuntimeException(e);
		}
	}

	public OpenSSLCertificateManager(Path opensslConfTemplate, Path serialPath) {
		super();
		this.opensslConfTemplate = opensslConfTemplate;
		this.serialPath = serialPath;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.vertextplatform.certmgmt.CertificateManager#generateKey(com.cpn.vsp.vertexcore.model.certificate.Certificate)
	 */
	@Override
	public Certificate generateKey(final Certificate aCert) {
		Path keyPath = randomPath();
		OpenSSLProcess openssl = new OpenSSLProcess("openssl","genrsa", "-out", keyPath.toString(), "2048");
		try {
			openssl.start();
			aCert.setPrivateKey(new String(Files.readAllBytes(keyPath)));
			return aCert;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(keyPath);
		}
	}

	private Path randomPath() {
		try {
			return Files.createTempFile(Paths.get(FileUtils.getTempDirectoryPath()), "temp", "openssl", new FileAttribute<?>[0]);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String buildSubject(final CertificateSubject certSubject) {
		StringBuilder subject = new StringBuilder("");
		for (Map.Entry<String, String> entry : certSubject.getOptions().entrySet()) {
			subject.append('/').append(entry.getKey()).append('=').append(entry.getValue());
		}
		return subject.toString();
	}

	private Path generateConfig(final CertificateSubject certSubject) {
		try {
			String confTemplate = new String(Files.readAllBytes(opensslConfTemplate));
			confTemplate = confTemplate.replaceAll("%%SUBJECT_ALT_NAME%%",
					StringUtils.isNotBlank(certSubject.getSubjectAltName()) ? certSubject.getSubjectAltName() : "");
			confTemplate = confTemplate.replaceAll("%%NSCOMMENT%%",
					StringUtils.isNotBlank(certSubject.getNsComment()) ? certSubject.getNsComment() : "");
			confTemplate = confTemplate.replaceAll("%%BASIC_CONSTRAINTS%%", certSubject.getCA() ? "CA:TRUE" : "CA:FALSE");
			confTemplate = confTemplate.replaceAll("CA:TRUE",
					certSubject.getPathlen() > -1 ? "CA:TRUE,pathlen:" + Integer.toString(certSubject.getPathlen()) : "CA:TRUE");
			Path tempConfFile = randomPath();
			Files.write(tempConfFile, confTemplate.getBytes());
			return tempConfFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Path generateExtensions(final Certificate aCert) {
		Path confPath = randomPath();
		String confTemplate = "";
		CertificateSubject subject = aCert.getSubject();

		String CA = "FALSE";
		if (subject.getCA()) {
			CA = "TRUE";
		}
		confTemplate = "basicConstraints=critical,CA:" + CA;
		if (subject.getCA() && subject.getPathlen() > -1) {
			confTemplate += ",pathlen:" + subject.getPathlen();
		}
		confTemplate += "\n";
		confTemplate += "subjectKeyIdentifier=hash\n" +
				"authorityKeyIdentifier=keyid,issuer\n" +
				"keyUsage = nonRepudiation, digitalSignature, keyEncipherment, dataEncipherment, keyAgreement, keyCertSign, cRLSign\n" +
				"extendedKeyUsage=critical,serverAuth,clientAuth,codeSigning,emailProtection,timeStamping\n";
		for (Map.Entry<String, String> entry : subject.getOptions().entrySet()) {
			switch (entry.getKey()) {
			case "nsComment":
			case "subjectAltName":
				confTemplate += entry.getKey() + "=" + entry.getValue() + "\n";
				break;
			}
		}
		try {
			Files.write(confPath, confTemplate.getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return confPath;
	}

	private Certificate findExtensions(final String aCert) {
		Certificate certificate = new Certificate();
		String lines[] = verifyCertificateSigningRequest(aCert).split("\n");
		CertificateSubject subject = new CertificateSubject();
		certificate.setSubject(subject);
		int index = 0;
		for (String line : lines) {
			if (line.indexOf("X509v3 Subject Alternative Name:") > -1) {
				subject.setSubjectAltName(lines[index + 1].trim());
			}
			if (line.indexOf("Netscape Comment:") > -1) {
				subject.setNsComment(lines[index + 1].trim());
			}
			if (line.indexOf("X509v3 Basic Constraints:") > -1) {
				subject.setCA(lines[index + 1].trim().toUpperCase().indexOf("CA:TRUE") > -1);
				Matcher matcher = Pattern.compile(".*pathlen:(\\d+)").matcher(line);
				if (matcher.matches()) {
					subject.setPathlen(Integer.parseInt(matcher.group(1)));
				}
			}
			index++;
		}

		return certificate;
	}

	private String verifyCertificateSigningRequest(final String aCert) {
		Path certPath = randomPath();
		Path outputPath = randomPath();
		try {
			Files.write(certPath, aCert.getBytes());
			OpenSSLProcess openssl = new OpenSSLProcess("openssl","req", "-verify", "-noout", "-in", certPath.toString(), "-text", "-out",
					outputPath.toString());
			openssl.start();
			return new String(Files.readAllBytes(outputPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(certPath);
			deletePath(outputPath);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.vertextplatform.certmgmt.CertificateManager#generateSelfSigned(com.cpn.vsp.vertexcore.model.certificate.Certificate)
	 */
	@Override
	public Certificate generateSelfSigned(final Certificate aCert) {
		Path keyPath = randomPath();
		Path outPath = randomPath();
		Path configPath = generateConfig(aCert.getSubject());
		OpenSSLProcess openssl = new OpenSSLProcess("req", "-batch", "-x509", "-nodes",
				"-days", Integer.toString(aCert.getDaysValidFor()), "-subj", "\"" + buildSubject(aCert.getSubject()) + "\"", "-sha1",
				"-newkey", "rsa:2048",
				"-keyout", keyPath.toString(), "-out", outPath.toString(), "-config", configPath.toString());
		try {
			openssl.start();
			aCert.setPrivateKey(new String(Files.readAllBytes(keyPath)));
			aCert.setCert(new String(Files.readAllBytes(outPath)));
			aCert.setSelfSigned(true);
			return aCert;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(configPath);
			deletePath(keyPath);
			deletePath(outPath);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.vertextplatform.certmgmt.CertificateManager#generateCertificateSigningRequest(com.cpn.vsp.vertexcore.model.certificate.
	 * Certificate
	 * )
	 */
	@Override
	public CertificateSigningRequest generateCertificateSigningRequest(final Certificate aCert) {
		Path keyPath = randomPath();
		Path outPath = randomPath();
		Path configPath = generateConfig(aCert.getSubject());
		OpenSSLProcess openssl = new OpenSSLProcess("openssl","req", "-batch", "-new", "-nodes", "-subj", "\"" + buildSubject(aCert.getSubject())
				+ "\"", "-key ", keyPath.toString(), "-out ", outPath.toString(), "-config", configPath.toString());
		try {
			Files.write(keyPath, aCert.getPrivateKey().getBytes());
			openssl.start();
			return new CertificateSigningRequest(new String(Files.readAllBytes(outPath)), null, aCert);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(keyPath);
			deletePath(outPath);
			deletePath(configPath);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.vertextplatform.certmgmt.CertificateManager#signCertificateSigningRequest(com.cpn.vsp.vertexcore.model.certificate.
	 * CertificateSigningRequest)
	 */
	@Override
	public Certificate signCertificateSigningRequest(final CertificateSigningRequest aCSR) {
		Path csrPath = randomPath();
		Path certPath = randomPath();
		Path keyPath = randomPath();
		Path outputPath = randomPath();
		Path extensionPath = generateExtensions(findExtensions(aCSR.getCsr()));
		OpenSSLProcess openssl = new OpenSSLProcess("openssl","x509", "-req", "-days ", "" + aCSR.getSignee().getDaysValidFor(),
				"-CA", certPath.toString(), "-CAkey", keyPath.toString(), "-CAserial", serialPath.toString(), "-in", csrPath.toString(),
				"-out", outputPath.toString(), "-extfile", extensionPath.toString());
		try {
			Files.write(csrPath, aCSR.getCsr().getBytes());
			Files.write(certPath, aCSR.getSigner().getCert().getBytes());
			Files.write(keyPath, aCSR.getSigner().getPrivateKey().getBytes());
			openssl.start();
			aCSR.getSignee().setCert(new String(Files.readAllBytes(outputPath)));
			aCSR.getSignee().setSigner(aCSR.getSigner());
			return aCSR.getSignee();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(outputPath);
			deletePath(extensionPath);
			deletePath(keyPath);
			deletePath(certPath);
			deletePath(csrPath);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.cpn.vsp.vertextplatform.certmgmt.CertificateManager#pkcs12(com.cpn.vsp.vertexcore.model.certificate.PKCS12CertificateRequest)
	 */
	@Override
	public PKCS12Certificate pkcs12(final PKCS12CertificateRequest aPkcs12Request) {
		Path keyPath = randomPath();
		Path certPath = randomPath();
		Path pkcsPath = randomPath();
		Path caPath = randomPath();
		Certificate certificate = aPkcs12Request.getCertificate();
		boolean havingPrivate = StringUtils.isNotBlank(certificate.getPrivateKey());
		OpenSSLProcess openssl = null;
		try {

			if (havingPrivate) {
				openssl = new OpenSSLProcess("openssl","pkcs12", "-export", "-inkey", keyPath.toString(),
						"-in", certPath.toString(), "-passout", "pass:password",
						"|", "openssl", "enc", "-a", "-out", pkcsPath.toString());
				Files.write(keyPath, certificate.getPrivateKey().getBytes());
			} else {
				openssl = new OpenSSLProcess("openssl","pkcs12", "-export", "-nokeys",
						"-in", certPath.toString(), "-passout", "pass:password", "-out", pkcsPath.toString(),
						"|", "openssl", "enc", "-a", "-out", pkcsPath.toString());
			}
			Files.write(certPath, certificate.getCert().getBytes());
			Files.write(caPath, aPkcs12Request.getCaBundle().getBytes());
			openssl.start();
			PKCS12Certificate pkcs12Cert = new PKCS12Certificate(certificate);
			pkcs12Cert.setPkcs12(new String(Files.readAllBytes(pkcsPath)));
			return pkcs12Cert;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (havingPrivate) {
				deletePath(keyPath);
			}
			deletePath(certPath);
			deletePath(pkcsPath);
			deletePath(caPath);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cpn.vsp.vertextplatform.certmgmt.CertificateManager#sign(com.cpn.vsp.vertextplatform.certmgmt.Certainly.SigningRequest)
	 */
	@Override
	public String sign(final SigningRequest signingRequest) {
		Path keyPath = randomPath();
		Path messagePath = randomPath();
		Path caPath = randomPath();
		Path certPath = randomPath();
		Path sigPath = randomPath();

		OpenSSLProcess openssl = new OpenSSLProcess("openssl","smime", "-sign",
				"-inkey", keyPath.toString(), "-out", sigPath.toString(),
				"-signer", certPath.toString(), "-certfile", caPath.toString(),
				"-nodetach", "-outform", "der", "-in", messagePath.toString());

		try {
			Files.write(keyPath, signingRequest.getPrivateKey().getBytes());
			Files.write(messagePath, signingRequest.getMessage().getBytes());
			Files.write(caPath, signingRequest.getCa().getBytes());
			Files.write(certPath, signingRequest.getCert().getBytes());
			openssl.start();
			return new String(Files.readAllBytes(sigPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(keyPath);
			deletePath(messagePath);
			deletePath(caPath);
			deletePath(certPath);
			deletePath(sigPath);
		}
	}

	// openssl smime -sign -in company.mobileconfig -out signed.mobileconfig -signer server.crt -inkey SERVER.KEY -certfile gd_bundle.crt
	// -outform der -nodetach
	@Override
	public byte[] signSMIME(byte[] buffer) {
		Path pathToMime = randomPath();
		Path outFile = randomPath();
		try{
			this._3rdPartySigner = Paths.get(this.getClass().getResource("/openssl/3rdPartyCerts/" + envName.trim() + ".crt").toURI());
			this._3rdPartyKey = Paths.get(this.getClass().getResource("/openssl/3rdPartyCerts/" + envName.trim() + ".key").toURI());
			this._3rdPartyCertChain = Paths
					.get(this.getClass().getResource("/openssl/3rdPartyCerts/" + envName.trim() + ".bundle").toURI());
		}
		catch(Exception e){
			throw new RuntimeException("Couldn't get 3rd Party Signer details");
		}
	
		OpenSSLProcess openSSL = new OpenSSLProcess("openssl", "smime", "-sign", "-in", pathToMime.toString(), "-out", outFile.toString(),
				"-signer", _3rdPartySigner.toString(), "-inkey", _3rdPartyKey.toString(),
				"-certfile", _3rdPartyCertChain.toString(), "-outform", "der", "-nodetach");
		try {
			Files.write(pathToMime, buffer);
			openSSL.start();
			return Files.readAllBytes(outFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			deletePath(pathToMime);
			deletePath(outFile);
		}
	}

	@PostConstruct
	void initSerialFile() {
		try {
			if (!Files.exists(Paths.get(FileUtils.getTempDirectoryPath().toString(), "serial.srl"), LinkOption.NOFOLLOW_LINKS)) {
				serialPath = Files.createFile(Paths.get(FileUtils.getTempDirectoryPath().toString(), "serial.srl"));
				Files.write(serialPath, "00".getBytes());
			} else {
				serialPath = Paths.get(FileUtils.getTempDirectoryPath().toString(), "serial.srl");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
			opensslConfTemplate = Paths.get(configDir+"/openssl/template/" + envName.trim() + ".openssl.cnf.template");
			if (this.certficateAuthoritySupport) {
				this._3rdPartySigner = Paths.get(configDir+"/openssl/3rdPartyCerts/" + envName.trim() + ".crt");
				this._3rdPartyKey = Paths.get(configDir+"/openssl/3rdPartyCerts/" + envName.trim() + ".key");
				this._3rdPartyCertChain = Paths
						.get(configDir+"/openssl/3rdPartyCerts/" + envName.trim() + ".bundle");
			}
	}



    class OpenSSLProcess {

        private NuProcessBuilder pb;

        public OpenSSLProcess(String... args) {
            pb = new NuProcessBuilder(args);
            pb.setProcessListener(new ProcessHandler());
        }

        public void start() {
            NuProcess np = pb.start();
            try {
                np.waitFor(0, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }

        class ProcessHandler extends NuAbstractProcessHandler {
            private NuProcess nuProcess;

            @Override
            public void onStart(NuProcess nuProcess) {
                this.nuProcess = nuProcess;
            }

            @Override
            public void onStdout(ByteBuffer buffer) {
                nuProcess.closeStdin();
            }

            @Override
            public void onStderr(ByteBuffer buffer) {
                nuProcess.closeStdin();
            }
        }
    }

}
