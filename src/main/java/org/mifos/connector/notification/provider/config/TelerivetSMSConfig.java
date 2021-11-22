package org.mifos.connector.notification.provider.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.telerivet.TelerivetAPI;
import com.telerivet.Project;


@Configuration
public class TelerivetSMSConfig {

    @Value("${provider.telerivet.api-key}")
    private String apiKey;

    @Value("${provider.telerivet.project-id}")
    private String projectId;


    @Bean
    @ConditionalOnProperty(
            value="provider.telerivet.enabled",
            havingValue = "true")
            public Project telerivetConfig(){
                TelerivetAPI tr = new TelerivetAPI(apiKey);
                Project project = tr.initProjectById(projectId);
            return project;
            }

}
