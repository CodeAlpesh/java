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
        * For basic auth the `Authorization` header will be sent with each request `Authorization: Basic dXNlcjpwYXNzd29yZA==` BASE64_Encoding(usename:password)
* **Milestone**: 
    * So far we have observed default spring security behaviour.  
    * Adds a custom login form
    * All URL are protected so application will load only after authentication.

## CUSTOM Login form + basic authentication
**Goal** Use application's login form (instead of one provided by spring-security's default login page.)  

**1. Using Basic Authentication** 

* Enable basic auth only using `WebSecurityConfigurerAdapter`
* Login component accepts username/password and submit the form.
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
* The server endpoint: 
```
	@GetMapping("/api/user")
	public Principal user(Principal user) {
		return user;
	}
```
* This is a useful **trick** in a Spring Security application. 
If the `/api/user` resource is reachable then it will return the currently authenticated user (an Authentication), and otherwise Spring Security will intercept the request and send a 401 response through an AuthenticationEntryPoint.
* Authentication details are sent with req. Spring Security will intercept and perform authentication (For Basic auth / token based auth).
    * **TODO**: Implement form based login.
* **Important**: Adding Default HTTP Request Headers: 
    * Home component calls `/api/resource` and Login component calls `/api/user` and browser will get HTTP status 401 and response header `WWW-Authenticate` Exa: `WWW-Authenticate: Basic realm="Realm"`
     and hence browser basic auth popup.
    * To suppress this popup, suppress the header, which is coming from Spring Security. To suppress the reponse header is to send a special, conventional request header `X-Requested-With=XMLHttpRequest`.
    * Write Angular filter (register as HTTP_INTERCEPTORS in App module) to add above header for each Xhr requests.
    * Browser basic auth popup is gone now.   
* **Logout**:
    * Spring securoty will auto configure the logout at `/logout`

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

### Issues with basic auth.
* restricted to username and password authentication
* HTTPS is must ... username password goes as Base64 encoded string
* no protection from [Cross Site Request Forgery](https://en.wikipedia.org/wiki/Cross-site_request_forgery) (CSRF)
    *  As soon as you have a POST, PUT or DELETE in your application it simply isn’t secure any mor
    * Use HttpSession to store and validate token at backend


### Dev Notes:
* For verbose (informative) spring security logs ...  `logging.level.org.springframework.security=DEBUG`
* Explore `spring.security.ignored="*.bundle.*"`
