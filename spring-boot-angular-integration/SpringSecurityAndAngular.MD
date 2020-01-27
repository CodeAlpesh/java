# Spring Security and Angular  
**Source**: https://spring.io/guides/tutorials/spring-security-and-angular-js/

Spring Security, Spring Boot and Angular working together, provide a pleasant and secure user experience. But any meaningful interactions from client will involve:
* A backend server 
* A static content (HTML, CSS and JavaScript etc...)

The backend server can play any or all of a number of roles: 
* Serving static content
* Sometimes (but not so often these days) rendering dynamic HTML
* Authenticating users
* Securing access to protected resources, and (last but not least) 
* Interacting with JavaScript in the browser through HTTP and JSON (sometimes referred to as a REST API).

Spring boot helps to perform above tasks.


## Building Secure Single Page Application with spring boot 
* **1. Create an angular project and package it as webjar**
* **2. Create a spring boot project** 
    * Add dependencies for web and security.
    * Add dependencies for web and security.
    * Add dpenedency for angular app. Spring boot is auto configured to handle resourses in webjars.
    * Building modular spring-boot and angular app is explained [here](https://github.com/CodeAlpesh/java/tree/master/spring-boot-angular-integration/templateapp) 


## Default behaviour:
* Run the app [`mvn spring-boot:run` or `java -jar app.jar`] and navigate to http://localhost:8080 and Browser will ask for form authentication, which is autoconfigured by adding spring-security as dependency.
* Username is "user" and the password is printed in the console logs on startup. Set your own username and password using `spring.security.user.name=alpesh` and  `spring.security.user.password=password` in application.properties file.
* May not experience this behaviour when run angular using `ng start` and access the spring-boot backend with proxy.
* Check section "Default spring security behaviour - Spring Boot Auto Configuration" in [ReadMe.md](https://github.com/CodeAlpesh/java/blob/master/spring-boot-angular-integration/authentication/ReadMe.md)
* **Adding Dynamic Content**
* Create a rest point that returns welcome message and UUID 
* Fetch using http and display in App-component
* Default spring security behaviour
    * Browser will ask for authentication.
    * Browser will send auth token with each request.
        * For form-based authentication JSESSIONID will be shared.
        * For basic auth the `Authorization` header will be sent with each request `Authorization: Basic dXNlcjpwYXNzd29yZA==` BASE64_Encoding(usename:password). 
        * HttpSession can be enabled for basic auth also. No need to send the `Authorization` header with each request. JSESSIONID will do the job.
        * CSRF protection is enabled by default in java autoconfiguration.
        * **Note:** Need to enable `CookieCsrfTokenRepository` implementation for the Cookie-to-Header Token technique.
* **Milestone**: 
    * So far we have observed default spring security behaviour.  
    * Forms login is enabled and a default login (with form) and logout  provided
    * All URL are protected so application will load only after authentication.

## CUSTOM Login form + basic authentication
**Goal** Use application's login form (instead of one provided by spring-security's default login page.)  

**1. Using Basic Authentication** 

* Create LoginComponent which accepts username/password and submit the form.
  ```
  login() {
    this.authService.authenticate(this.credentials, () => {
        this.router.navigateByUrl('/');
    });
    return false;
  }
  ```
* The authenticate() makes a GET to a secured resource "/api/user". It adds the Base64-encoded credentials in the headers, the server does an authentication and accepts a cookie in return.
```
  authenticate(credentials, callback) {

    const headers = new HttpHeaders(credentials ? {
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
    } : {});

    this.http.get('/api/user', {headers : headers}).subscribe(
      response => {
        if (response['name']) {
          this.authenticated = true;
      } else {
          this.authenticated = false;
      }
      return callback && callback();
      }
    );

   }
```
* The AuthService keeps authentication status. Any error should be shared with login component.
* The server - Enable basic auth only using `WebSecurityConfigurerAdapter`
* Create secured rest endpoint: 
```
	@GetMapping("/api/user")
	public Principal user(Principal user) {
		return user;
	}
```
* This is a useful **trick** in a Spring Security application. 
If the `/api/user` resource is reachable then it will return the currently authenticated user (an Authentication), and otherwise Spring Security will intercept the request and send a 401 response through an AuthenticationEntryPoint.
* Authentication details sent as http request header. Spring Security will intercept and perform authentication (For Basic auth).
    * **TODO**: Implement form based login. JWT token ???
* **Important**: Adding Default HTTP Request Headers: 
    * Home component calls `/api/resource` and Login component calls `/api/user` and browser will get HTTP status 401 with response header `WWW-Authenticate` Exa: `WWW-Authenticate: Basic realm="Realm"`
     and hence browser shows basic auth popup.
    * To suppress this popup, suppress the header, which is coming from Spring Security. To suppress the reponse header is to send a special, conventional request header `X-Requested-With=XMLHttpRequest`.
    * Write Angular filter (register as HTTP_INTERCEPTORS in App module) to add above header for each Xhr requests.
    * Browser basic auth popup is gone now.   
* **Logout**:
    * Spring security will auto configure the logout at `/logout`

* **Server config notes**:
    * Enable HTTP basic
    * All resources are protected by default. You have to permit `index.html, resources (js, css, ico etc.)` required to render angular app. Else browser's basic auth popup will appear. The request header `X-Requested-With=XMLHttpRequest` is sent only for ajax requests. Hence all non-xhr requests must be free.
    * Spring boot is auto configured to forward request `/` to `index.html` 
        ```
        @Configuration
        protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
            http
                .httpBasic()
            .and()
                .authorizeRequests()
                    .antMatchers("/*.js", "/index.html", "/", "/home", "/login", "/*.ico").permitAll()
                .anyRequest().authenticated()
            .and().csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
            }
        }
        ```

* **CSRF Protection**
    * Enable CSRF protection using Cookie-to-Header Token protection strategy. It's a common anti-XSRF technique.
        * The application server sends a randomly generated token in a cookie.
        * The client reads the cookie and adds a custom request header with the token in all subsequent requests.
        * The server compares the received request header value (to maintained on server) and rejects the request if the values are missing or don't match.
    * Spring boot: 
        * Enable `csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())`. 
        * `CookieCsrfTokenRepository` is an implementation of the Cookie-to-Header Token technique.
        * Will send cookie `{"XSRF-TOKEN":{"path":"/","value":"824cb949-008d-4bb0-a10c-f6aaf178601f"}}`
        * Adds `CsrfFilter` filter which will check for token in header or request param. Does NOT expect a cookie.
        * Validates all **except** "GET", "HEAD", "TRACE", "OPTIONS"
        * POST, PUT, DELETE etc MUST include request header `X-XSRF-TOKEN` or request param `_csrf` 
        * Check spring classes `CookieCsrfTokenRepository, DefaultRequiresCsrfMatcher, DefaultCsrfToken` 
    * Angular - Has built in support. 
        * will read cookie `XSRF-TOKEN` and sets request header `X-XSRF-TOKEN`  for POST, PUT etc ..
        * `X-XSRF-TOKEN:824cb949-008d-4bb0-a10c-f6aaf178601f`
    * [Reference](https://medium.com/sparkles-blog/angular2-and-spring-a-friend-in-security-need-is-a-friend-against-csrf-indeed-9f83eaa9ca2e)
    * **TODO**: Writeup on what is csrf and how it works. More details [spring-docs](https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html), [BaelDung](https://www.baeldung.com/spring-security-csrf), [YouTube](https://www.youtube.com/watch?v=9inczw6qtpY)
    * Alternative: 
        * GOAL: Protect the log in form in static HTML site.
        * Solution: Obtain a valid CSRF token before the form submission. Configure the `CsrfTokenArgumentResolver`. Expose an [endpoint](https://docs.spring.io/spring-security/site/docs/5.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#mvc-csrf-resolver) e.g. `/csrf` Do not enable CORS for this endpoint.    

### Issues with basic auth. NOT SUre ...
* restricted to username and password authentication
* HTTPS is must ... username password goes as Base64 encoded string
* No builtin protection from [Cross Site Request Forgery](https://en.wikipedia.org/wiki/Cross-site_request_forgery) (CSRF)
    *  As soon as you have a POST, PUT or DELETE in your application it simply isn’t secure any mor
    * Use HttpSession to store and validate token at backend (cookie-2-header)


### Dev Notes:
* For verbose (informative) spring security logs ...  `logging.level.org.springframework.security=DEBUG`
* Explore `spring.security.ignored="*.bundle.*"`
* Debug params `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8998`

## Scalability
Isn’t it Really Bad to use session state in a single-page application?
* For authentication and CSRF protection using HttpSession is definitely good.
* Security has to be stateful. 
    * TCP and SSL are stateful
    * State has to be stored somewhere.
    * Manage yourself (i.e. more code ... and vulnerabilities)
        * Why to reinvent perfectly working wheel?
* **`TODO`:** Is CORS POST (non xhr) blocked??? same origin policy ...???
How do I scale horizontally?
* Sticky sessions, loadbalancer (have n nodes) .. will scale big
    * Routes request to same JVM where session is managed
    * Will work for large number of users
* Share session with nodes
    * Memcache / redis backed session storage
    * Try [Spring Session](https://github.com/spring-projects/spring-session/)
    * Keep session light weight
* What about JWTs?
    * How are you going to provide CSRF protection?
    * Rule of thumb (Rob Winch):
        * If your application or API is going to be accessed by a browser, you need CSRF protection.
        * Using cookies and HTTP session provides proven solution. 
        * Do not have to write/manage/update - non business logic code.


## WHAT NEXT:
* For current setup i.e client app, spring securoty (basic auth + http session + CSRF protection)
    1. Differen user storages (InMemory, JDBC, LDAP etc)
    2. Forms login
    3. Different entry points as [here](https://medium.com/@mind_sFlight/basic-authentication-with-angular-7-and-spring-security-6fd140efc031) and [here](https://leaks.wanari.com/2017/11/28/how-to-make-custom-usernamepasswordauthenticationfilter-with-spring-security/)
    4. Other ways to authenticate / authorise ... Oauth, SAML etc
* Lets explore FormsLogin(2) then entrypoints(3) and then storage integerations(1) then other ways(4)  

### 2. Diffrenet entry points to spring security
* **Source:** https://medium.com/@mind_sFlight/basic-authentication-with-angular-7-and-spring-security-6fd140efc031



## CUSTOM Login form + FormsLogin

### Server Configuration 
```
	http
	    .authorizeRequests()
	  	.antMatchers("/*.js", "/index.html", "/", "/home", "/login", "/*.ico").permitAll()
	    .anyRequest().authenticated()
	    .and()
      .formLogin()
      	.loginPage("/login")                <- path to request Login page 
      	.loginProcessingUrl("/api/login")   <- Path that receives POST
      	.successForwardUrl("/home")
      	.defaultSuccessUrl("/home")
      	.failureUrl("/home")
      .and()
	  .csrf()
	      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
```
```
  authenticate(credentials, callback) {

    let body: HttpParams = new HttpParams();
    body = body.append('username', credentials.username);
    body = body.append('password', credentials.password);

    this.http.post('/api/login', body).subscribe(
      response => {
        console.log(response);
        if (response['name']) {
          this.authenticated = true;
      } else {
          this.authenticated = false;
      }
      return callback && callback();
      }
    );

   }

  login() {
    this.app.authenticate(this.credentials, () => {
        this.router.navigateByUrl('/');
    });
    return false;
  }   
```
### Default Behaviour
* The `UsernamePasswordAuthenticationFilter` is configured by SpringSecurity(SS) to handle http POST at `/login` with parameter names `username` and `password` - all configurable
* On success send HTTP response 302 to loginSuccess URL
* **Issue:**
    * For Ajax Login requests also, SS sends 302 response which is followed AND a new xhr request is sent for new URL). In case of Angular-SB-SS, the server is configured to forward non api requests to index.html
    * Hence the XHR response will have index.html. Client code neither understand it nor use it, hence Angular router does not kick in and no navigation takes place.
    * **Solution:**
    * Configure server to send JSON response in case AJAX login requests. Use `AuthenticationSuccessHandler` and `AuthenticationFailureHandler` to send JSON response.  
    * It can be used to manage conditional redirects/forwards to various URLs.
    * Check this [github repo](https://github.com/camechis/Spring-Security-Ajax/blob/master/org/camechis/AjaxSuccessAuthenticationHandler.java) for Example Implementation. Some devs also check whether resource is commited or not - Check [Baeldung example](https://www.baeldung.com/spring_redirect_after_login)
    * The JSON res example:
    ```
      .formLogin()
      	.loginPage("/login")
      	.loginProcessingUrl("/api/login")
      	.successHandler(new AuthenticationSuccessHandler() {
			
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(
						response);

				Writer out = responseWrapper.getWriter();
				Map<String, Object> authResponse = new HashMap<String, Object>();
				authResponse.put("success", true);
				authResponse.put("user", SecurityContextHolder.getContext()
						.getAuthentication().getName());
				List<String> authorities = new ArrayList<String>();
				for (GrantedAuthority auth : SecurityContextHolder.getContext()
						.getAuthentication().getAuthorities()) {
					authorities.add(auth.getAuthority());
				}
				authResponse.put("authorities", authorities);
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(out, authResponse);
				out.close();	
			
			}
		})    
    ```

* **Issue:**
    * On application start, the client sends api request. User may not have valid session. By default, User will be reditected to login page request url (HTTP 302) and above behaviour will be observed again. Instead server should send JSON response with status code 401.
    * **Solution:**
    * Configure AuthenticationEntryPoint for exception handling, which will set necessary HTTP HEADERs.
    * SS Filter chain has `ExceptionTranslationFilter`, whose job is to handle SpringSecurity exceptions `AccessDeniedException` and `AuthenticationExceptionthrown`. It delegates work to an `authenticationEntryPoint ` handler that should commencethe authentication process if an AuthenticationException is detected. 
    * Use `AuthenticationException` type to set 401 Unauthorized  or 403 Forbidden response status code.
    ```
        http
          ...
          ...
        .and()
      	  .exceptionHandling()
	        .authenticationEntryPoint(new AuthenticationEntryPoint() {				
				@Override
				public void commence(HttpServletRequest request, HttpServletResponse response,
						AuthenticationException authException) throws IOException, ServletException {
					
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
			});
    ```
* Notes
    * Also configure loginProcessing URL as `/api/...` so that:
        * In dev mode when app is run by angular using proxy, login will still continue to work single consistent rule. **MAYBE** configure the proxy to pass `/login` and `/logout` also to server. 
    