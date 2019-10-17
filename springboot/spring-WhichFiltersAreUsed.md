org.apache.catalina.core.ApplicationFilterChain@1132d43d
[
ApplicationFilterConfig[name=characterEncodingFilter, filterClass=org.springframework.boot.web.servlet.filter.OrderedCharacterEncodingFilter],
 ApplicationFilterConfig[name=hiddenHttpMethodFilter, filterClass=org.springframework.boot.web.servlet.filter.OrderedHiddenHttpMethodFilter], 
 ApplicationFilterConfig[name=formContentFilter, filterClass=org.springframework.boot.web.servlet.filter.OrderedFormContentFilter], 
 ApplicationFilterConfig[name=requestContextFilter, filterClass=org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter], 
 ApplicationFilterConfig[name=OAuth2ClientContextFilter, filterClass=org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter], 
 ApplicationFilterConfig[name=springSecurityFilterChain, filterClass=org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean$1], 
 ApplicationFilterConfig[name=Tomcat WebSocket (JSR356) Filter, filterClass=org.apache.tomcat.websocket.server.WsFilter], null, null, null
]


class org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter
class org.springframework.security.web.context.SecurityContextPersistenceFilter
class org.springframework.security.web.header.HeaderWriterFilter
class org.springframework.security.web.csrf.CsrfFilter
class org.springframework.security.web.authentication.logout.LogoutFilter
class org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
class org.springframework.security.web.savedrequest.RequestCacheAwareFilter
class org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter
class org.springframework.security.web.authentication.AnonymousAuthenticationFilter
class org.springframework.security.web.session.SessionManagementFilter
class org.springframework.security.web.access.ExceptionTranslationFilter
class org.springframework.security.web.access.intercept.FilterSecurityInterceptor



	@Autowired
	@Qualifier("springSecurityFilterChain")
	private Filter springSecurityFilterChain;
	
	@EventListener(ApplicationReadyEvent.class)
	public void getFilters() {
	    FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
	    List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
	    System.out.println("--------------------------------------------------------");
	    list.stream()
	      .flatMap(chain -> chain.getFilters().stream()) 
	      .forEach(filter -> System.out.println(filter.getClass()));
	}
	
	//Negative number  --> Higher in filter chain.
	//check @Order annotation
	@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}
