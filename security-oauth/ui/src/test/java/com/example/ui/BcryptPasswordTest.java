package com.example.ui;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptPasswordTest {

	public static void main(String[] args) {
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encoded = encoder.encode("Hello");
		System.out.println(encoded);
		System.out.println(encoder.matches("Hello", encoded));
		System.out.println("-----------------------------");
		encoded = encoder.encode("Hello");
		System.out.println(encoded);
		System.out.println(encoder.matches("Hello", encoded));
	}
}
