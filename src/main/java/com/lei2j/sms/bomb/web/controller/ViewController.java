package com.lei2j.sms.bomb.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author leijinjun
 * @date 2021/1/24
 **/
@Controller
public class ViewController {

    @GetMapping("/admin/{path}/{function}")
    public String view(@PathVariable("path") String path,
                       @PathVariable("function")String function) {
        return String.format("admin/%s/%s", path, function);
    }
}
