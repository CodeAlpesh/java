package com.comp.code.resourceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
public class ResourceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceServerApplication.class, args);
	}
	
	@Configuration
	class ResourceSecurity extends WebSecurityConfigurerAdapter {
		
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.httpBasic().disable();
		    http.authorizeRequests().anyRequest().authenticated();
		}
		
		/*
		 * To prevent the browser from popping up authentication dialogs (incase of auth failures).
		 * - Explicitly disable HTTP Basic in the resource server.
		 * OR
		 * An alternative: 
		 * - Keep HTTP Basic but change the 401 challenge to something other than "Basic". 
		 * - You can do that with a one-line implementation of AuthenticationEntryPoint
		 * 
		 * But for spring-boot 2.2.x ... Spring security defaults to forms login. So 401 will send a redirect.
		 */
		
	}

}
