package com.sih.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sends a bare visit to "/" to the right place: the dashboard if
 * already logged in (session-based JSP auth), otherwise the login page.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/login";
    }
}
