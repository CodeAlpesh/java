# Spring Security Architecture
[Source](https://spring.io/guides/topicals/spring-security-architecture)  

Application security boils down to:
* Authentication (who are you?) and 
* authorization (what are you allowed to do?)

## Authentication

* ### AuthenticationManager
    It's a strategy interface for authentication is `AuthenticationManager`.
    ```
    public interface AuthenticationManager {

    Authentication authenticate(Authentication authentication)
        throws AuthenticationException;
        // Authentication.authenticated=true|false
        // Authentication can be null
    }
    ```
    1. return an Authentication (authenticated=true|false) if true - input represents a valid principal.
        * AbstractAuthenticationToken implements Authentication
        * UsernamePasswordAuthenticationToken extends AbstractAuthenticationToken
        * RememberMeAuthenticationToken extends AbstractAuthenticationToken 
    2. throw an AuthenticationException for invalid principal
        * Handled bt SS in generic way.
        * The `ExceptionTranslationFilter`, whose job is to handle SpringSecurity exceptions `AccessDeniedException` and `AuthenticationException`. 
        * It further delegates work to an `authenticationEntryPoint ` handler that should commence the authentication process if an AuthenticationException is detected.
            * Ads response headers (status code etc.) and JSON response body. Exa:
            * `BasicAuthenticationEntryPoint` adds `response.addHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");` which forces browser to open username / password dialog.
            * `LoginUrlAuthenticationEntryPoint` by default will redirect (302) to login form requet url.
    3. return null if it can’t decide.
    * `ProviderManager` is concrete implementation of `AuthenticationManager`
        * Has list of AuthenticationProvider(s)
        * Iterates an Authentication request through a list of AuthenticationProvider(s)
        * Until one provides a non-null response
        * on successfully authentication, the earlier authentication exception (if any) is disregarded and the successful authentication will be used
        * If no provider returns a non-null response throw ProviderNotFoundException
        * For AccountStatusException, no further provider will be queried.
        * ProviderManager has an optional parent, which it can consult if all providers return null. If the parent is not available then a null Authentication results in an AuthenticationException

* ### AuthenticationProvider 
    ```
    public interface AuthenticationProvider {

        Authentication authenticate(Authentication authentication)
                throws AuthenticationException;

        boolean supports(Class<?> authentication);

    }
    ```
    * supports() method is really Class<? extends Authentication>
    * Exa: AbstractUserDetailsAuthenticationProvider, AuthenticationProvider, DaoAuthenticationProvider etc.
        ```
        class AbstractUserDetailsAuthenticationProvider implements
		AuthenticationProvider ... {
            ...
            public boolean supports(Class<?> authentication) {
                return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
            }
            ...
        }

        class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
            ...
            @SuppressWarnings("deprecation")
            protected void additionalAuthenticationChecks(UserDetails userDetails,
                UsernamePasswordAuthenticationToken authentication)
                throws AuthenticationException {
                ...
                ...
                String presentedPassword = authentication.getCredentials().toString();

                if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
                    logger.debug("Authentication failed: password does not match stored value");

                    throw new BadCredentialsException(messages.getMessage(
                            "AbstractUserDetailsAuthenticationProvider.badCredentials",
                            "Bad credentials"));
                }
            }
            ...
        }
        ```

* ### Custom UserDetailsService implementation
    * Commonly used helper is `AuthenticationManagerBuilder` in `WebSecurityConfigurerAdapter` which is great for 
        * setting up in-memory, JDBC or LDAP user details, or 
        * adding a custom UserDetailsService
    ```
        @Configuration
        public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        ... // web stuff here

            @Autowired
            public void initialize(AuthenticationManagerBuilder builder, DataSource dataSource) {
                builder.jdbcAuthentication().dataSource(dataSource).withUser("dave")
                .password("secret").roles("USER");
            }

        }   
    ```
    * In WebSecurityConfigurerAdapter, you can set Custom user-detail-service to DaoAuthenticationProvider.
        * AuthProvider (Exa: AbstractUserDetailsAuthenticationProvider) will use user-service to retrieve user-details and perform authentication. Retrieved user is retuned as `org.springframework.security.core.userdetails.UserDetails`
        * Prefer setting DelegatingPasswordEncoder or any app specific PasswordEncoder to AuthProvider. [Example:](https://gist.githubusercontent.com/kverchi/967d5fe9a083f12806bea23da8698cb7/raw/95d7895ff6f38d50f3e23d608c0522b2f4c1b00d/WebSecurityConfig.java) 
        ```
            @Configuration
            @EnableWebSecurity
            public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
                
                @Autowired
                private CustomUserDetailsService userDetailsService;

                @Override
                protected void configure(AuthenticationManagerBuilder auth)
                    throws Exception {
                    auth.authenticationProvider(authenticationProvider());
                }

                @Bean
                public DaoAuthenticationProvider authenticationProvider() {
                    DaoAuthenticationProvider authProvider
                        = new DaoAuthenticationProvider();
                    authProvider.setUserDetailsService(userDetailsService);
                    authProvider.setPasswordEncoder(bCryptPasswordEncoder());
                    return authProvider;
                }
                
                @Bean
                public BCryptPasswordEncoder bCryptPasswordEncoder() {
                    return new BCryptPasswordEncoder();
                }
            
            }
            @Service
            public class CustomUserDetailsService implements UserDetailsService {
            
                @Autowired
                private UserRepository userRepository;
            
                @Override
                public UserDetails loadUserByUsername(String username) {
                    User user = userRepository.findByUsername(username);
                    // checking if user exists and converting into UserDetails
                    //...
                }
            }
        ```
* ### Custom AuthenticationProvider implementation
    Usecase is when we need to use a third-party authentication system and password isn’t stored in our database or other storage.  
    **Solution**: Use AuthenticationManager which selects a necessary AuthenticationProvider implementation
    ```
    @Configuration
    @EnableWebSecurity
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        
        @Bean("authenticationManager")
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    
        /// ....
    }

    @Service
    public class UserServiceImpl implements UserService {
        
        @Autowired
        AuthenticationManager authenticationManager; 
        
        @Override
        public ServiceResponse login(User requestUser) {
            UsernamePasswordAuthenticationToken authenticationTokenRequest = new
                    UsernamePasswordAuthenticationToken(requestUser.getUsername(), requestUser.getPassword());
            try {
                Authentication authentication = this.authenticationManager.authenticate(authenticationTokenRequest);
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authentication);

                User user = (User) authentication.getPrincipal();
                //...

            } catch (BadCredentialsException ex) {
                // handle User not found exception
                //...
            }
        }
    }

    ```
    1. expose the AuthenticationManager
    1. Use AuthenticationManager bean in login method of UserService



