package org.sunbird.cb.hubservices.service;

import org.springframework.http.ResponseEntity;
import org.sunbird.cb.hubservices.model.NotificationEvent;
import org.sunbird.cb.hubservices.model.PushNotification;

public interface INotificationService {

	/**
	 * Build the notification request
	 * @param eventId
	 * @param sender
	 * @param reciepient
	 * @param status
	 * @return
	 */
	NotificationEvent buildEvent(String eventId, String sender, String reciepient, String status);

    PushNotification buildNewEvent(String eventId, String sender, String reciepient, String status);

    ResponseEntity postEvent(NotificationEvent notificationEventV2);


	ResponseEntity postNewEvent(PushNotification pushNotification);
}
