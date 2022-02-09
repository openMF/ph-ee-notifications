package org.mifos.connector.notification.sms.delivery;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.json.JSONArray;
import org.mifos.connector.notification.sms.dto.MessageResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.*;

@Component
public class DeliveryCallbackRoute extends RouteBuilder{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${messagegatewayconfig.protocol}")
    private String protocol;

    @Value("${messagegatewayconfig.host}")
    private String address;

    @Value("${messagegatewayconfig.port}")
    private int port;

    @Value("${operationsconfig.tenantid}")
    private String tenantId;

    @Value("${operationsconfig.tenantidvalue}")
    private String tenantIdValue;

    @Value("${operationsconfig.tenantappkey}")
    private String tenantAppKey;

    @Value("${operationsconfig.tenantappvalue}")
    private String tenantAppKeyValue;



    @Override
    public void configure() throws Exception {
            from("direct:delivery-notifications")
                    .id("delivery-notifications")
                    .choice()
                    .when(exchange -> Integer.parseInt(exchange.getProperty(RETRY_COUNT_CALLBACK).toString()) < 3)
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
                    .to(String.format("%s://%s/sms/report/?bridgeEndpoint=true", protocol, address))
                    .log(LoggingLevel.INFO, "Delivery Status Endpoint Received")
                    .process(exchange -> {
                        String id = exchange.getProperty(CORRELATION_ID, String.class);
                        String body= exchange.getIn().getBody(String.class);
                        JsonArray jsonParser = JsonParser.parseString(body).getAsJsonArray();
                        JsonObject jsonObject = jsonParser.get(0).getAsJsonObject();
                        int deliveryStatus = jsonParser.get(0).getAsJsonObject().get("deliveryStatus").getAsInt();
                        if(deliveryStatus == 300){
                            logger.info("Passed");
                            exchange.setProperty(MESSAGE_DELIVERY_STATUS,true);
                        }
                        else {
                            boolean hasError = jsonObject.get("hasError").getAsBoolean();
                            if(!hasError) {
                                if (jsonObject.has("errorMessage")) {
                                    if(jsonObject.get("errorMessage") != null) {
                                        logger.info("Error encountered: " + jsonObject.get("errorMessage"));
                                        exchange.setProperty(DELIVERY_ERROR_INFORMATION, jsonObject.get("errorMessage"));
                                        exchange.setProperty(MESSAGE_DELIVERY_STATUS, false);
                                    }
                                } else {
                                    logger.info("Still Pending, will retry");
                                }
                            }
                        }
                        Map<String, Object> newVariables = new HashMap<>();
                       if(exchange.getProperty(MESSAGE_DELIVERY_STATUS).equals(true)){
                           logger.info("Publishing variables: " + newVariables);
                           newVariables.put(MESSAGE_DELIVERY_STATUS, exchange.getProperty(MESSAGE_DELIVERY_STATUS));
                           zeebeClient.newPublishMessageCommand()
                                   .messageName(CALLBACK_MESSAGE)
                                   .correlationKey(id)
                                   .variables(newVariables)
                                   .timeToLive(Duration.ofMillis(timeToLive))
                                   .send()
                                   .join();
                       }
                       else if(exchange.getProperty(MESSAGE_DELIVERY_STATUS).equals(false)){
                           logger.info("Publishing variables: " + newVariables);
                           newVariables.put(DELIVERY_ERROR_MESSAGE, exchange.getProperty(DELIVERY_ERROR_INFORMATION));
                           newVariables.put(MESSAGE_DELIVERY_STATUS, exchange.getProperty(MESSAGE_DELIVERY_STATUS));
                           zeebeClient.newPublishMessageCommand()
                                   .messageName(CALLBACK_MESSAGE)
                                   .correlationKey(id)
                                   .variables(newVariables)
                                   .timeToLive(Duration.ofMillis(timeToLive))
                                   .send()
                                   .join();
                       }
                       else{
                           logger.info("Publishing created variables to variables: " + newVariables);
                           newVariables.put(CALLBACK_RETRY_COUNT,exchange.getProperty(RETRY_COUNT_CALLBACK));
                           zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                                   .variables(newVariables)
                                   .send()
                                   .join();
                       }


                     })
                    .otherwise()
                    .log("Callback Retry Over")
                    .process(exchange -> {
                        exchange.setProperty(MESSAGE_DELIVERY_STATUS,false);
                        String id = exchange.getProperty(CORRELATION_ID, String.class);
                        Map<String, Object> newVariables = new HashMap<>();
                        newVariables.put(MESSAGE_DELIVERY_STATUS, exchange.getProperty(MESSAGE_DELIVERY_STATUS));
                        newVariables.put(CALLBACK_RETRY_COUNT,exchange.getProperty(RETRY_COUNT_CALLBACK));
                        zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                                .variables(newVariables)
                                .send()
                                .join();
                        logger.info("Publishing created messages to variables: " + newVariables);
                        zeebeClient.newPublishMessageCommand()
                                .messageName(CALLBACK_MESSAGE)
                                .correlationKey(id)
                                .timeToLive(Duration.ofMillis(timeToLive))
                                .variables(newVariables)
                                .send()
                                .join();
                    });



        from("rest:POST:/sms/callback/")
                .id("delivery-callback")
                .log(LoggingLevel.INFO, "Waiting for delivery status callback")
                .choice()
                .when(exchange -> {
                    String callback = exchange.getIn().getBody(String.class);
                   return callback.contains((CharSequence) exchange.getProperty((INTERNAL_ID)));
                })
                .log("Message callback recieved. Continuing.")
                .process(exchange -> {
                    String id = exchange.getProperty(CORRELATION_ID, String.class);
                    String body= exchange.getIn().getBody(String.class);
                    JsonArray jsonParser = JsonParser.parseString(body).getAsJsonArray();
                    JsonObject jsonObject = jsonParser.get(0).getAsJsonObject();
                    int deliveryStatus = jsonParser.get(0).getAsJsonObject().get("deliveryStatus").getAsInt();
                    if(deliveryStatus == 300){
                        logger.info("Passed");
                        exchange.setProperty(MESSAGE_DELIVERY_STATUS,true);
                    }
                    else {
                        boolean hasError = jsonObject.get("hasError").getAsBoolean();
                        if(!hasError) {
                            if (jsonObject.has("errorMessage")) {
                                if(jsonObject.get("errorMessage") != null) {
                                    logger.info("Error encountered: " + jsonObject.get("errorMessage"));
                                    exchange.setProperty(DELIVERY_ERROR_INFORMATION, jsonObject.get("errorMessage"));
                                    exchange.setProperty(MESSAGE_DELIVERY_STATUS, false);
                                }
                            } else {
                                logger.info("Still Pending, will retry");
                            }
                        }
                    }
                    Map<String, Object> newVariables = new HashMap<>();
                    if(exchange.getProperty(MESSAGE_DELIVERY_STATUS).equals(true)){
                        logger.info("Publishing variables: " + newVariables);
                        newVariables.put(MESSAGE_DELIVERY_STATUS, exchange.getProperty(MESSAGE_DELIVERY_STATUS));
                        zeebeClient.newPublishMessageCommand()
                                .messageName(CALLBACK_MESSAGE)
                                .correlationKey(id)
                                .variables(newVariables)
                                .timeToLive(Duration.ofMillis(timeToLive))
                                .send()
                                .join();
                    }
                    else if(exchange.getProperty(MESSAGE_DELIVERY_STATUS).equals(false)){
                        logger.info("Publishing variables: " + newVariables);
                        newVariables.put(DELIVERY_ERROR_MESSAGE, exchange.getProperty(DELIVERY_ERROR_INFORMATION));
                        newVariables.put(MESSAGE_DELIVERY_STATUS, exchange.getProperty(MESSAGE_DELIVERY_STATUS));
                        zeebeClient.newPublishMessageCommand()
                                .messageName(CALLBACK_MESSAGE)
                                .correlationKey(id)
                                .variables(newVariables)
                                .timeToLive(Duration.ofMillis(timeToLive))
                                .send()
                                .join();
                    }
                    else{
                        logger.info("Publishing created variables to variables: " + newVariables);
                        newVariables.put(CALLBACK_RETRY_COUNT,exchange.getProperty(RETRY_COUNT_CALLBACK));
                        zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                                .variables(newVariables)
                                .send()
                                .join();
                    }
                })
                .otherwise()
                .log("Received callback did not correspond to sent message, Waiting");


        }
    }




