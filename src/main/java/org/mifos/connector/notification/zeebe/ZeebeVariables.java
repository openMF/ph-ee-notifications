package org.mifos.connector.notification.zeebe;

import org.springframework.stereotype.Component;

@Component
public class ZeebeVariables {

    private ZeebeVariables() {
    }

    public static final String TRANSACTION_ID = "transactionId";
    public static final String DELIVERY_STATUS = "notification-request";
    public static final String MESSAGE_TO_SEND = "deliveryMessage";
    public static final String MESSAGE_INTERNAL_ID = "internalId";
}