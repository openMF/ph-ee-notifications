package org.mifos.connector.notification.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {
    @Value("${provider.telerivet.id}")
    private int id;


    @Bean
    @ConditionalOnProperty(
            value="provider.telerivet.enabled",
            havingValue = "true")
    public int getProviderConfig(){
        return id;
    }

}
