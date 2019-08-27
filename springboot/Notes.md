* Bean scopes
* DI
* Field Injection vs  setter injection vs const injection.

#### Propertes
* properties file
	```
	@Value(${app.name})
	String name;
	```
* Using ${propname}, exisitng properties can be reffered by other properties within application.properties
* Override value from commandline ... --spring.port=9000 --spring.profile.active=production --app.name="Hello"
* Type-safe/POJO Configuration Properties
	```
	@Component
	@ConfigurationProperties("basic")
	public class BasicConfiguration {
		private boolean value;
		private String message;
		private String appName;
		private String adminEmail;
		private int number;
		
		//getters and setters
	}
	
	//Older version may need @EnableConfigurationProperties on application class.
	```
	* application.properties
	```
	basic.value: true
	basic.message: Dynamic Message
	basic.app-name: CoolApp
	basic.adinEmail: a@a.com
	basic.number: 100	
	```
#### Profiles
* Environment specific configurations. 
	* application.properties -> Specify active profile name
		* spring.profile.active=development 
		* Can override / specify from commandline => --spring.profile.active=production 
	* application.development.properties for env/profile specific other properties
* Environment specific beans
	```
	@Configuration()
	public class DataSourceConfig {
		
		@Autowired
		private DataSourceProperties dataSourceProperties;
		
		@Profile("development")
		@Bean(name= "datasource")
		public DataSource development() {
			// Config properties will load env. specific properties file. and set the bean accordingly.
			return new DataSource(dataSourceProperties.getUrl(), dataSourceProperties.getUserName(), dataSourceProperties.getPassword());
		}
		
		@Profile("production")
		@Bean(name= "datasource")
		public DataSource production() {
			// Config properties will load env. specific properties file. and set the bean accordingly.
			return new DataSource(dataSourceProperties.getUrl(), dataSourceProperties.getUserName(), dataSourceProperties.getPassword());
		}
		
	}
	
	@ConfigurationProperties(prefix = "app.datasource")
	public class DataSourceProperties {

		private String url;
		private String userName;
		private String password;
	
		//geters / seters
	}
	
	//application-development.properties
	//application-production.properties
	app.datasource.url=devel db
	app.datasource.userName=dev admin
	app.datasource.password=dev password
	
	//For multiple env specific beans abbotate @Profile("development") at calss level. 
	```

#### Auto Configuration
* Simplicity results from auto configuration based dependecies.
	* When hsqldb -> datasource will be autoconfigured
	* When spring web mvc -> dispatch servlet is auto configured.
* Can write your own  configurations also.
##### What is It?	
* Where to find?
** spring.factories 
** @Conditional ... when to load this factory class.
** run --debug to see report on autoconfiguration
** Its not magic has method to madness
** Can exclude Autoconfiguration class and do it manually as per your usecase.
** Check AutoConfigurationReport.txt


#### Web Applications
* MVC - Model View Controller
* Other ones:
** Grails (its spring MVC in desguise)
* **Static Content**
	* CSS / JS / Images / Webjars
	* use /static folder.
	* create /public folder (Spring initializer does not create it.)
	* Packaging as jar or war?
	* If Jar - Do not use webapp folder ..?
	* Check 5_2_StaticContent.png
	* Webjars
		* Add dependency for client framework webjars like jQuery, bootstrap etc.
		* refer them in index.html / templates as "/webjars/..path
		* Spring will down load those webjars and map it to /webjars URL
	* Alternative: Use client dependency tools like bower.
		* Use bower to manage dependencies required for frontend.
		* .bowerrc => specify where to download file (/static/lib)
		* bower.json => list dependencies 
	* Manually add libraries liek jqury to /static or /public folder.
* **Dynamic HTML content**
	* Template engines
		* FreeMarker, Groovy, Thymeleaf, Velocity, Mustache, GSP ...
		* Avoid JSP ... in embedded servlet container
		* Templates will be pickedup from resources/templates
	* Thymeleaf
	* GSP - Groovy Server Pages
		* Prevent XSS - https://www.owasp.org/index.php/Double_Encoding
		* SQL Injection
		* Path Travelrsal -> ../../
		* Execute commands
	* JSP - Should be avoided. Has some limitations with Embedded Servlet Containers
* I18N
	* Localication: View can be adapted to different locales
		* date format
		* Text etc.
	* proeprties file for each locale.
		* message.proeprties, message_fr.proeprties, message_es.proeprties ... 
		* Key = Value pair
	* Thymeleaf will automatically process using #{}
	* Specify non-english locale as default locale, for testign using spring application.proeprties
		* spring.mvc.locale=fr
		
#### Error Handling
* Thymeleaf (will look for error.html)
	* /static/template/error.html
* @Controller that implement ErrorController
* can customize 404 or any specic HTTP status code.
```	
	  @RequestMapping("404")
	  public m1() { //set model and return specific view }
	
	  // Then customize EmbeddedServletContainerCustomizer .. add ErrorPage for specific error code.
```	
#### Exception Handling
* @ExceptionHandler annotation per controller (may be base controller)
	* Set model and forward to error view
	* Check 5_7_1_ExceptionHandling_ExceptionHandler.PNG
* Global Error handler using @ControllerAdvise
	* Add method for each type of exception (possibly application exception).
	* Each controller will throw one of them and will be handled in one of the handler in this class.
	* App Exceptions to have standard params like message, message code etc.
	* Check 5_7_2_ExceptionHandling_ControllerAdvise+ExceptionHandler.PNG
	* **Preferred**

	
#### Spring Data
* Enable H2 console using 
	* spring.h2.console.enabled=true
	* spring.h2.console.path=/console
* JPA crud repositories
* JAP entities
** Relationship - 1:1, 1:n, n:1 etc.
* On app start add soem datat to in-memory H2
** application.properties ... 
*** spring.datasource.platform=h2
*** create data-h2.sql  
*** Spring will run sqls in this file.