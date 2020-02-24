# Protecting the Resource Server
## Goal: 
* Single page application with forms authentication.
* Resourse server (microService) that understands authenticated usesrs.

## How?
* Store HttpSession on distributed service like ralational database, redis, hazlecast etc.
* Microservices use Header-To-Session strategy to retrieve session from distributed services.
* Enable CORS on Microservices (allow custom headers)
* Send session id as a HTTP request header `X-Auth-Header` to microservice(s) APIs
    * JSESSIONID cookie is http-only (js can not read it - thats how it should be)
    * On main server build a rest api to return JSESSIONID. 


## Step1:
* UI/Main App:
    * Angular + Spring boot  
        * Spring security forms authentication backed by HttpSession
* Resource server (Microservice)
    * No spring security
    * CORS enabled 
        * Add `@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge=30)` on controller method.
        * Can be configured globally or at controller (all methods) or for one of the controller method.

## Step2:
* Secure Resource server apis (microservice).
* On ui/main Application:
    * Store http session in distributed datasource 
    * Use Spring-session
        ```
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>io.lettuce</groupId>
            <artifactId>lettuce-core</artifactId> 
        </dependency>
        ```
    * Why is Lettuce the default Redis client used in Spring Session Redis? [#789](https://github.com/spring-projects/spring-session/issues/789)
        * Can use Jedis as an alternative - But seems lettuce is betterchoice. 
    * Rest endpoint that returns sessionid for authenticated users.
        * Client/ui will send sessionid as header `X-Auth-Token` to resource server, uses to retrieve session from redis/jdbc.
        * JSESSIONID is (and should be) http-only cookie and will not be read by JS.
        ```
        @GetMapping("/token")
        public Map<String, String> token(HttpSession session) {
            return Collections.singletonMap("token", session.getId());
        }

        export class HomeComponent implements OnInit {

            title = 'Demo';
            greeting:T = { id: '', content: ''};

            constructor(private app: AuthService, private http: HttpClient) {
                http.get('token').subscribe(data => {
                    const token = data['token'];
                    http.get('http://localhost:9000/api/resource', {headers : new HttpHeaders().set('X-Auth-Token', token)})
                    .subscribe((data:T) => this.greeting = data)
                });
            }

            ...
        ```
* On Resource Server - Microservice
    * Same maven dependencies
    * Configure for Http header backed session using `HeaderHttpSessionIdResolver(HEADER_X_AUTH_TOKEN)`  
    ```
        //Application configufation class
        @Bean
        public HttpSessionIdResolver httpSessionStrategy() {
            return HeaderHttpSessionIdResolver.xAuthToken();
        } 
    ```
    * Enable CORS preflight 
    ```
        @Configuration
        public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                    .cors()
                .and()
                    .authorizeRequests()
                        .anyRequest().authenticated();
            }
            
        }
    ```
    * Allow additional headers for CORS 
    ```
        @CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders={"x-auth-token", "x-requested-with", "x-xsrf-token"})
    ```            
    * `security.sessions=NEVER` never create a session, but use one if it is there 
    * Notes:
        * Check `SessionRepositoryFilter` ... Defaults to `private HttpSessionIdResolver httpSessionIdResolver = new CookieHttpSessionIdResolver();`

**TODO**:
* Cusotm token authentication using `AbstractPreAuthenticatedProcessingFilter` and `TokenService`. Example is [here](https://github.com/cer/event-sourcing-examples/blob/master/java-spring/common-auth/src/main/java/net/chrisrichardson/eventstore/javaexamples/banking/commonauth/AuthConfiguration.java).
* Spring Session at [here](https://github.com/hantsy/spring-microservice-sample/blob/master/auth-service/src/main/java/com/hantsylabs/sample/springmicroservice/auth/AuthServiceApplication.java)
* How to configure:
    * Session timeout
    * Session events like ... session expiry etc.
* Default serialization is binary .. can be stored as JSON