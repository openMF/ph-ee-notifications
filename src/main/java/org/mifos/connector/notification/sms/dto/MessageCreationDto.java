package org.mifos.connector.notification.sms.dto;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        Long originDate = (Long) variables.get(ORIGIN_DATE);
        Date date=new Date(originDate);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        String dateText = df2.format(date);
        String amount = (String) variables.get(AMOUNT);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty(CORRELATION_ID, transactionId);
        exchange.setProperty(DATE, dateText);
        exchange.setProperty(ACCOUNT_ID,account);
        exchange.setProperty(TRANSACTION_AMOUNT,amount);
        return exchange;
    }
}
