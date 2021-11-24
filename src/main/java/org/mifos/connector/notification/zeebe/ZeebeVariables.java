package org.mifos.connector.notification.zeebe;

import org.springframework.stereotype.Component;

@Component
public class ZeebeVariables {

    private ZeebeVariables() {
    }

    public static final String TRANSACTION_ID = "transactionId";
}