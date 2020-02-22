package com.comp.code.resourceserver.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*", methods= {RequestMethod.GET, RequestMethod.OPTIONS})
public class ResourceController {

	@CrossOrigin(
		origins = "*", 
		maxAge=30, 
		allowedHeaders={"x-auth-token", "x-requested-with", "x-xsrf-token"}
	)
	@RequestMapping(value = "/api/resource", method = RequestMethod.GET)
	public Greeting message() {
		return new Greeting("Hello");
	}
	
}