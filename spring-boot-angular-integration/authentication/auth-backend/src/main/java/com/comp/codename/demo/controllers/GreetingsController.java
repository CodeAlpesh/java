package com.comp.codename.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController {

	@GetMapping("/api/resource")
	public Greeting greet() {
		return new Greeting("Hello Alpesh");
	}
}
