package org.sunbird.cb.hubservices.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cb.hubservices.model.*;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.service.INotificationService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.*;

@Service
public class NotificationService implements INotificationService {

	private Logger logger = LoggerFactory.getLogger(NotificationService.class);
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	ConnectionProperties connectionProperties;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private ProfileUtils profileUtils;

	@Override
	public NotificationEvent buildEvent(String eventId, String sender, String reciepient, String status) {

		NotificationEvent notificationEvent = new NotificationEvent();

		if (eventId != null && sender != null && reciepient != null) {

			String fromUUID = sender;

			Map<String, List<String>> recipients = new HashMap<>();
			List<String> toList = Arrays.asList(reciepient);
			recipients.put(connectionProperties.getNotificationTemplateReciepient(), toList);

			logger.info("Notification sender --> {}", fromUUID);
			logger.info("Notification recipients --> {}", recipients);
			// values in body of notification template
			Map<String, Object> tagValues = new HashMap<>();
			tagValues.put(connectionProperties.getNotificationTemplateSender(), getUserName(fromUUID));
			tagValues.put(connectionProperties.getNotificationTemplateTargetUrl(),
					connectionProperties.getNotificationTemplateTargetUrlValue());
			tagValues.put(connectionProperties.getNotificationTemplateStatus(), status);

			notificationEvent.setMode(connectionProperties.getNotificationv2Mode());
			notificationEvent.setDeliveryType(connectionProperties.getNotificationv2DeliveryType());
			// replace recipient ids to email ids
			List<Map<String, Object>> profiles = profileUtils.getUserProfiles(toList);
			List<String> toListMails = new ArrayList<>();
			profiles.forEach(profile -> {
				toListMails.add(((Map<String, Object>) profile.get(Constants.Profile.PERSONAL_DETAILS))
						.get("primaryEmail").toString());
			});
			logger.info("to mail list = "+ toListMails);
			notificationEvent.setIds(toListMails);

			NotificationConfig configV2 = new NotificationConfig();
			configV2.setSender(connectionProperties.getNotificationv2Sender());
			configV2.setSubject(eventId);
			notificationEvent.setConfig(configV2);

			NotificationTemplate templateV2 = new NotificationTemplate();
			templateV2.setId(connectionProperties.getNotificationv2Id());
			Map<String, String> params = new HashMap<>();
			if (eventId.equals(connectionProperties.getNotificationTemplateRequest()))
				params.put("body", replaceWith(connectionProperties.getNotificationv2RequestBody(), tagValues));
			else if (eventId.equals(connectionProperties.getNotificationTemplateResponse()))
				params.put("body", replaceWith(connectionProperties.getNotificationv2ResponseBody(), tagValues));

			templateV2.setParams(params);
			notificationEvent.setTemplate(templateV2);

		}
		logger.info("notification event = "+notificationEvent);
		return notificationEvent;

	}


	@Override
	public PushNotification buildNewEvent(String eventId, String sender, String reciepient, String status) {

		PushNotification notificationEvent = new PushNotification();

		if (eventId != null && sender != null && reciepient != null) {

			String fromUUID = sender;

			Map<String, List<String>> recipients = new HashMap<>();
			List<String> toList = Arrays.asList(reciepient);
			recipients.put(connectionProperties.getNotificationTemplateReciepient(), toList);

			logger.info("Notification sender --> {}", fromUUID);
			logger.info("Notification recipients --> {}", recipients);
			Map<String, Object> tagValues = new HashMap<>();
			tagValues.put(connectionProperties.getNotificationTemplateSender(), getUserName(fromUUID));
			tagValues.put(connectionProperties.getNotificationTemplateTargetUrl(),
					connectionProperties.getNotificationTemplateTargetUrlValue());
			tagValues.put(connectionProperties.getNotificationTemplateStatus(), status);
			notificationEvent.setDataValue("Push notification");
			PushNotificationConfig config = new PushNotificationConfig();
			config.setSubject("Push deploy notification");
			PushNotificationTemplate template =new PushNotificationTemplate();
			template.setConfig(config);
			TemplateString tempdata = new TemplateString();
			template.setData(tempdata.getData());
			template.setType("email");
			template.setId("cbplanContentRequestTemplate");
			PushNotificationAction action = new PushNotificationAction();
			action.setCategory("email");
			action.setType("email");
			action.setTemplate(template);
			PushNotificationData data = new PushNotificationData();
			data.setType("email");
			data.setPriority(1);
			data.setAction(action);
			ArrayList d = new ArrayList<>();
			d.add(data);
			notificationEvent.setData(d);
			notificationEvent.setIsScheduleNotification(false);
			FilterConfig filter = new FilterConfig();
			filter.setUserId(toList.get(0));
			notificationEvent.setFilter(filter);
		}
		logger.info("notification event = "+notificationEvent);
		return notificationEvent;

	}
	private String getUserName(String uuid) {

		String fromName = null;
		try {

			Map<String, Object> profile = profileUtils.getUserProfiles(Arrays.asList(uuid)).get(0);

			if (profile != null) {

				JsonNode dataNode = mapper.convertValue(profile, JsonNode.class);
				logger.info("profile dataNode :-{}", dataNode);

				JsonNode profilePersonalDetails = dataNode.get(Constants.Profile.PERSONAL_DETAILS);
				fromName = profilePersonalDetails.get(Constants.Profile.FIRST_NAME).asText();
			} else {
				fromName = Constants.Profile.HUB_MEMBER;
			}
		} catch (Exception e) {
			logger.error("Profile name could not be extracted :-{}", e.getMessage());
			fromName = Constants.Profile.HUB_MEMBER;

		}

		return fromName;
	}

