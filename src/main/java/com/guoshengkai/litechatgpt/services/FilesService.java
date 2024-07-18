package com.guoshengkai.litechatgpt.services;

import com.guoshengkai.litechatgpt.entity.Files;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.List;

public interface FilesService {

    String getSignUrl(String fileKey);


    String getSignUrl(String fileKey, int size, int zip);

    void use(String fileKey);

    void unUse(String fileKey);

    String getUploadUrl();

    void delete(String fileKey);

    List<Files> listUnUse(Date date);

    void download(String fileKey, HttpServletResponse response);

    Files uploadByUrl(String url);

    Files upload(byte[] bytes);

    String getTempBytesUrl(byte[] bytes);

    String getTempBytesUrl(byte[] bytes, String contentType);

    Object getSignUrl(String url, String contentType);
}
