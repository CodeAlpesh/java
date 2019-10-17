1. Using PostConstruct annotation

@Configuration
public class InitialDataConfiguration {

    @PostConstruct
    public void postConstruct() {
        System.out.println("Started after Spring boot application !");
    }

}

@Component
public class PostConstructExampleBean {
 
    private static final Logger LOG = Logger.getLogger(PostConstructExampleBean.class);
 
    @Autowired
    private Environment environment;
 
    @PostConstruct
    public void init() {
        LOG.info(Arrays.asList(environment.getDefaultProfiles()));
    }
}


2. Using command line runner bean

@Configuration
public class InitialDataConfiguration {

    @Bean
    CommandLineRunner runner() {
        return args -> {
            System.out.println("CommandLineRunner running in the UnsplashApplication class...");
        };
    }
}


public classs SpringBootApplication implements CommandLineRunner{

    @Override
        public void run(String... arg0) throws Exception {
        // write your logic here 

        }
}



3. Using ApplicationReadyEvent

@EventListener 
fun doSomethingAfterStartup(event: ApplicationReadyEvent) {
    System.out.println("hello world, I have just started up");
}

