package org.mifos.connector.notification.sms.message;

import io.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.notification.sms.ProviderConfig;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class SendMessageRoute extends RouteBuilder {

    @Autowired
    private ProviderConfig providerConfig;

    @Autowired
    private ZeebeVariables zeebeVariables;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Value("${hostconfig.protocol}")
    private String protocol;

    @Value("${hostconfig.host-address}")
    private String address;

    @Value("${hostconfig.port}")
    private int port;


    @Override
        public void configure() throws Exception {

            from("direct:send-notifications")
                    .id("send-notifications")
                    .log(LoggingLevel.INFO, "Sending success for ${exchangeProperty."+PROVIDER_ID+"}")
                    .setHeader("Fineract-Platform-TenantId", constant("default"))
                    .setHeader("Fineract-Tenant-App-Key", constant("123456543234abdkdkdkd"))
                    .process(exchange ->{
                        JSONObject response = new JSONObject();
                        int providerId = providerConfig.getProviderConfig();
                        response.put("internalId", "234");
                        response.put("mobileNumber", "2343432");
                        response.put("message", exchange.getProperty(DELIVERY_MESSAGE));
                        response.put("providerId", providerId);
                        JSONArray ja = new JSONArray();
                            exchange.getIn().setBody(ja.put(response).toString());

                    })
                    .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to(String.format("%s://%s:%d/sms/?bridgeEndpoint=true", protocol, address, port))
                    .log(LoggingLevel.INFO, "Sending sms to message gateway completed")
                    .process(exchange ->{
                        String id = "123";
                        zeebeClient.newPublishMessageCommand()
                                .messageName(DELIVERY_STATUS)
                                .correlationKey(id)
                                .timeToLive(Duration.ofMillis(timeToLive))
                                .variables("Waiting for Callback")
                                .send()
                                .join();
                    })
                   ;

        }
    }


