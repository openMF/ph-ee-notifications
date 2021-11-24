package org.mifos.connector.notification.sms;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class SMSGatewayImpl extends RouteBuilder {

    @Autowired
    private ProviderConfig providerConfig;

    @Autowired
    private ZeebeVariables zeebeVariables;


    @Override
        public void configure() throws Exception {

            from("direct:success-notifications")
                    .id("success-notifications")
                    .log(LoggingLevel.INFO, "Sending success for ${exchangeProperty."+PROVIDER_ID+"}")
                    .setHeader("Fineract-Platform-TenantId", constant("default"))
                    .setHeader("Fineract-Tenant-App-Key", constant("123456543234abdkdkdkd"))
                    .process(exchange ->{
                        JSONObject response = new JSONObject();
                        int providerId = providerConfig.getProviderConfig();
                        response.put("internalId", "56765");
                        response.put("mobileNumber", "21223");
                        response.put("message", "sent from notifications to message gateway");
                        response.put("providerId", providerId);
                        JSONArray ja = new JSONArray();
                            exchange.getIn().setBody(ja.put(response).toString());

                    })
                    .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to("http://127.0.0.1:9191/sms/?bridgeEndpoint=true")
                    .log(LoggingLevel.INFO, "Sending sms to message gateway completed")
                   ;

        }
    }


