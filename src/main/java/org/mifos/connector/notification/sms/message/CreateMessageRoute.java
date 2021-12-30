package org.mifos.connector.notification.sms.message;


import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.mifos.connector.notification.provider.config.ProviderConfig;
import org.mifos.connector.notification.template.TemplateConfig;
import org.mifos.connector.notification.zeebe.ZeebeVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.*;


@Component
public class CreateMessageRoute extends RouteBuilder {

    @Autowired
    private ProviderConfig providerConfig;


    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private TemplateConfig templateConfig;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Value("${velocity.transactionid}")
    private String transactionId;

    @Value("${velocity.amount}")
    private String amount;

    @Value("${velocity.date}")
    private String date;

    @Value("${velocity.account}")
    private String account;




    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
        public void configure() throws Exception {

            from("direct:create-failure-messages")
                    .id("create-messages")
                    .log(LoggingLevel.INFO, "Creating message")
                    .process(exchange ->{
                        StringWriter message = new StringWriter();
                        account= account.replaceAll("\\d(?=(?:\\D*\\d){4})", "*");
                        templateConfig.getVelocityContext().put(transactionId,exchange.getProperty(CORRELATION_ID));
                        templateConfig.getVelocityContext().put(amount,exchange.getProperty(TRANSACTION_AMOUNT));
                        templateConfig.getVelocityContext().put(date,exchange.getProperty(DATE));
                        templateConfig.getVelocityContext().put(account,exchange.getProperty(ACCOUNT_ID));
                        templateConfig.getFailureTemplate().merge(templateConfig.getVelocityContext(),message);
                        exchange.setProperty(DELIVERY_MESSAGE, message);
                        Map<String, Object> newVariables = new HashMap<>();
                        newVariables.put(MESSAGE_TO_SEND, exchange.getProperty(DELIVERY_MESSAGE).toString());
                        newVariables.put(MESSAGE_INTERNAL_ID,exchange.getProperty(INTERNAL_ID).toString());
                        zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                                .variables(newVariables)
                                .send()
                                .join();
                    })
                    .log(LoggingLevel.INFO, "Creating message completed with message :${exchangeProperty."+DELIVERY_MESSAGE+"}")
                   ;



        from("direct:create-success-messages")
                .id("create-success messages")
                .log(LoggingLevel.INFO, "Drafting success message")
                .process(exchange ->{
                    StringWriter message = new StringWriter();
                    account= account.replaceAll("\\d(?=(?:\\D*\\d){4})", "*");
                    templateConfig.getVelocityContext().put(amount,exchange.getProperty(TRANSACTION_AMOUNT));
                    templateConfig.getVelocityContext().put(date,exchange.getProperty(DATE));
                    templateConfig.getVelocityContext().put(account,exchange.getProperty(ACCOUNT_ID));
                    templateConfig.getSuccessTemplate().merge(templateConfig.getVelocityContext(),message);
                    exchange.setProperty(DELIVERY_MESSAGE, message);
                    Map<String, Object> newVariables = new HashMap<>();
                    newVariables.put(MESSAGE_TO_SEND, exchange.getProperty(DELIVERY_MESSAGE).toString());
                    newVariables.put(MESSAGE_INTERNAL_ID,exchange.getProperty(INTERNAL_ID).toString());
                    zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                            .variables(newVariables)
                            .send()
                            .join();
                })
                .log(LoggingLevel.INFO, "Creating message completed with message :${exchangeProperty."+DELIVERY_MESSAGE+"}")
        ;







    }
    }


