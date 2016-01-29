package rapi.ca.certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import rapi.ca.certificate.model.Certificate;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rapi.ca.certificate.model.*;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Enumeration;

@Service("bouncyManager")
public class BouncyCastleCertificateManager implements CertificateManager {
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;
    private static String SIGNATURE_ALGORITHM = "SHA1WithRSAEncryption";
    private static String PKCS12="PKCS12";
    private static int LINE_BREAK=64;//openssl compatibility
    private KeyPairGenerator keyGen;
    private AlgorithmIdentifier sigAlgId;
    private AlgorithmIdentifier digAlgId;
    private KeyUsage keyUsage;
    private ExtendedKeyUsage extendedKeyUsage;
    private JcaX509ExtensionUtils jcaX509ExtensionUtils;
    private JcaContentSignerBuilder jcaContentSignerBuilder;
    private JcaX509CertificateConverter jcaX509CertificateConverter;
    private JcaPEMKeyConverter jcaPEMKeyConverter;
    private Base64 b64 = new Base64(LINE_BREAK);
    private char[] defaultPassword={'p','a','s','s','w','o','r','d'};
    private SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        initKeyPairGenerator();
        sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(SIGNATURE_ALGORITHM);
        digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        keyUsage = new KeyUsage(KeyUsage.cRLSign | KeyUsage.digitalSignature| KeyUsage.dataEncipherment|
                KeyUsage.keyAgreement | KeyUsage.nonRepudiation | KeyUsage.keyCertSign| KeyUsage.keyEncipherment);
        extendedKeyUsage = new ExtendedKeyUsage(new KeyPurposeId[]{KeyPurposeId.id_kp_emailProtection , KeyPurposeId.id_kp_codeSigning,
                KeyPurposeId.id_kp_timeStamping, KeyPurposeId.id_kp_clientAuth,KeyPurposeId.id_kp_serverAuth});
        jcaX509ExtensionUtils = new JcaX509ExtensionUtils();
        jcaContentSignerBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BC);
        jcaX509CertificateConverter = new JcaX509CertificateConverter().setProvider(BC);
        jcaPEMKeyConverter = new JcaPEMKeyConverter().setProvider(BC);
    }

    @Override
    public Certificate generateKey(Certificate aCert) {
        KeyPair keypair = keyGen.generateKeyPair();
        aCert.setPrivateKey(toPEM(keypair.getPrivate()));
        return aCert;
    }

    private void initKeyPairGenerator() {
        try {
            keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048, secureRandom);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> String toPEM(final T aKey) {
        try (StringWriter key = new StringWriter(); PEMWriter writer = new PEMWriter(key)) {
            writer.writeObject(aKey);
            writer.close();
            return key.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private X500Name buildSubject(final CertificateSubject certSubject) {
        X500NameBuilder builder = new X500NameBuilder().
                addRDN(BCStyle.CN,certSubject.getEmailAddress()).
                addRDN(BCStyle.ST, certSubject.getST()).
                addRDN(BCStyle.L, certSubject.getL()).
                addRDN(BCStyle.C, certSubject.getC()).
                addRDN(BCStyle.O, certSubject.getO()).
                addRDN(BCStyle.OU, certSubject.getOU()).
                addRDN(BCStyle.EmailAddress, certSubject.getEmailAddress());
        return builder.build();
    }

    private GeneralNames buildSubjectAlternative(CertificateSubject subject){
        GeneralNames subjectAltName;
        switch(subject.getSubjectAltName()){
            case "dns:alt":
                String[] altNames = subject.getSubjectAltName().split(";");
                GeneralName[] genNames = new GeneralName[altNames.length];
                int i=0;
                for (String altName : altNames){
                    genNames[i] = new GeneralName(GeneralName.dNSName,altName);
                    i++;
                }
                subjectAltName = new GeneralNames(genNames);

                break;
            case "email:copy":
            default:
                subjectAltName = new GeneralNames(new GeneralName(GeneralName.rfc822Name,subject.getEmailAddress()));
        }
        return subjectAltName;
    }

    @Override
    public Certificate generateSelfSigned(Certificate aCert) {
        //Set to 5 years back.
        Date dates[] = validityDates(-5 * 365, aCert.getDaysValidFor());
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        KeyPair keyPair = keyGen.generateKeyPair();
        X500Name issuer = buildSubject(aCert.getSubject());
        X500Name subject = issuer;
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, serial, dates[0], dates[1], subject, keyPair.getPublic());
        try {
            ContentSigner  contentSigner = jcaContentSignerBuilder.build(keyPair.getPrivate());
            certBuilder.addExtension(Extension.subjectAlternativeName, false, buildSubjectAlternative(aCert.getSubject()));
            GeneralNames issuerName = new GeneralNames(new GeneralName(GeneralName.directoryName, issuer));
            AuthorityKeyIdentifier authorityKeyIdentifier = new AuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()), issuerName, serial);
            certBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);
            BasicConstraints bc;
            if (aCert.getSubject().getPathlen()>-1 && aCert.getSubject().getCA()) {
                bc = new BasicConstraints(aCert.getSubject().getPathlen());
            }else{
                bc = new BasicConstraints(aCert.getSubject().getCA());
            }
            certBuilder.addExtension(Extension.basicConstraints, false, bc);
            certBuilder.addExtension(Extension.subjectKeyIdentifier, false, jcaX509ExtensionUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
            X509Certificate cert = jcaX509CertificateConverter.getCertificate(certBuilder.build(contentSigner));
            aCert.setPrivateKey(toPEM(keyPair.getPrivate()));
            aCert.setCert(toPEM(cert));
            return aCert;
        } catch (CertificateException | CertIOException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CertificateSigningRequest generateCertificateSigningRequest(Certificate aCert) {
        X500Name subject = buildSubject(aCert.getSubject());
        KeyPair keyPair = keyGen.generateKeyPair();
        ExtensionsGenerator generator = new ExtensionsGenerator();
        try {
            PKCS10CertificationRequestBuilder pkcs10Builder = new PKCS10CertificationRequestBuilder(subject, SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
            //IS CA (Certificate Authority)
            BasicConstraints bc;
            if (aCert.getSubject().getPathlen()>-1 && aCert.getSubject().getCA()) {
                bc = new BasicConstraints(aCert.getSubject().getPathlen());
            }else{
                bc = new BasicConstraints(false);
            }
            generator.addExtension(Extension.basicConstraints, aCert.getSubject().getCA(), bc);
            //Subject Alternative name (mostly email) otherwise DNS
            generator.addExtension(Extension.subjectAlternativeName,false,buildSubjectAlternative(aCert.getSubject()));
            generator.addExtension(Extension.keyUsage,false,keyUsage);
            generator.addExtension(Extension.extendedKeyUsage,false,extendedKeyUsage);
            generator.addExtension(Extension.subjectKeyIdentifier, false, jcaX509ExtensionUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
            pkcs10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,generator.generate());
            ContentSigner signer = jcaContentSignerBuilder.build(keyPair.getPrivate());
            aCert.setPrivateKey(toPEM(keyPair.getPrivate()));
            return new CertificateSigningRequest(toPEM(pkcs10Builder.build(signer)), null, aCert);
        } catch (OperatorCreationException | IOException   e ) {
            throw new RuntimeException(e);
        }
    }

    private <T> T readObject(String str, Class<T> clazz) throws IOException {
        try(PEMParser parser = new PEMParser(new StringReader(str))) {
            return clazz.cast(parser.readObject());
        }finally{
        }
    }

    private Date[] validityDates(int from, int to) {
        LocalDateTime begin, end;
        if (from < 0) {
            begin =  LocalDateTime.now().minusDays(from * -1);
        } else {
            begin =  LocalDateTime.now().plusDays(from);
        }

        end =  LocalDateTime.now().plusDays(to);
        return new Date[]{Date.from(begin.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(end.atZone(ZoneId.systemDefault()).toInstant())};
    }

    @Override
    public Certificate signCertificateSigningRequest(CertificateSigningRequest aCSR) {
        //0 is today, Any thing less than 0 takes to past. For example -500 is 500 days before.
        Date[] dates = validityDates(-1, aCSR.getSignee().getDaysValidFor());
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        try {
            //Load the CSR
            PKCS10CertificationRequest csrRequest = readObject(aCSR.getCsr(), PKCS10CertificationRequest.class);
            //Load the Signer, since we are dealing with PEMs
            X509CertificateHolder signer = readObject(aCSR.getSigner().getCert(), X509CertificateHolder.class);
            //Load signer private key
            PEMKeyPair signerKeyPair = readObject(aCSR.getSigner().getPrivateKey(), PEMKeyPair.class);
            AsymmetricKeyParameter signerPrivateKey = PrivateKeyFactory.createKey(signerKeyPair.getPrivateKeyInfo());
            //Signing is nothing but generating a certificate with signer certificate subject, private key
            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(signer.getSubject(), serial,
                    dates[0], dates[1], csrRequest.getSubject(), csrRequest.getSubjectPublicKeyInfo());
            //Retrieve the extensions from CSR.
            for (Attribute attribute : csrRequest.getAttributes()){
                if (attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    Extensions extensions = Extensions.getInstance(attribute.getAttrValues().getObjectAt(0));
                    Enumeration e = extensions.oids();
                    while (e.hasMoreElements()) {
                        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
                        Extension ext = extensions.getExtension(oid);
                        certBuilder.addExtension(ext.getExtnId(), ext.isCritical(), ext.getParsedValue());
                    }
                }
            }
            certBuilder.addExtension(Extension.authorityKeyIdentifier, false, jcaX509ExtensionUtils.createAuthorityKeyIdentifier(signer));
            ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(signerPrivateKey);
            X509Certificate cert = jcaX509CertificateConverter.getCertificate(certBuilder.build(contentSigner));
            aCSR.getSignee().setCert(toPEM(cert));
            aCSR.getSignee().setSigner(aCSR.getSigner());
            return aCSR.getSignee();
        } catch (IOException | OperatorCreationException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCommonName(final X500Name subject){
        for (RDN rdn : subject.getRDNs()){
            if(rdn.getFirst().getType().equals(BCStyle.CN)){
                return rdn.getFirst().getValue().toString();
            }
        }
    return "Common Not Found";
    }

    @Override
    public PKCS12Certificate pkcs12(PKCS12CertificateRequest aPkcs12Request) {
        Certificate acert = aPkcs12Request.getCertificate();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            X509CertificateHolder pkCert= readObject(acert.getCert(), X509CertificateHolder.class);
            PEMKeyPair keyPair = readObject(acert.getPrivateKey(), PEMKeyPair.class);
            KeyStore keyStore = KeyStore.getInstance(PKCS12, BC);
            keyStore.load(null,null);

            char[] password = StringUtils.isNotEmpty(aPkcs12Request.getPassword())?aPkcs12Request.getPassword().toCharArray():defaultPassword;
            keyStore.setKeyEntry(getCommonName(pkCert.getSubject()),jcaPEMKeyConverter.getPrivateKey(keyPair.getPrivateKeyInfo()),password,
                    new java.security.cert.Certificate[]{jcaX509CertificateConverter.getCertificate(pkCert)});
            PKCS12Certificate pkcs12Certificate = new PKCS12Certificate();
            keyStore.store(bos,password);
            pkcs12Certificate.setPkcs12(new String(b64.encode(bos.toByteArray())));
            return pkcs12Certificate;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException|  NoSuchProviderException |IOException e) {
            throw new RuntimeException(e);
        }finally{

        }
    }

    @Override
    public String sign(SigningRequest signingRequest) {
        return openSSLManager.sign(signingRequest);
    }

    @Override
    public byte[] signSMIME(byte[] buffer) {
        return openSSLManager.signSMIME(buffer);
    }

    @Autowired @Qualifier("opensslManager")
    private CertificateManager openSSLManager;

}
