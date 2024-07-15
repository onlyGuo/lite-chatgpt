package com.guoshengkai.litechatgpt.controller;

import com.guoshengkai.litechatgpt.conf.NoLogin;
import com.guoshengkai.litechatgpt.core.beans.Method;
import com.guoshengkai.litechatgpt.core.cache.CacheUtil;
import com.guoshengkai.litechatgpt.core.cache.Keys;
import com.guoshengkai.litechatgpt.core.sql.where.C;
import com.guoshengkai.litechatgpt.core.util.CodeUtil;
import com.guoshengkai.litechatgpt.core.util.ThreadUtil;
import com.guoshengkai.litechatgpt.dao.UserDao;
import com.guoshengkai.litechatgpt.entity.User;
import com.guoshengkai.litechatgpt.entity.vo.ValidationCodeSendRequest;
import com.guoshengkai.litechatgpt.exception.AccessOAuthException;
import com.guoshengkai.litechatgpt.exception.AccessResourceOAuthException;
import com.guoshengkai.litechatgpt.services.SmsService;
import com.guoshengkai.litechatgpt.util.EmailUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource
    private SmsService smsService;

    @Resource
    private UserDao userDao;

    @PostMapping("/sendVerificationCode")
    @NoLogin
    public void sendVerificationCode(@RequestBody ValidationCodeSendRequest request, HttpSession session) {
        Object validation = session.getAttribute("validation");
        if (validation == null || !(Boolean) validation) {
            throw new AccessResourceOAuthException("No Access Resource");
        }
        String code = CodeUtil.rand(111111, 999999) + "";
        if (request.getType().equals("email")) {
            EmailUtil.sendTemplateEmail("Verification Code",
                    "Thank you for choosing Lite-GPT, your captcha is:" + code +
                    ".<br/>Please use the verification code within 5 minutes.", request.getEmail());
            CacheUtil.pushObject(Keys.keys("verification_code", request.getEmail()), code, 60 * 5);
        } else if (request.getType().equals("mobile")) {
            smsService.sendCodeSms(request.getMobile(), code);
            CacheUtil.pushObject(Keys.keys("verification_code", request.getMobile()), code, 60 * 5);
        } else {
            throw new IllegalArgumentException("Invalid contact type");
        }
    }

    @PostMapping("verify")
    @NoLogin
    public Map<String, Object> verify(@RequestBody ValidationCodeSendRequest request) {
        User user = null;
        if (request.getType().equals("email")) {
            String code = CacheUtil.getObject(Keys.keys("verification_code", request.getEmail()), String.class);
            if (code == null || !code.equals(request.getCode())) {
                throw new IllegalArgumentException("Invalid verification code");
            }
            request.setMobile(null);
            user = userDao.get(Method.where(User::getEmail, C.EQ, request.getEmail()));
        } else if (request.getType().equals("mobile")) {
            String code = CacheUtil.getObject(Keys.keys("verification_code", request.getMobile()), String.class);
            if (code == null || !code.equals(request.getCode())) {
                throw new IllegalArgumentException("Invalid verification code");
            }
            request.setEmail(null);
            user = userDao.get(Method.where(User::getMobile, C.EQ, request.getMobile()));
        } else {
            throw new IllegalArgumentException("Invalid contact type");
        }

        if (user == null) {
            user = new User();
            user.setEmail(request.getEmail());
            user.setMobile(request.getMobile());
            user.setNicker(StringUtils.hasText(request.getEmail()) ? request.getEmail() : request.getMobile());
            // 脱敏nicker
            if (user.getNicker().length() > 10){
                user.setNicker(user.getNicker().substring(0, 3) + "****" + user.getNicker().substring(7));
            } else if (user.getNicker().length() > 7) {
                user.setNicker(user.getNicker().substring(0, 3) + "****" + user.getNicker().substring(5));
            }else if (user.getNicker().length() > 4){
                user.setNicker(user.getNicker().charAt(0) + "***" + user.getNicker().substring(3));
            }else{
                user.setNicker(user.getNicker().charAt(0) + "***");
            }
            userDao.add(user);
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        CacheUtil.pushObject(Keys.keys("TOKEN", token), user.getId(), 60 * 60 * 24 * 7);
        return Map.of("token", token, "user", user);
    }

    @GetMapping
    @NoLogin
    public User userInfo(HttpSession session) {
        session.setAttribute("validation", true);
        if (ThreadUtil.getUserId() == null || ThreadUtil.getUserId() == 0){
            throw new AccessOAuthException("No Access");
        }
        User user = userDao.get(Method.where(User::getId, C.EQ, ThreadUtil.getUserId()));
        if (null == user){
            throw new AccessOAuthException("No Access");
        }
        return user;
    }

}