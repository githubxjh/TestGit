package org.example.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PictureController {
    @GetMapping("/picture-list")
    public String articleList(){
        return "picture-list";
    }

}
