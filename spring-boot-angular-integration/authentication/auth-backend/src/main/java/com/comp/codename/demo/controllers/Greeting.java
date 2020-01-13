package com.comp.codename.demo.controllers;

import java.util.UUID;

public class Greeting {

	private String id;
	private String content;
	
	public Greeting(String message) {
		this.id = UUID.randomUUID().toString();
		this.content = message;
	}

	public Greeting(String id, String content) {
		this.id = id;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
