package com.guoshengkai.litechatgpt.controller;

import com.guoshengkai.litechatgpt.conf.NoLogin;
import com.guoshengkai.litechatgpt.services.FilesService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/v1/file")
public class FileApiController {

    @Resource
    private FilesService filesService;

    @GetMapping("upload")
    public Map<String, String> getUploadUrl(){
        return Map.of("url", filesService.getUploadUrl());
    }


    @GetMapping("sign")
    @NoLogin
    public Map<String, String> getSignUrl(String fileKey){
        return Map.of("url", filesService.getSignUrl(fileKey));
    }

    @NoLogin
    @GetMapping("display/**")
    public void display(HttpServletResponse response, HttpServletRequest request){
        String[] split = request.getRequestURI().split("/api/v1/file/display/");
        if (split.length != 2){
            return;
        }
        String fileKey = split[1];
        String url = filesService.getSignUrl(fileKey);
        response.setHeader("Location", url);
        response.setStatus(302);
    }
}
