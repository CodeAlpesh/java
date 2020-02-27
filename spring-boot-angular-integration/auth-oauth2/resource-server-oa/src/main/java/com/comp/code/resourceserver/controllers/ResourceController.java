package com.comp.code.resourceserver.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceController {

	@CrossOrigin(origins="*", maxAge=10, allowedHeaders="*")
	@RequestMapping(value = "/api/resource", method = RequestMethod.GET)
	public Greeting message() {
		return new Greeting("Hello");
	}
	
}