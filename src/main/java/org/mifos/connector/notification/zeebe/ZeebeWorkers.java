package org.mifos.connector.notification.zeebe;

import io.zeebe.client.ZeebeClient;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ZeebeWorkers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {
        zeebeClient.newWorker()
                .jobType("transaction-failure")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);
                    producerTemplate.send("direct:create-messages", exchange);
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("transaction-failure")
                .maxJobsActive(workerMaxJobs)
                .open();


//        zeebeClient.newWorker()
//                .jobType("transaction-success")
//                .handler((client, job) -> {
//                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
//                    client.newCompleteCommand(job.getKey())
//                            .send()
//                    ;
//                })
//                .name("transaction-success")
//                .maxJobsActive(workerMaxJobs)
//                .open();

        zeebeClient.newWorker()
                .jobType("notification-service")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);
                    producerTemplate.send("direct:send-notifications", exchange);
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("notification-service")
                .maxJobsActive(workerMaxJobs)
                .open();



        zeebeClient.newWorker()
                .jobType("get-notification-status")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);
                    producerTemplate.send("direct:delivery-notifications", exchange);
                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name("get-notification-status")
                .maxJobsActive(workerMaxJobs)
                .open();

    }
}