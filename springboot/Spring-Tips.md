### Load configuration as a Type. 
```
@Configuration
class OAuth2ProtectedResourceDetailsConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "security.oauth2.client")
	@Primary
	public AuthorizationCodeResourceDetails oauth2RemoteResource() {
		return new AuthorizationCodeResourceDetails();
	}

}

security:
  oauth2:
    client:
      clientId: 123...
      clientSecret: da...
      accessTokenUri: https://graph.facebook.com/oauth/access_token
      userAuthorizationUri: https://www.facebook.com/dialog/oauth
      tokenName: oauth_token
      authenticationScheme: query
      clientAuthenticationScheme: form
    resource:
      userInfoUri: https://graph.facebook.com/me

```

### Load configuration as a Type (Nested).
```
class ClientResources {

  @NestedConfigurationProperty
  private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

  @NestedConfigurationProperty
  private ResourceServerProperties resource = new ResourceServerProperties();

  public AuthorizationCodeResourceDetails getClient() {
    return client;
  }

  public ResourceServerProperties getResource() {
    return resource;
  }
}

@Bean
@ConfigurationProperties("github")
public ClientResources github() {
  return new ClientResources();
}

@Bean
@ConfigurationProperties("facebook")
public ClientResources facebook() {
  return new ClientResources();
}

```
```
facebook:
  client:
    clientId: 233668646673605
    clientSecret: 33b17e044ee6a4fa383f46ec6e28ea1d
    accessTokenUri: https://graph.facebook.com/oauth/access_token
    userAuthorizationUri: https://www.facebook.com/dialog/oauth
    tokenName: oauth_token
    authenticationScheme: query
    clientAuthenticationScheme: form
  resource:
    userInfoUri: https://graph.facebook.com/me
github:
  client:
    clientId: bd1c0a783ccdd1c9b9e4
    clientSecret: 1a9030fbca47a5b2c28e92f19050bb77824b5ad1
    accessTokenUri: https://github.com/login/oauth/access_token
    userAuthorizationUri: https://github.com/login/oauth/authorize
    clientAuthenticationScheme: form
  resource:
    userInfoUri: https://api.github.com/user

```