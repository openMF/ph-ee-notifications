package org.mifos.connector.notification.sms.message;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.mifos.connector.notification.provider.config.ProviderConfig;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.DELIVERY_MESSAGE;
import static org.mifos.connector.notification.camel.config.CamelProperties.PROVIDER_ID;

@Component
public class CreateMessageRoute extends RouteBuilder {

    @Autowired
    private ProviderConfig providerConfig;

    @Autowired
    private ZeebeVariables zeebeVariables;


    @Override
        public void configure() throws Exception {
        Map<String, Object> variables = new HashMap<>();
            from("direct:create-messages")
                    .id("create-messages")
                    .log(LoggingLevel.INFO, "Creating message")
                    .process(exchange ->{
                        variables.put(DELIVERY_MESSAGE, "Message to be Sent");
                    })
                    .log(LoggingLevel.INFO, "Creating message completed with message :${exchangeProperty."+DELIVERY_MESSAGE+"}")
                   ;

        }
    }


