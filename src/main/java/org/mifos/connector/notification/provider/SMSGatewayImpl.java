package org.mifos.connector.notification.provider;

import com.telerivet.Message;
import com.telerivet.Project;
import com.telerivet.TelerivetAPI;
import com.telerivet.Util;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.gsma.dto.RequestStateDTO;
import org.mifos.connector.notification.provider.config.TelerivetSMSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;
import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class SMSGatewayImpl extends RouteBuilder {

    @Autowired
    private TelerivetSMSConfig telerivetSMSConfig;

    @Override
        public void configure() throws Exception {
            from("direct:success-notifications")
                    .id("success-notifications")
                    .log(LoggingLevel.INFO, "Sending success for ${exchangeProperty."+PROVIDER_NAME+"}")
                    .process(exchange -> {


                    });


        }
    }


