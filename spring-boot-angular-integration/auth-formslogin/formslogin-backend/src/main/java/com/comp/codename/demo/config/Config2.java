package com.comp.codename.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@EnableWebSecurity
public class Config2 {

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	    auth
	    	.inMemoryAuthentication()
	    		.withUser("user").password("{noop}password").roles("USER").and()
	    		.withUser("admin").password("{noop}password").roles("USER", "ADMIN", "API_USER");
	}
	//default password encoder is DelegatingPasswordEncoder
	
	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.antMatcher("/rest/**")
					.authorizeRequests()
					.anyRequest().authenticated()
					//.anyRequest().hasRole("API_USER")
				.and()
					.httpBasic()
				.and()
					.csrf()
						.disable();
		}
	}
	
	@Configuration
	@Order(2)
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
		
        @Override
        protected void configure(HttpSecurity http) throws Exception {
    		http
				.csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
			.and()
				.authorizeRequests()
				.antMatchers("/*.js", "/index.html", "/", "/home", "/login", "/*.ico", "/csrf")
					.permitAll()
				.anyRequest()
					.authenticated()
			.and()
				.formLogin()
					.loginPage("/login")
					.loginProcessingUrl("/api/login")
					.successHandler(new AppAuthSuccessHandler())
					.failureHandler(new AppAuthFailureHandler())
			.and()
				.logout()
//					.invalidateHttpSession(true) //Default behaviour
//					.deleteCookies("JSESSSIONID") //Default behaviour
					.addLogoutHandler(new AppLogoutHandler())
    		.and()
    			.exceptionHandling()
    				.authenticationEntryPoint(new AppAuthExceptionEntryPoint());
//			.and()
//					.exceptionHandling()
//						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        }
	}
}
