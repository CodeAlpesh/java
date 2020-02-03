```
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/**")
                    .authenticated()
                .and()
                    .httpBasic()
            .and()
                .authorizeRequests()
                    .antMatchers("/*.js", "/index.html", "/", "/home", "/login", "/*.ico")
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
                        .exceptionHandling()
                            .authenticationEntryPoint(new AppAuthExceptionEntryPoint())
                    .and()
                        .csrf()
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
                
    }
}
```
* This will configure single filterchain ...












## Multiple config classes for different entry points
* If you have different entry paths then works like a charm. Can configure different classes `WebSecurityConfigurerAdapter` classes. Default Order is 100 as of Spring Security 5.x 
* Creates filter chain based on auth type selected. Filter chain gets associated with given path. Exa:
    * forms login for `/api/**` (csrf protection)
    * basic auth for `/rest/**`
        * Can setup CSRF protection. CLient needs to retrieve it  
```
@EnableWebSecurity
public class Config2 {

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	    auth
	    	.inMemoryAuthentication()
	    		.withUser("user").password("{noop}password").roles("USER").and()
	    		.withUser("admin").password("{noop}password").roles("USER", "ADMIN");
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
						.anyRequest().hasRole("ADMIN")
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
				.antMatchers("/*.js", "/index.html", "/", "/home", "/login", "/*.ico")
					.permitAll()
				.anyRequest()
					.authenticated()
			.and()
				.formLogin()
					.loginPage("/login")
					.loginProcessingUrl("/api/login")
					.successHandler(new AppAuthSuccessHandler())
					.failureHandler(new AppAuthFailureHandler())
//			.and()
//				.logout()
//					.addLogoutHandler(new AppLogoutHandler())
			.and()
					.exceptionHandling()
						.authenticationEntryPoint(new AppAuthExceptionEntryPoint());
        }
	}
}

//Same API's can be exposed by different paths.
@RestController
@RequestMapping(path = {"/api", "/rest"})
public class GreetingsController {

	@GetMapping("/resource")
	public Greeting greet() {
		return new Greeting("Hello Alpesh");
	}

	@PostMapping("/user")
	public Principal user(Principal user) {
		return user;
	}
	
}
```
* logs
```
Eagerly initializing {config2=com.comp.codename.demo.config.Config2$$EnhancerBySpringCGLIB$$7fc3b8c@1f77b5cc}

Adding web access control expression 'hasRole('ROLE_ADMIN')', for any request
Validated configuration attributes
Validated configuration attributes
Creating filter chain: Ant [pattern='/rest/**'], 
[
	org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@2630dbc4,
	org.springframework.security.web.context.SecurityContextPersistenceFilter@bc042d5,
	org.springframework.security.web.header.HeaderWriterFilter@6b1dc20f,
	org.springframework.security.web.authentication.logout.LogoutFilter@7640a5b1,
	org.springframework.security.web.authentication.www.BasicAuthenticationFilter@4c58255,
	org.springframework.security.web.savedrequest.RequestCacheAwareFilter@37df14d1,
	org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@443effcb,
	org.springframework.security.web.authentication.AnonymousAuthenticationFilter@5ea4300e,
	org.springframework.security.web.session.SessionManagementFilter@6540cf1d,
	org.springframework.security.web.access.ExceptionTranslationFilter@36c2b646,
	org.springframework.security.web.access.intercept.FilterSecurityInterceptor@4f89331f
]

Adding web access control expression 'permitAll', for Ant [pattern='/*.js']
Adding web access control expression 'permitAll', for Ant [pattern='/index.html']
Adding web access control expression 'permitAll', for Ant [pattern='/']
Adding web access control expression 'permitAll', for Ant [pattern='/home']
Adding web access control expression 'permitAll', for Ant [pattern='/login']
Adding web access control expression 'permitAll', for Ant [pattern='/*.ico']
Adding web access control expression 'authenticated', for any request
Validated configuration attributes
Validated configuration attributes
Creating filter chain: any request, 
[
	org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@7e53339,
	org.springframework.security.web.context.SecurityContextPersistenceFilter@4821aa9f,
	org.springframework.security.web.header.HeaderWriterFilter@48b4a043,
	org.springframework.security.web.csrf.CsrfFilter@f1d0004,
	org.springframework.security.web.authentication.logout.LogoutFilter@4996c99,
	org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter@21505815,
	org.springframework.security.web.savedrequest.RequestCacheAwareFilter@5f481b73,
	org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@32130e61,
	org.springframework.security.web.authentication.AnonymousAuthenticationFilter@58fef7f7,
	org.springframework.security.web.session.SessionManagementFilter@2f5b8250,
	org.springframework.security.web.access.ExceptionTranslationFilter@6e4f263e,
	org.springframework.security.web.access.intercept.FilterSecurityInterceptor@6cf0a747
]
Initializing ExecutorService 'applicationTaskExecutor'
```