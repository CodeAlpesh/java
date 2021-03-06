package com.comp.codename.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController {
 
	@RequestMapping("/{path:[^\\.]+}/**")
    public String forward() {
        return "forward:/";
    }
	
	@GetMapping("/home")
    public String home() {
        return "forward:/";
    }

}

//// Match everything without a suffix (so not a static resource)
//    @RequestMapping(value = "/**/{path:[^.]*}")       
