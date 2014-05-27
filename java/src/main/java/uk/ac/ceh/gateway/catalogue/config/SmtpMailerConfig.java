package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * The following spring JavaConfig defines the mail configuration for sending
 * emails over smtp. This only applies when running in a production environment
 * @author Christopher Johnson
 */
@Configuration
@Profile("production")
public class SmtpMailerConfig {
    @Value("${mail.host}") private String host;
    
    @Bean
    public MailSender mailSender() {
        JavaMailSenderImpl mailer = new JavaMailSenderImpl();
        mailer.setHost(host);
        return mailer;
    }
}