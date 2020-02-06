### HandlerMethodArgumentResolver 
* Strategy interface for resolving method parameters into argument values in the context of a given request.
* Example: CsrfTokenArgumentResolver
* Explore: How it works ... other usages
    ```
    //Usage

    @RestController
    public class PublicApis {
        @GetMapping("/csrf")
        public CsrfToken csrf(CsrfToken token) {
            return token;
        }
    }


    ```