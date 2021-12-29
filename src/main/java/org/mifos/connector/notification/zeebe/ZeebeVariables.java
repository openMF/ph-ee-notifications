package org.mifos.connector.notification.zeebe;

import org.springframework.stereotype.Component;

@Component
public class ZeebeVariables {

    private ZeebeVariables() {
    }

    public static final String TRANSACTION_ID = "transactionId";
    public static final String MESSAGE_DELIVERY_STATUS = "isMessageDelivered";
    public static final String CALLBACK_MESSAGE = "notification-request";
    public static final String MESSAGE_TO_SEND = "deliveryMessage";
    public static final String MESSAGE_INTERNAL_ID = "internalId";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final int CALLBACK_RETRY_COUNT = 0;
}