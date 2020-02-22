package com.comp.code.controllers;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

	@GetMapping("/token")
	public Map<String, String> token(HttpSession session) {
		return Collections.singletonMap("token", session.getId());
	}
	
}
