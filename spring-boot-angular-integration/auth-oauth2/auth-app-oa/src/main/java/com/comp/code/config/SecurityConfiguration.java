package com.comp.code.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
				.withUser("alpesh").password("{noop}password").authorities("ADMIN", "API_USER", "USER")
				.and()
				.withUser("foo").password("{noop}bar").authorities("ADMIN", "API_USER", "USER");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.authorizeRequests()
			.antMatchers("/", "/**.js", "index.html", "/**.ico")
				.permitAll()
			.anyRequest()
				.authenticated()
		.and()
			.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/api/login")
				.successHandler(new AppAuthenticationSuccessHandler())
				.failureHandler(new AppAuthenticationFailureHandler())
		.and()
			.logout()
				.logoutSuccessHandler(new AppLogoutSuccessHandler())
		.and()
			.exceptionHandling()
				.authenticationEntryPoint(new AppAuthenticationEntryPoint())
		.and()
			.csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
				
	}

}


class AppAuthenticationSuccessHandler extends ResponseProcessor implements  AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request, 
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		process(response, authentication, null);
	}
}

class AppAuthenticationFailureHandler extends ResponseProcessor implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request, 
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		process(response, null, exception);
		
	}				
}

class AppLogoutSuccessHandler extends ResponseProcessor implements LogoutSuccessHandler {

	@Override
	public void onLogoutSuccess(
			HttpServletRequest request, 
			HttpServletResponse response, 
			Authentication authentication) throws IOException, ServletException {
		
		process(response, authentication, null);
		
	}
	
}

//Alternatively you can use  HttpStatusEntryPoint ep = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
class AppAuthenticationEntryPoint extends ResponseProcessor implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		
		process(response, null, authException);
	}
	
}

abstract class ResponseProcessor {
	
	void process(HttpServletResponse response, Authentication authentication, AuthenticationException exception) throws IOException {
		
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
		Map<String, Object> authResponse = new HashMap<String, Object>();
		
		System.out.println("==========>" + this.getClass().getName());
		System.out.println("==========>" + (this instanceof AuthenticationFailureHandler));
		System.out.println("==========>" + AuthenticationFailureHandler.class.isAssignableFrom(this.getClass()));
		System.out.println("==========>" + AuthenticationEntryPoint.class.isAssignableFrom(this.getClass()));
		System.out.println("==========>" + (this instanceof AuthenticationEntryPoint));
		
		if(this instanceof AuthenticationSuccessHandler ) {
			authResponse.put("success", true);
			authResponse.put("messgae", "Login Successful");
			authResponse.put("user", authentication.getName());
			
			List<String> authorities = new ArrayList<String>();
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				authorities.add(authority.getAuthority());
			}
			authResponse.put("authorities", authorities);
		} else if (this instanceof AuthenticationFailureHandler) {
			authResponse.put("success", false);
			authResponse.put("messgae", exception.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
		} else if(this instanceof LogoutSuccessHandler) {
			authResponse.put("success", true);
		} else if (this instanceof AuthenticationEntryPoint) {
			authResponse.put("access", HttpStatus.UNAUTHORIZED.getReasonPhrase());
			responseWrapper.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
		} else {
			authResponse.put("success", false);
		}
		
		PrintWriter writer = responseWrapper.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(writer, authResponse);
		writer.close();
	}
}