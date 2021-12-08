package org.mifos.connector.notification.sms.delivery;


import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.json.JsonObject;
import org.json.JSONArray;
import org.mifos.connector.common.gsma.dto.AccountNameResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class DeliveryCallbackRoute extends RouteBuilder{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${hostconfig.protocol}")
    private String protocol;

    @Value("${hostconfig.host}")
    private String address;

    @Value("${hostconfig.port}")
    private int port;

    @Value("${fineractconfig.tenantid}")
    private String tenantId;

    @Value("${fineractconfig.tenantidvalue}")
    private String tenantIdValue;

    @Value("${fineractconfig.tenantappkey}")
    private String tenantAppKey;

    @Value("${fineractconfig.tenantappvalue}")
    private String tenantAppKeyValue;



    @Override
    public void configure() throws Exception {
        final int[] msg = new int[1];
            from("direct:delivery-notifications")
                    .id("delivery-notifications")
                    .log(LoggingLevel.INFO, "Calling delivery status API")
                    .setHeader(tenantId, constant(tenantIdValue))
                    .setHeader(tenantAppKey, constant(tenantAppKeyValue))
                    .setBody(exchange -> {
                        JSONArray request = new JSONArray();
                        Long internalId = Long.parseLong(exchange.getProperty(INTERNAL_ID).toString());
                        request.put(internalId);
                        return  request.toString();
                    })
                    .log("${body}")
                    .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to(String.format("%s://%s:%d/sms/report/?bridgeEndpoint=true", protocol, address, port))
                    .log(LoggingLevel.INFO, "Delivery Status Endpoint Received")
                    .process(exchange -> {
                        String id = exchange.getProperty(CORRELATION_ID, String.class);
                        Map<String, Object> variables = new HashMap<>();
                        String callback = exchange.getIn().getBody(String.class);
                        if(callback.contains("200")){
                            logger.info("Still Pending");
                            variables.put(DELIVERY_STATUS,"false");
                        }
                       else{
                           logger.info("Passed");
                            variables.put(DELIVERY_STATUS,"true");
                        }
                        logger.info("Publishing created messages to variables: " + variables);
                        zeebeClient.newPublishMessageCommand()
                                .messageName(DELIVERY_STATUS)
                                .correlationKey(id)
                                .timeToLive(Duration.ofMillis(timeToLive))
                                .variables(variables)
                                .send()
                                .join();
                     })

            ;


        }
    }




