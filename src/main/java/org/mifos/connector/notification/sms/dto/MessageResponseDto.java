/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.mifos.connector.notification.sms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;


public class MessageResponseDto {

	@JsonProperty("id")
	private String id;

	@JsonProperty("externalId")
	private String externalId;

	@JsonProperty("deliveredOnDate")
	private Date deliveredOnDate;

	@JsonProperty("deliveryStatus")
	private Integer deliveryStatus;

	@JsonProperty("errorMessage")
	private String errorMessage;


	public MessageResponseDto() {

	}
	@Override
	public String toString() {
		return "MessageResponseDto{" +
				"id='" + id + '\'' +
				", externalId='" + externalId + '\'' +
				", deliveredOnDate=" + deliveredOnDate +
				", deliveryStatus='" + deliveryStatus + '\'' +
				", errorMessage=" + errorMessage + '\'' +
				'}';
	}

	public String getId() {
		return id;
	}

	public String getExternalId() {
		return externalId;
	}

	public Date getDeliveredOnDate() {
		return deliveredOnDate;
	}

	public Integer getDeliveryStatus() {
		return deliveryStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
