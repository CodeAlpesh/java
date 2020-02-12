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
    

### Enabling the Authorization Server
* Authorization Server bunch of endpoints for token validation   
* Implemented in Spring OAuth2 as Spring MVC handlers
* just a add the @EnableAuthorizationServer annotation

```
@Configuration
@ConditionalOnClass(EnableAuthorizationServer.class)
@ConditionalOnMissingBean(AuthorizationServerConfigurer.class)
@ConditionalOnBean(AuthorizationServerEndpointsConfiguration.class)
@EnableConfigurationProperties(AuthorizationServerProperties.class)
public class OAuth2AuthorizationServerConfiguration
		extends AuthorizationServerConfigurerAdapter {
		
		
```
#### How to Get an Access Token 
##### Client credentials tokens: grant_type=client_credentials
```
$ curl acme:acmesecret@localhost:8080/oauth/token -d grant_type=client_credentials
{"access_token":"dbe57c81-d59e-4bca-a507-97e3476b69bc","token_type":"bearer","expires_in":43199,"scope":"read write"}
```

##### Token for given user: grant_type=password
* Mainly useful for testing
* Can be appropriate for a native or mobile application, when you have a local user database to store and validate the credentials
* User **user** is created by default in springboot security setup.

```
curl acme:acmesecret@localhost:8080/oauth/token -d grant_type=password -d username=user -d password=3e921e2b-a68b-403e-88d8-1830cad0e4ce
{"access_token":"b183309e-058c-4038-9864-c6a3f077e434","token_type":"bearer","refresh_token":"62ba976c-b172-4c7e-b234-9cd860f05fda","expires_in":43199,"scope":"read write"}
```

##### For Social login: grant grant_type=authorization_code
* need a browser (or a client that behaves like a browser) to handle:
	* redirects, cookies and render the user interfaces from the external providers.
* Need a client application .. check ClientApplication.java
	* MUST NOT be created in the same package (or a sub-package) of the SocialApplication class. 
	* Otherwise, Spring will load some ClientApplication auto-configurations while starting the SocialApplication server, resulting in startup errors. 
* 