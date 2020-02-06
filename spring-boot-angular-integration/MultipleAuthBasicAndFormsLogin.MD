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
* If you have different entry paths then works like a charm. Can configure different classes `WebSecurityConfigurerAdapter` classes for each path. Default Order is 100 as of Spring Security 5.x 
* Creates filter chain based on auth type selected. Filter chain gets associated with given path. Exa:
    * forms login for `/api/**` (csrf protection)
    * basic auth for `/rest/**`
        * Should I setup CSRF protection for external API clients? Short answer **NO**. Why? The idea of CSRF protection is to prevent POST from non-sameorigin webpages. CSRF token is known to legitimat client only. By allowing all clients to retrieve the csrf token, defeats the purpose. That's why /csrf end point using CsrfTokenArgumentResolver/@EnableWebSecurity Should not be enabled for CORS (Unauthorized client can also request it - So infact do NOT allow CORS for /csrf)
    * `ApiWebSecurityConfigurationAdapter`
        *  sets up basic auth for `"/rest/**"` path 
        * authorized user also needs `ADMIN` role. Can be `API_USER` also.
        * `@Order(1)` ... smaller number takes higher precedence ... i.e. will handle request earlier.
        * Difference of `http.antMatcher` and  and  `http..authorizeRequests().antMatchers("/rest/**")` ...  
            ```
            http
            .antMatcher("/rest/**")
                .authorizeRequests()
                ....

            vs...
            
            http.
            .authorizeRequests()
                .antMatchers("/*.js" ...
                ...
            ```
        * `http.antMatcher().authorizeRequests() ...` will ensure that security configuration will be applied to matching routes only.
            * Notice the singular *antMatcher* method.
            * Check logs ... *Creating filter chain: Ant* [pattern='/rest/**'],
            * Rules(Auth type) and filter chain will be applied to  only `/rest/**` in this case. Check the *Creating filter chain* line in logs.
            * Non matching will **NOT** error out. SS will try to match other providers.
            * Without this clause non-rest reques also falls through `ApiWebSecurityConfigurationAdapter` and will error out - Because `ApiWebSecurityConfigurationAdapter` is not configured to handle non-rest calls.
        * `http.authorizeRequests()` ... all valid requests will be passed through these rules and filter chain. (If request is not approved / rejected).
            * Example: all requests will fall through this rules and chain.  
            * check logs *Creating filter chain: any request*
        * Check logs ... *Creating filter chain: Ant* [pattern='/rest/**'] ... BasicAuthenticationFilter is configured for this path
    * The `FormLoginWebSecurityConfigurerAdapter` sets up Forms login for browser users.
        * It will handle all non authenticated requets ... using `http.authorizeRequests()`.
        * All static resources (css/js/images/icons) should be accsible by all.
        * login (get login page request) and logout should be accesible by all.
        * **MUST** return JSON response for following cases, because SPA has made an API call and its expecting JSON. Default is some 302 on XHR and app will not know what to do.
            * login success 
            * login failure
                ```
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/api/login")
                    .successHandler(new AppAuthSuccessHandler())
                    .failureHandler(new AppAuthFailureHandler()
                ``` 
            * logout
                * Default Behaviour:
                    * On successful logout, Send 302 with configured logoutsuccessurl (can override default)
                        ```
                        Request URL:http://localhost:8080/logout
                        Request Method:POST
                        Status Code: 302
                        Location: http://localhost:8080/login?logout
                        ```
                    * Invalidates session and deleted JSESSIONID cookie.
                * Incase of SPA, need JSON response and not redirect (HTTP status 302)
                    * Configure logout handler, which will write JSON response. Below configuration will also invalidate session and delete session cookie. 
                    ```
                        .logout()
                            .addLogoutHandler(new AppLogoutHandler())
                    ```

            * unauthorized access - Configure authenticationEntryPoint
                ```
                    .exceptionHandling()
					    .authenticationEntryPoint(new AppAuthExceptionEntryPoint());

                    //OR

			        .and()
					    .exceptionHandling()
						    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                ```
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
						.anyRequest().hasRole("ADMIN ")
                        // or simply .authenticated()
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