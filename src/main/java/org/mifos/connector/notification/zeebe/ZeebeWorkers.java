package org.mifos.connector.notification.zeebe;

import io.zeebe.client.ZeebeClient;

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
    private ZeebeClient zeebeClient;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {
        zeebeClient.newWorker()
                    .jobType("create-message-to-send")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("create-message-to-send")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("send-message-to-server")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("send-message-to-server")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("get-delivery-status")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("get-delivery-status")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("waiting-for-callback")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    client.newCompleteCommand(job.getKey())
                            .send()
                    ;
                })
                .name("waiting-for-callback")
                .maxJobsActive(workerMaxJobs)
                .open();


        zeebeClient.newWorker()
                .jobType("send-sms-success")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name("send-sms-success")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("send-sms-failure")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                })
                .name("send-sms-failure")
                .maxJobsActive(workerMaxJobs)
                .open();
    }
}