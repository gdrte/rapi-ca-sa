package rapi.ca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Created by gdr on 5/4/15.
 */
@SpringBootApplication
@EnableAutoConfiguration
public class RapiCA {

private static final Logger log = LoggerFactory
			.getLogger(RapiCA.class);
    public static void main(String args[]){
	System.setProperty("spring.config.name","rapi-ca");
	ApplicationContext ctx = SpringApplication.run(RapiCA.class, args);
	log.info("Rest Certificate Authority started...");
    }
}
