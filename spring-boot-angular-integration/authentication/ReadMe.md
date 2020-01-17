# Spring Security with Spring Boot

##  Create a spring boot project.
* Using curl on the command line
    ```
    $ mkdir ui && cd ui
    $ curl https://start.spring.io/starter.tgz -d style=web -d style=security -d name=ui | tar -xzvf -
    ```
* Using Spring Boot CLI
    ```
    $ spring init --dependencies web,security ui/ && cd ui
    ```
* Using the Spring Initializr website
* Using Spring Tool Suite

## Create an angular project boot project.
* using angular cli
* creare npm and ng shell/batch files that point to locally installed node,npm and anglular cli
  
 ## Building Secure Single Page Application with spring boot 
Building modular spring-boot and angular app is explained [here](https://github.com/CodeAlpesh/java/blob/master/spring-boot-angular-integration/templateapp/template-app.pdf) and checkout the  [template](https://github.com/CodeAlpesh/java/tree/master/spring-boot-angular-integration/templateapp)

## Default spring security behaviour - Spring Boot Auto Configuration
* [Defalut Spring Security Behaviour](https://docs.spring.io/spring-security/site/docs/current/reference/html/hello-spring-security.html) Check Section - 8.1.3 Spring Boot Auto Configuration:  
    *  creates a servlet Filter as a bean named springSecurityFilterChain - esponsible for all the security 
        * Protecting the application URLs
        * Validating submitted username and passwords
        * Redirecting to the log in form, and so on ...  within your application
    * Register this filter for every request to servlet container
    * Creates a UserDetailsService bean with a username of `user` and a randomly generated password (BCrypt) - logged to the console. Set your own password using `spring.security.user.name=alpesh` and `spring.security.user.password=password` in `application.properties` file.
* Spring Boot is not configuring much, but it does a lot. A summary of the features follows
    * Require an authenticated user for any interaction with the application
    * Generate a default login form for you
    * Let the user authenticate with form-based authentication
    * Protects the password storage with BCrypt
    * Lets the user log out
    * CSRF attack prevention
    * Session Fixation protection
    * Security Header integration
    * Integrate with the various user management apis Servlet API methods:

