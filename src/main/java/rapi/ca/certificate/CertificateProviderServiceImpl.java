package rapi.ca.certificate;


import rapi.ca.certificate.model.*;
import rapi.ca.rest.RestCommand;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class CertificateProviderServiceImpl implements  CertificateProviderService{
    private Cache certCache;
    private final String REMOTE_SIGNERS = "remote-signers";
    private final String REMOTE_CERTS = "remote-certs";

    @PostConstruct
    public void init() {
        CacheManager cacheManager = CacheManager.create();
        certCache = new Cache(new CacheConfiguration("NexusCerts", 1000)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .eternal(false)
                .timeToLiveSeconds(60 * 10)
                .timeToIdleSeconds(60)
                .diskExpiryThreadIntervalSeconds(0)
                .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE)));
        cacheManager.addCache(certCache);
    }


    @Autowired
    CertificateProviderDAO certificateProviderDAO;
    private static final Logger log = LoggerFactory.getLogger(CertificateProviderServiceImpl.class);

    
    @Transactional
    public CertificateProvider add(CertificateProvider aCertificateProvider) {
        certificateProviderDAO.persist(aCertificateProvider);
        return aCertificateProvider;
    }

    
    @Transactional
    public CertificateProvider update(CertificateProvider aCertificateProvider, String aCPId) {
        aCertificateProvider.setId(aCPId);
        return certificateProviderDAO.merge(aCertificateProvider);
    }

    
    @Transactional
    public void delete(String aCPId) {
        CertificateProvider aCertificateProvider = certificateProviderDAO.find(aCPId);
        if (aCertificateProvider != null) {
            certificateProviderDAO.remove(aCertificateProvider);
        }
    }

    
    @Transactional
    public List<CertificateProvider> list() {
        return certificateProviderDAO.list();
    }

    
    @Transactional
    public CertificateProvider show(String aCPId) {
        return certificateProviderDAO.show(aCPId);
    }

    
    public List<Certificate> getRemoteCertificates(CertificateProvider certificateProvider) {
        Element remoteCerts = certCache.get(REMOTE_CERTS);
        if (null == remoteCerts) {
            log.debug("CertificateProvider host name: " + certificateProvider.getHostName() + " UserName: "
                    + certificateProvider.getUserName() + " Password: " + certificateProvider.getPassword());
            // final Certificate[] certificateList =
            // template.getForEntity(certificateProvider.getHostName()+"/certificate",
            // Certificate[].class).getBody();

            URL url = toUrl(certificateProvider.getHostName());

            RestCommand<String, Certificate[]> command = new RestCommand<>(certificateProvider.getUserName(),
                    certificateProvider.getPassword(), url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
            command.setResponseModel(Certificate[].class);
            command.setUrl(certificateProvider.getHostName() + "/certificate");
            final Certificate[] certificateList = command.get();

            //log.debug("CertificateProvider certificateList: " + certificateList);
            for (final Certificate c : certificateList) {
                c.setUpstream(true);
                c.setCertificateProvider(certificateProvider);
            }
            certCache.put(new Element(REMOTE_CERTS, certificateList));
            return Arrays.asList(certificateList);
        }
        return (List<Certificate>) remoteCerts.getObjectValue();
    }

    
    public List<Certificate> getRemoteSigners(CertificateProvider certificateProvider, final String anId) {
        String key = String.format("%s:%s:%s", REMOTE_SIGNERS, certificateProvider.getId(), anId);
        Element remoteSigners = certCache.get(key);
        if (null == remoteSigners) {
            log.debug("CertificateProvider host name: " + certificateProvider.getHostName() + " UserName: "
                    + certificateProvider.getUserName() + " Password: " + certificateProvider.getPassword());
            URL url = toUrl(certificateProvider.getHostName());
            RestCommand<String, Certificate[]> command = new RestCommand<>(certificateProvider.getUserName(),
                    certificateProvider.getPassword(), url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
            command.setResponseModel(Certificate[].class);
            command.setUrl(certificateProvider.getHostName() + "/certificate/" + anId + "/signers");
            final Certificate[] certificateList = command.get();

            List<Certificate> remoteSignersList = Arrays.asList(certificateList);
            certCache.put(new Element(key, remoteSignersList));
            return remoteSignersList;
        }
        return (List<Certificate>) remoteSigners.getObjectValue();
    }

    private URL toUrl(String anUrl) {
        URL url = null;
        try {
            url = new URL(anUrl);
        } catch (MalformedURLException e) {

        }

        return url;
    }

    
    public Certificate signCertificateSigningRequest(final CertificateProvider aCertificateProvider, final CertificateSigningRequest aCSR) {

        final CertificateSigningRequest sentCSR = new CertificateSigningRequest();
        sentCSR.setCsr(aCSR.getCsr());
        sentCSR.setSigner(new LocatedCertificate(aCSR.getSigner()));
        aCSR.getSignee().setSigner(null);
        sentCSR.setSignee(new DownstreamCertificate(aCSR.getSignee()));
        URL url = toUrl(aCertificateProvider.getHostName());
        RestCommand<CertificateSigningRequest, Certificate> command = new RestCommand<>(aCertificateProvider.getUserName(),
                aCertificateProvider.getPassword(), url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
        command.setUrl(aCertificateProvider.getHostName() + "/certificate/signCSR");
        command.setRequestModel(sentCSR);
        command.setResponseModel(Certificate.class);
        return command.post();
    }

    
    public Certificate findRemoteCertificate(final CertificateProvider aCertificateProvider, String anId) {
        URL url = toUrl(aCertificateProvider.getHostName());
        RestCommand<CertificateSigningRequest, Certificate> command = new RestCommand<>(aCertificateProvider.getUserName(),
                aCertificateProvider.getPassword(), url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
        command.setUrl(aCertificateProvider.getHostName() + "/certificate/" + anId);
        command.setResponseModel(Certificate.class);
        return command.get();
    }

}
