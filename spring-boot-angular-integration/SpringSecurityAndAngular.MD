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

### 1 Create an angular project and package it as webjar
### 2 Create a spring boot project 
* Add dependencies for web and security.
* Add dpenedency for angular app. Spring boot is auto configured to handle resourses in webjars.
* Building modular spring-boot and angular app is explained [here](https://github.com/CodeAlpesh/java/tree/master/spring-boot-angular-integration/templateapp) 


### Default behaviour:
* Run the app [`mvn spring-boot:run` or `java -jar app.jar`] and navigate to http://localhost:8080 and Browser will ask for form authentication, which is autoconfigured by adding spring-security as dependency.
* Username is "user" and the password is printed in the console logs on startup. Set your own password using `security.user.password=password` in application.properties file.
* May not experience this behaviour when run angular using `ng start` and access the spring-boot backend with proxy.
* Check section "Default spring security behaviour - Spring Boot Auto Configuration" in ReadMe.md


### Adding Dynamic Content
* Create a rest point that returns welcome message and UUID 
* Fetch using http and display in App-component
* Default spring security behaviour
    * Browser will ask for authentication.
    * Browser will send auth token with each request.
        * For form-based authentication JSESSIONID will be shared.
        * For basic auth the `Authorization` header will be sent with each request 
`Authorization: Basic dXNlcjpwYXNzd29yZA==` BASE64_Encoding(usename:password)

So far we have observed default behaviour. Now:
* Add a custom login form, give the user some control over whether to authenticate or not
* 

### Issues with basic auth.
* restricted to username and password authentication
* no protection from [Cross Site Request Forgery](https://en.wikipedia.org/wiki/Cross-site_request_forgery) (CSRF)
    *  As soon as you have a POST, PUT or DELETE in your application it simply isn’t secure any mor
    * Use HttpSession to store and validate token at backend


