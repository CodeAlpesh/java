package com.comp.code.controllers;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {"/api", "/rest"})
public class GreetingsController {

	@GetMapping("/resource")
	public Greeting greet() {
		return new Greeting("Hello Alpesh");
	}

	@PostMapping("/user")
	public Principal user(Principal user) {
		return user;
	}
	
}