package org.example.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ArticleController {
@GetMapping("/article-list")
    public String articleList(){
        return "comment-list";
    }
}
