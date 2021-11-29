package org.sunbird.cb.hubservices.network.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.cb.hubservices.model.ConnectionRequestV2;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.serviceimpl.ConnectionService;
import org.sunbird.cb.hubservices.util.Constants;

@RestController
@RequestMapping("/connections")
public class UserConnectionCrudController {

	@Autowired
	private ConnectionService connectionService;

	@PostMapping("/add")
	public ResponseEntity<Response> add(@RequestHeader String rootOrg, @RequestBody ConnectionRequestV2 request)
			throws Exception {
		request.setStatus(Constants.Status.PENDING);
		Response response = connectionService.upsert(rootOrg, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);

	}

	@PostMapping("/update")
	public ResponseEntity<Response> update(@RequestHeader String rootOrg, @RequestBody ConnectionRequestV2 request)
			throws Exception {
		String connectionId = request.getUserIdTo();
		String userId = request.getUserIdFrom();
		request.setUserNameFrom(connectionId);
		request.setUserDepartmentTo(userId);
		Response response = connectionService.upsert(rootOrg, request);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
