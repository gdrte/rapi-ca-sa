package rapi.ca;

import org.jasypt.hibernate4.encryptor.HibernatePBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by gdr on 5/5/15.
 */
@Configuration
public class MiscConfiguration {
    @Value("${certificate.privateKey.encryptionKey}")
    private String encryptionKey;

    @Bean
    public HibernatePBEStringEncryptor createHibernatePBEStringEncryptor(){
        HibernatePBEStringEncryptor hpse = new HibernatePBEStringEncryptor();
        hpse.setRegisteredName("hibernateStringEncryptor");
        hpse.setPassword(this.encryptionKey);
       return hpse;
    }
}
