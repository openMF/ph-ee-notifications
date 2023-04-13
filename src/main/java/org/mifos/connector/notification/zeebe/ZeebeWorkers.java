package org.mifos.connector.notification.zeebe;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.mifos.connector.notification.sms.dto.MessageCreationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.TRANSACTION_ID;

@Component
public class ZeebeWorkers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageCreationDto messageCreationDto;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {
        zeebeClient.newWorker()
                .jobType("transaction_failure")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Map<String, Object> variables = job.getVariablesAsMap();
                    Exchange exchange = messageCreationDto.setPropertiesForMessage(variables);
                    String internalId = String.valueOf(job.getProcessInstanceKey());
                    exchange.setProperty(INTERNAL_ID,internalId);
                    producerTemplate.send("direct:create-failure-messages", exchange);
                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join()
                    ;
                })
                .name("transaction_failure")
                .maxJobsActive(workerMaxJobs)
                .open();


        zeebeClient.newWorker()
                .jobType("transaction_success")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Map<String, Object> variables = job.getVariablesAsMap();
                    Exchange exchange = messageCreationDto.setPropertiesForMessage(variables);
                    String internalId = String.valueOf(job.getProcessInstanceKey());
                    exchange.setProperty(INTERNAL_ID,internalId);
                    producerTemplate.send("direct:create-success-messages", exchange);
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("transaction_success")
                .maxJobsActive(workerMaxJobs)
                .open();
    }
}