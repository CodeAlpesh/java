https://spring.io/guides/tutorials/spring-boot-oauth2

### Manually Configure OAuth2 SSO 
So far the oauth2-sso was configured by the "magic" in the @EnableOAuth2Sso annotation. 
Let's manually configuring everything in there to make it explicit.

##### Clients and Authentication
* There are 2 features behind @EnableOAuth2Sso: 
* The **OAuth2 client** 
    * client is re-usable
    * Use it to interact with the OAuth2 resources that your Authorization Server (in this case Facebook) provides in this case (https://developers.facebook.com/docs/graph-api) 
* The **authentication**:
    * Aligns app with the rest of Spring Security. 
    * So once the dance with Facebook is over your app behaves exactly like any other secure Spring app.
	* Principle , Grants etc. will work as is 

* **the OAuth2 client**
    * client piece is provided by Spring Security OAuth2
    * switched on by a different annotation @EnableOAuth2Client
    * Step1 is to remove the @EnableOAuth2Sso and replace with low level annotation @EnableOAuth2Client
    * Step2 inject an OAuth2ClientContext - use it to build an authentication filter that we add to our security configuration
        * OAuth2ClientAuthenticationProcessingFilter
    
    