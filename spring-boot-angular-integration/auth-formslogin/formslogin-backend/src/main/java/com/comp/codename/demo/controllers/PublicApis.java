package com.comp.codename.demo.controllers;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicApis {
	
/**
 *  Should I setup CSRF protection for external API clients? 
 *  Short answer **NO**. 
 *  Why? 
 *  The idea of CSRF protection is to prevent POST from non-sameorigin webpages. 
 *  CSRF token is known to legitimat client only. 
 *  By allowing all clients to retrieve the csrf token, defeats the purpose. 
 *  That's why /csrf end point using CsrfTokenArgumentResolver/@EnableWebSecurity Should not be enabled for CORS, 
 *  (Unauthorized client can also request it - So infact do NOT allow CORS for /csrf)
 *  
 *  Ensure spring security is not blocking this path.
 *  
 *  TODO: Why Spring boot is not injecting CsrfToken, if endpoint is having some path param like /api or /rest
 */
	
//	@GetMapping("/csrf")
//    public CsrfToken csrf(CsrfToken token) {
//        return token;
//    }
}
