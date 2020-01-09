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

Building modular spring-boot and angular app is explained [here](https://github.com/CodeAlpesh/java/tree/master/spring-boot-angular-integration/templateapp) 

### 1 Create an angular project and package it as webjar
### 2 Create a spring boot project 
* Add dependencies fro web and security.
* Add dpenedency for angular app. Spring boot is auto configured to handle resourses in webjars.

### Default behaviour:
* Run the app [`mvn spring-boot:run` or `java -jar app.jar`] and navigate to http://localhost:8080 and Browser will ask for form authentication, which is autoconfigured by adding spring-security as dependency.
* Username is "user" and the password is printed in the console logs on startup. Set your own password using `security.user.password=password` in application.properties file.
* May not experience this behaviour when run angular using `ng start` and access the spring-boot backend with proxy.


### Adding Dynamic Content

