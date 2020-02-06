package com.comp.codename.demo.config;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

//@Configuration
//public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
//	
//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth
//			.inMemoryAuthentication()
//		    	.withUser("user").password("{noop}password").roles("USER").and()
//		      	.withUser("admin").password("{noop}password").roles("USER", "ADMIN");
//	}
//	
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http
//			.authorizeRequests()
//				.antMatchers("/api/**")
//					.authenticated()
//				.and()
//					.httpBasic()
//			.and()
//				.authorizeRequests()
//					.antMatchers("/*.js", "/index.html", "/", "/home", "/login", "/*.ico")
//						.permitAll()
//						.anyRequest()
//						.authenticated()
//				.and()
//					.formLogin()
//						.loginPage("/login")
//						.loginProcessingUrl("/api/login")
//						.successHandler(new AppAuthSuccessHandler())
//						.failureHandler(new AppAuthFailureHandler())
//				.and()
//					.logout()
//						.addLogoutHandler(new AppLogoutHandler())
//				.and()
//					.exceptionHandling()
//						.authenticationEntryPoint(new AppAuthExceptionEntryPoint())
//				.and()
//					.csrf()
//						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
//				
//	}
//}

class AppAuthSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);

		Writer out = responseWrapper.getWriter();
		Map<String, Object> authResponse = new HashMap<String, Object>();
		authResponse.put("success", true);
		authResponse.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
		List<String> authorities = new ArrayList<String>();
		for (GrantedAuthority auth : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			authorities.add(auth.getAuthority());
		}
		authResponse.put("authorities", authorities);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, authResponse);
		out.close();

	}
}

class AppAuthFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
		Writer out = responseWrapper.getWriter();
		Map<String, Object> authResponse = new HashMap<String, Object>();
		authResponse.put("success", false);
		authResponse.put("reason", exception.getMessage());
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, authResponse);
		out.close();

	}
}

class AppAuthExceptionEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		
		//Use `AuthenticationException` type to set 401 Unauthorized  or 403 Forbidden response status code.
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
		Writer out = responseWrapper.getWriter();
		Map<String, Object> authResponse = new HashMap<String, Object>();
		authResponse.put("success", false);
		authResponse.put("reason", authException.getMessage());
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, authResponse);
		out.close();
	}
}


class AppLogoutHandler implements LogoutHandler {

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
		try {
			Writer out = responseWrapper.getWriter();
			Map<String, Object> authResponse = new HashMap<String, Object>();
			authResponse.put("success", true);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(out, authResponse);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}