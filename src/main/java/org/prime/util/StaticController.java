package org.prime.util;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


//@Controller
public class StaticController {

    //It serves the stm-web in /resources/static/
    //@RequestMapping("/")
    public String index() {
        return "redirect:/index.html";
    }
}