	@Override
	public ResponseEntity postEvent(NotificationEvent notificationEventv2) {

		ResponseEntity<?> response = null;
		try {
			final String uri = connectionProperties.getNotificationIp()
					.concat(connectionProperties.getNotificationEventEndpoint());
			RestTemplate restTemplate = new RestTemplate();
			logger.info("uri = "+uri);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			logger.info(String.format("Notification event v2 value :: %s", notificationEventv2));
			Map<String, List<NotificationEvent>> notifications = new HashMap<>();
			notifications.put("notifications", Arrays.asList(notificationEventv2));
			Map<String, Object> nrequest = new HashMap<>();
			nrequest.put("request", notifications);
			logger.info(String.format("Notification event v2 value :: %s", nrequest));
			HttpEntity request = new HttpEntity<>(nrequest, headers);
			response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);

			logger.info(Constants.Message.SENT_NOTIFICATION_SUCCESS, response.getStatusCode());

		} catch (Exception e) {
			logger.error(Constants.Message.SENT_NOTIFICATION_ERROR + ":{}", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return response;
	}



	private String replaceWith(String templateStr, Map<String, Object> tagValues) {
		for (Map.Entry entry : tagValues.entrySet()) {
			if (templateStr.contains(entry.getKey().toString())) {
				templateStr = templateStr.replace(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		logger.info(String.format("replaceWith value ::%s", templateStr));

		return templateStr;
	}

	@Override
	public ResponseEntity postNewEvent(PushNotification pushNotification) {

		ResponseEntity<?> response = null;
		try {
			final String uri = "http://learner-service:9000/private/user/feed/v2/create";
			RestTemplate restTemplate = new RestTemplate();
			logger.info("uri = "+uri);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
//			headers.set("Authorization","bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI0WEFsdFpGMFFhc1JDYlFnVXB4b2RvU2tLRUZyWmdpdCJ9.mXD7cSvv3Le6o_32lJplDck2D0IIMHnv0uJKq98YVwk");
//			headers.set("x-authenticated-user-token","eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5YnBaRzhRQWF1SnZodUl2a2VqM2RVU2g3MUMzLTlvc21TdlhNeFBrbENRIn0.eyJqdGkiOiI0OTgzOWZjNC1iNWI2LTQyMjAtYWFjMi03ZTFmOWE1Yjk2NTciLCJleHAiOjE3MTAzNDg2MDYsIm5iZiI6MCwiaWF0IjoxNzEwMzA1NDA2LCJpc3MiOiJodHRwczovL2NvbXBhc3MtZGV2LnRhcmVudG8uY29tL2F1dGgvcmVhbG1zL3N1bmJpcmQiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZjo0NDBjZTUzNi01NTYxLTRjYzgtOGZjMy1mZTRmMjUyMTBiZWY6NjMxNDE4NDEtODQ1ZC00ZDJlLTgyMDMtMGQyYzY0YjRkOTc5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG1zIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiZmU3ZWUxNzgtNTZhNi00NmE1LTgxMmUtMTY1ZmM2NWY5Y2I0IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjMwMDAvKiIsImh0dHBzOi8vY29tcGFzcy1kZXYudGFyZW50by5jb20iLCJodHRwOi8vbG9jYWxob3N0OjMwMDAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6IiIsIm5hbWUiOiJUb255IFN0YXJrIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidG9ueUB5b3BtYWlsLmNvbSIsImdpdmVuX25hbWUiOiJUb255IiwiZmFtaWx5X25hbWUiOiJTdGFyayIsImVtYWlsIjoidG8qKkB5b3BtYWlsLmNvbSJ9.HZ6sFLPhTdRBa7SxHkc4z66YikUJIIYcOd6RjlqYUNouxJReHotc6jNh0dbybV2qyVAz-V0QqzVWp-w9dxAPKUC_fX719lkv43V8rmUw-lfX--rz3_5eFdd44xvpiFxSRNlEgGYnou_gCUkyDWpYF3oin_8RmLpXHd5utW-a0ovi1cPQ6nuO6Nn36fYG3Yqy-cSVcd2ktTSvbKsTh8s9koVkE0Cj4lT63mKq2GEwYtm1Gsd5kcW2D71ArFxPYqu5k_LT6h0RZ16qwJeHK0s89D8tt-TnBaIXAVmtKNMVclyEn3L2d1G8h_AAdpgs0wlkxaglR18bTDTJYCbKMw6amA");
			logger.info(String.format("Notification event v2 value :: %s", pushNotification));
			Map<String, Object> nrequest = new HashMap<>();
			nrequest.put("request", pushNotification);
			logger.info(String.format("Notification event v2 value :: %s", nrequest));
			HttpEntity request = new HttpEntity<>(nrequest, headers);
			response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);

			logger.info(Constants.Message.SENT_NOTIFICATION_SUCCESS, response.getStatusCode());

		} catch (Exception e) {
			logger.error(Constants.Message.SENT_NOTIFICATION_ERROR + ":{}", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return response;
	}
}
