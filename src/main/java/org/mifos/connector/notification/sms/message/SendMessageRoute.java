package org.mifos.connector.notification.sms.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.notification.provider.config.ProviderConfig;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.mifos.connector.common.channel.dto.TransactionChannelCollectionRequestDTO;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class SendMessageRoute extends RouteBuilder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProviderConfig providerConfig;

    @Autowired
    private ZeebeVariables zeebeVariables;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

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


            from("direct:send-notifications")
                    .id("send-notifications")
                    .log(LoggingLevel.INFO, "Sending success for ${exchangeProperty."+PROVIDER_ID+"}")
                    .setHeader(tenantId, constant(tenantIdValue))
                    .setHeader(tenantAppKey, constant(tenantAppKeyValue))
                    .process(exchange ->{
                        String mobile = exchange.getProperty(MOBILE_NUMBER).toString();
                        Long internalId = Long.parseLong(exchange.getProperty(INTERNAL_ID).toString());
                        int providerId = providerConfig.getProviderConfig();

                        JSONObject request = new JSONObject();
                        JSONArray jArray = new JSONArray();
                        request.put("internalId", internalId);
                        request.put("mobileNumber", mobile);
                        request.put("message",  exchange.getProperty(DELIVERY_MESSAGE));
                        request.put("providerId", providerId);
                        exchange.getIn().setBody(jArray.put(request).toString());
                    })
                    .log("${body}")
                    .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to(String.format("%s://%s:%d/sms/?bridgeEndpoint=true", protocol, address, port))
                    .log(LoggingLevel.INFO, "Sending sms to message gateway completed")
                    .process(exchange ->{
                        String id = exchange.getProperty(CORRELATION_ID, String.class);
                        Map<String, Object> variables = new HashMap<>();
                        variables.put(INTERNAL_ID,exchange.getProperty(INTERNAL_ID));
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


