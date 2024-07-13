package com.guoshengkai.litechatgpt.services;

import com.alibaba.fastjson.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SmsService {

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    private Client client;

    public static final String TEMPLATE_APPCODE = "SMS_13190904";

    public static final String SIGN = "智友AI";

    public static final String DEFAULT_APP_NAME = "尊敬的Lite-GPT";

    @SneakyThrows
    @PostConstruct
    private void init(){
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = "dysmsapi.aliyuncs.com";
        client = new Client(config);
    }


    public void sendCodeSms(String phone, String code) {
        sendSms(phone, SIGN, TEMPLATE_APPCODE, Map.of("appname", DEFAULT_APP_NAME, "code", code));
    }

    public void sendSms(String phone, String sign, String template, Map<String, Object> params) {
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(sign)
                .setTemplateCode(template)
                .setTemplateParam(JSON.toJSONString(params));
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.sendSmsWithOptions(sendSmsRequest, new com.aliyun.teautil.models.RuntimeOptions());
        } catch (TeaException error) {
            throw new RuntimeException(error.getMessage());
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 错误 message
            System.out.println();
            throw new RuntimeException(error.getMessage());
        }
    }
}
