package org.mifos.connector.notification.sms.delivery;

import io.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONArray;
import org.mifos.connector.common.gsma.dto.AccountNameResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class DeliveryCallbackRoute extends RouteBuilder{

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${hostconfig.protocol}")
    private String protocol;

    @Value("${hostconfig.host-address}")
    private String address;

    @Value("${hostconfig.port}")
    private int port;

    @Override
    public void configure() throws Exception {
        final int[] msg = new int[1];
            from("direct:delivery-notifications")
                    .id("delivery-notifications")
                    .log(LoggingLevel.INFO, "Calling delivery status API")
                    .setHeader("Fineract-Platform-TenantId", constant("default"))
                    .setHeader("Fineract-Tenant-App-Key", constant("123456543234abdkdkdkd"))
                    .process(exchange ->{
                        JSONArray response = new JSONArray();
                        response.put("123");
                        exchange.getIn().setBody(response.toString());

                        })
                    .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to(String.format("%s://%s:%d/sms/report/?bridgeEndpoint=true", protocol, address, port))
                    .log(LoggingLevel.INFO, "Delivery Status Endpoint Received")

            ;

        }
    }




