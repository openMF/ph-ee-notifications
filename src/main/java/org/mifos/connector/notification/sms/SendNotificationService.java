package org.mifos.connector.notification.sms;

import org.springframework.stereotype.Service;

@Service
public interface SendNotificationService {

    public void sendMessage(String message, Long number);

    public String receiveDeliveryStatus();
}
