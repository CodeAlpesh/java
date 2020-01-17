package com.comp.codename.demo.controllers;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController {

	@GetMapping("/api/resource")
	public Greeting greet() {
		return new Greeting("Hello Alpesh");
	}

	@PostMapping("/api/user")
	public Principal user(Principal user) {
		return user;
	}
}
