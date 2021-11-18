package org.mifos.connector.notification.provider;

import org.springframework.stereotype.Service;

@Service
public interface SendNotificationService {

    public void sendMessage(String message, Long number);

    public String receiveDeliveryStatus();
}
