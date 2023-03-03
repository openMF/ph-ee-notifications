package org.mifos.connector.notification.template;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class TemplateDefaultConfig {


    @Value("${velocity.transactionid}")
    private String transactionId;

    @Value("${velocity.amount}")
    private String amount;

    @Value("${velocity.date}")
    private String date;

    @Value("${velocity.account}")
    private String account;

    @Value("${velocity.failure_type}")
    private String failType;

    @Value("${velocity.txnType}")
    private String txnType;

    @Value("${velocity.currency}")
    private String currency;

    @Value("${velocity.defaults.transactionid}")
    private String defaultTransactionId;

    @Value("${velocity.defaults.amount}")
    private String defaultAmount;



    @Value("${velocity.defaults.account}")
    private String defaultAccount;

    @Value("${velocity.defaults.failure_type}")
    private String defaultFailType;

    @Value("${velocity.defaults.txnType}")
    private String defaultTxnType;

    @Value("${velocity.defaults.currency}")
    private String defaultCurrency;

    public static String nvlTransactionId(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlAccount(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else {
            return value.replaceAll("\\d(?=(?:\\D*\\d){4})", "*");
        }

    }

    public static String nvlAmount(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlDate(String value, String alternateValue) {
        if (value.equals("null"))
            return String.valueOf(new Date().getTime());
        else
            return value;
    }

    public static String nvlCurrency(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlTxnType(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlFailType(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public TemplateConfig replaceTemplatePlaceholders(TemplateConfig templateConfig, Exchange exchange) {

        templateConfig.getVelocityContext().put(transactionId, nvlTransactionId(
                String.valueOf(exchange.getProperty(CORRELATION_ID)), defaultTransactionId));
        templateConfig.getVelocityContext().put(amount, nvlAmount(
                String.valueOf(exchange.getProperty(TRANSACTION_AMOUNT)), defaultAmount));
        templateConfig.getVelocityContext().put(date, nvlDate(
                String.valueOf(exchange.getProperty(DATE)), String.valueOf(new Date().getTime())));
        templateConfig.getVelocityContext().put(account, nvlAccount(
                String.valueOf(exchange.getProperty(ACCOUNT_ID)), defaultAccount));
        templateConfig.getVelocityContext().put(currency, nvlCurrency
                (String.valueOf(exchange.getProperty(CURRENCY)), defaultCurrency));
        templateConfig.getVelocityContext().put(txnType, nvlTxnType
                (String.valueOf(exchange.getProperty(TRANSACTION_TYPE)), defaultTxnType));
        templateConfig.getVelocityContext().put(failType, nvlFailType
                (String.valueOf(exchange.getProperty(ERROR_DESCRIPTION)), defaultFailType));
        return templateConfig;
    }
}
