package com.comp.code.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController {
 
	@RequestMapping("/{path:[^\\.]+}/**")
    public String forward() {
        return "forward:/";
    }

}

//// Match everything without a suffix (so not a static resource)
//    @RequestMapping(value = "/**/{path:[^.]*}")       
