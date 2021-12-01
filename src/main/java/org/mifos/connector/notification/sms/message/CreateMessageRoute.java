package org.mifos.connector.notification.sms.message;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.notification.sms.ProviderConfig;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.mifos.connector.notification.camel.config.CamelProperties.DELIVERY_MESSAGE;

@Component
public class CreateMessageRoute extends RouteBuilder {

    @Autowired
    private ProviderConfig providerConfig;

    @Autowired
    private ZeebeVariables zeebeVariables;


    @Override
        public void configure() throws Exception {

            from("direct:create-messages")
                    .id("create-messages")
                    .log(LoggingLevel.INFO, "Creating message")
                    .process(exchange ->{
                        exchange.setProperty(DELIVERY_MESSAGE, "message to be sent");
                    })
                    .log(LoggingLevel.INFO, "Creating message completed")
                   ;

        }
    }


