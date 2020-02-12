package com.example.ui;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@SpringBootApplication
@RestController
@EnableOAuth2Client
@EnableAuthorizationServer
@Order(200)
public class SocialApplication extends WebSecurityConfigurerAdapter  {

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	
    @Value("${encrypted.property}")
    private String property;	

    @Value("${encrypted.property2}")
    private String property2;	
	
	@RequestMapping({ "/user", "/me" })
	public Map<String, String> user(Principal principal) {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("name", principal.getName());
		return map;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	http
	  .antMatcher("/**").authorizeRequests() //1. All requests are protected by default
	  .antMatchers("/", "/login**", "/webjars/**", "/error**").permitAll() //2. The home page and login endpoints are explicitly excluded
	  .anyRequest().authenticated() //3. All other endpoints require an authenticated user
	  .and().exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")) //4. Unauthenticated users are re-directed to the home page
	  .and().logout().logoutSuccessUrl("/").permitAll()
      .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      .and().addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}
	
	@Configuration
	@EnableResourceServer
	protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.
				antMatcher("/me").authorizeRequests().
				anyRequest().authenticated();
		}
	}
	
	public static void main(String[] args) {
		new SpringApplicationBuilder(SocialApplication.class).
		properties("jasypt.encryptor.password=password").run(args);
	}
	
	@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean
	@ConfigurationProperties("facebook")
	public ClientResources facebook() {
		return new ClientResources();
	}
	
	 @Bean
	 @ConfigurationProperties("github")
	 public ClientResources github() {
		 return new ClientResources();
	 }
	 
	private Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();
		filters.add(filter("/login/facebook", facebook()));
		filters.add(filter("/login/github", github()));
		filter.setFilters(filters);
		return filter;
	}
	
	private OAuth2ClientAuthenticationProcessingFilter filter(String url, ClientResources client) {
		OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter(url);
		OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		facebookFilter.setRestTemplate(facebookTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId());
		tokenServices.setRestTemplate(facebookTemplate);
		facebookFilter.setTokenServices(tokenServices);
		return facebookFilter;
	}
		 



	 
	 @EventListener(ApplicationReadyEvent.class)
	 public void getFilters() {
	    FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
	    List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
	    System.out.println("--------------------------------------------------------");
	    list.stream()
	      .flatMap(chain -> chain.getFilters().stream()) 
	      .forEach(filter -> System.out.println(filter.getClass()));
	    System.out.println("---------------->" + property);
	    System.out.println("---------------->" + property2);
	    
	    PooledPBEStringEncryptor encryptor1 = new PooledPBEStringEncryptor();
	    SimpleStringPBEConfig config1 = new SimpleStringPBEConfig();
	    config1.setPassword("password");
	    config1.setAlgorithm("PBEWithMD5AndDES");
	    config1.setKeyObtentionIterations("1000");
	    config1.setPoolSize("1");
	    config1.setProviderName("SunJCE");
	    config1.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
	    config1.setStringOutputType("base64");
	    encryptor1.setConfig(config1);
	    m1(encryptor1, "hello");
	    
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("password");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName(null);
        config.setProviderClassName(null);
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        m1(encryptor, "hello");
        
        StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        enc.setPassword("password");
        System.out.println("=================> " + enc.encrypt("hello"));
        System.out.println("=================> " + enc.decrypt("YID5r+E50vvAMOKoz0iNxw=="));
//        System.out.println("=================> " + enc.decrypt("ENC(YID5r+E50vvAMOKoz0iNxw==)"));
        
	}
// ---- 

	private void m1(PooledPBEStringEncryptor encryptor, String message) {
		String encrypt1 = encryptor.encrypt(message);
	    String encrypt2 = encryptor.encrypt(message);
	    String encrypt3 = encryptor.encrypt(message);
		System.out.println(encrypt1 + ":" + encryptor.decrypt(encrypt1));
		System.out.println(encrypt2 + ":" + encryptor.decrypt(encrypt2));
		System.out.println(encrypt3 + ":" + encryptor.decrypt(encrypt3));
	}
	 
	@Autowired
	@Qualifier("springSecurityFilterChain")
	private Filter springSecurityFilterChain;
		
	@Configuration
	@PropertySource("encrypted.properties")
	public class AppConfigForJasyptStarter {
	}	 
	
	
	 
	
}