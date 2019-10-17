package com.example.ui;

import java.security.Principal;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableOAuth2Client
@RestController
public class SocialApplication extends WebSecurityConfigurerAdapter  {

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	@Qualifier("springSecurityFilterChain")
	private Filter springSecurityFilterChain;
	
	public static void main(String[] args) {
		SpringApplication.run(SocialApplication.class, args);
	}
	
	@RequestMapping("/user")
	public Principal user(Principal principal) {
		return principal;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	http
	  .antMatcher("/**")
	  .authorizeRequests()
		.antMatchers("/", "/login**", "/webjars/**", "/error**")
		.permitAll()
	  .anyRequest()
		.authenticated()
	  .and()
	    .logout().logoutSuccessUrl("/").permitAll()
      .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      .and().addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}

	private Filter ssoFilter() {
		OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/facebook");
		OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oauth2ClientContext);
		facebookFilter.setRestTemplate(facebookTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(facebookResource().getUserInfoUri(), facebook().getClientId());
		tokenServices.setRestTemplate(facebookTemplate);
		facebookFilter.setTokenServices(tokenServices);
		return facebookFilter;
	}	
		 
	@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	 @Bean
	 @ConfigurationProperties("facebook.client")
	 public AuthorizationCodeResourceDetails facebook() {
		 return new AuthorizationCodeResourceDetails();
	 }
	  
	 @Bean
	 @ConfigurationProperties("facebook.resource")
	 public ResourceServerProperties facebookResource() {
		 return new ResourceServerProperties();
	 }
	 
	 @EventListener(ApplicationReadyEvent.class)
	 public void getFilters() {
	    FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
	    List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
	    System.out.println("--------------------------------------------------------");
	    list.stream()
	      .flatMap(chain -> chain.getFilters().stream()) 
	      .forEach(filter -> System.out.println(filter.getClass()));
	}
	
}