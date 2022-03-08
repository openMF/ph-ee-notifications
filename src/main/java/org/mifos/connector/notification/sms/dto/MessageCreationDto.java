package org.mifos.connector.notification.sms.dto;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

import static org.mifos.connector.notification.zeebe.ZeebeVariables.*;
import static org.mifos.connector.notification.zeebe.ZeebeVariables.TRANSACTION_ID;

@Component
public class MessageCreationDto {

    @Autowired
    private CamelContext camelContext;

    DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss");

    public Exchange setPropertiesForMessage(Map<String, Object> variables){
        String transactionId = (String) variables.get(TRANSACTION_ID);
        String account = (String) variables.get(ACCOUNT);
        long originDate = (Long) variables.get(ORIGIN_DATE);
        LocalDate localDate = Instant.ofEpochMilli(originDate)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        String amount = (String) variables.get(AMOUNT);
        Exchange exchange = new DefaultExchange(camelContext);

        if(variables.containsKey(SERVER_TRANSACTION_RECEIPT_NUMBER)){
            exchange.setProperty(CORRELATION_ID, variables.get(SERVER_TRANSACTION_RECEIPT_NUMBER).toString());
        }
        else {
            exchange.setProperty(CORRELATION_ID, transactionId);
        }
        exchange.setProperty(DATE, localDate);
        exchange.setProperty(ACCOUNT_ID,account);
        exchange.setProperty(TRANSACTION_AMOUNT,amount);
        return exchange;
    }
}
