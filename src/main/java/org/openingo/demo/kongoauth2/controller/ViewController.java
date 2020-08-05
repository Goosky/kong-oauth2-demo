package org.openingo.demo.kongoauth2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * ViewController
 *
 * @author Qicz
 */
@RestController
public class ViewController {

    @GetMapping("/")
    public ModelAndView index() {
        return indexView();
    }

    @GetMapping("/login.html")
    public ModelAndView loginView() {
        return new ModelAndView("login.html");
    }

    @GetMapping("/index.html")
    public ModelAndView indexView() {
        return new ModelAndView("index.html");
    }

    @GetMapping("/error0.html")
    public ModelAndView errorView() {
        return new ModelAndView("error0.html");
    }

}
