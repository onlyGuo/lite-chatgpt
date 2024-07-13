package com.guoshengkai.litechatgpt.util;

import com.aliyun.oss.HttpMethod;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmailUtil {

    private final static String AccessKeyId = "<accessKeyId>";
    private final static String AccessKeySecret = "<accessKeySecret>";

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    public static void sendEmail(String title, String content, String toEmail) {
        Map<String, Object> params = new TreeMap<String, Object>();
        params.put("AccessKeyId", AccessKeyId);
        params.put("Action", "SingleSendMail");
        params.put("Format", "JSON");
        params.put("RegionId", "cn-hangzhou");
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("SignatureVersion", "1.0");
        params.put("Timestamp", getUTCTimeStr());
        params.put("Version", "2015-11-23");

        params.put("AccountName", "system@icoding.ink");
        params.put("FromAlias", "Lite-GPT");
        params.put("AddressType", "1");
        params.put("HtmlBody", content);
        params.put("ReplyToAddress", true);
        params.put("Subject", title);
        params.put("TagName", "Lite-GPT");
        params.put("ToAddress", toEmail);

        Long start = System.currentTimeMillis();
        httpRequestSendEmail(params);
        System.out.println("耗时 ： " + (System.currentTimeMillis() - start));
    }

    public static void sendTemplateEmail(String title, String content, String toEmail) {
        sendEmail(title, """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width,initial-scale=1.0,user-scalable=0">
                    <title>Lite-GPT</title>
                </head>
                <body>
                <div style="
                                            max-width: 600px;
                                            background-color: white;
                                            border: #8c56fc solid 1px;
                                            border-radius: 10px;
                                            margin: 0 auto;
                                            overflow: hidden;
                                ">
                    <h1 style="color: white; margin-top: 0; border-bottom: #d5beff solid 1px; padding: 10px; background-color: #7a4cf2">
                        <span style="vertical-align: middle; font-size: 25px; color: white">Lite-GPT</span>
                    </h1>
                    <p style="color: #6c6c6c; padding: 10px">
                        {{content}}
                    </p>
                    <p style="text-align: center; color: #6c6c6c; border-top: #d5beff solid 1px; padding-top: 10px; padding-bottom: 10px; margin-top: 40px; margin-bottom: 0;font-size: 12px; background-color: #7a4cf2">
                        <a href="https://lite.icoding.ink" style="color: white; margin: 0 20px">Home</a>
                        <a href="https://lite.icoding.ink/#/bbs" style="color: white; margin: 0 20px">BBS</a>
                        <a href="https://github.com/onlyGuo/lite-chatgpt" style="color: white; margin: 0 20px">GitHub</a>
                    </p>
                </div>
                </body>
                </html>
                """.replace("{{content}}", content), toEmail);
    }

    public static String httpRequestSendEmail(Map<String, Object> params) {
        String result = null;
        try {
            params.put("Signature", getSignature(prepareParamStrURLEncoder(params), HttpMethod.POST));
            String param = prepareParamStrURLEncoder(params);
            String url = "https://dm.aliyuncs.com/?" + param;

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = null;
            HttpPost request = new HttpPost(url);
            response = httpClient.execute(request);
            System.out.println(response);
            if (null != response){
                result = EntityUtils.toString(response.getEntity());
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    public static String prepareParamStrURLEncoder(Map<String, Object> params) {
        try {
            StringBuffer param = new StringBuffer();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (StringUtils.isBlank(entry.getKey()) || null == entry.getValue()) {
                    continue;
                }
                param.append(getUtf8Encoder(entry.getKey()) + "=" + getUtf8Encoder(entry.getValue().toString()) + "&");

            }
            return param.substring(0, param.lastIndexOf("&"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取签名
     *
     * @param param
     * @param method
     * @return
     * @throws Exception
     */
    private static String getSignature(String param, HttpMethod method) throws Exception {
        String toSign = method + "&" + URLEncoder.encode("/", "utf8") + "&"
                + getUtf8Encoder(param);
        byte[] bytes = HmacSHA1Encrypt(toSign, AccessKeySecret + "&");
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String getUtf8Encoder(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "utf8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\*", "%2A")
                .replaceAll("%7E", "~");
    }


    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = encryptText.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }

    private static DateFormat daetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm"<br />
     * 如果获取失败，返回null
     *
     * @return
     */
    public static String getUTCTimeStr() {
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        String date = daetFormat.format(cal.getTime());
        System.out.println("时间------" + date);
        String[] strs = date.split(" ");
        return strs[0] + "T" + strs[1] + "Z";
    }


    public static void sendEmailSmtp(String title, String content, String to) {
        // 设置发件人邮箱
        String fromAddress="719348277@qq.com";
        // 设置SMTP服务器地址
        String smtpServer="smtp.qq.com";
        // 设置发件人邮箱的帐号和密码
        String account="719348277";
        String password="<password>";

        // 设置邮件属性
        Properties props=new Properties();
        props.put("mail.smtp.host",smtpServer);
        props.put("mail.smtp.port","465"); // QQ 邮箱需要加上 SSL 认证
        props.put("mail.smtp.auth","true");
        props.put("mail.transport.protocol","smtp");
        props.put("mail.smtp.ssl.enable","true");
        // 设置发送邮件的会话
        Session session = Session.getInstance(props,new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(account,password);
            }
        });

        try{
            // 创建邮件对象
            MimeMessage message=new MimeMessage(session);
            // 设置发件人
            message.setFrom(new InternetAddress(fromAddress, "My ChatGPT"));
            // 设置发件人昵称
            // 设置收件人
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            // 设置邮件主题
            message.setSubject(title);
            // 设置邮件内容
            message.setContent(content, "text/html;charset=utf-8");
            // 发送邮件
            Transport.send(message);
            System.out.println("邮件发送成功");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    static int a = 0;
    public static void main(String[] args) throws InterruptedException {
        String[] emails = {"719348277@qq.com"};

        for (String email : emails) {
            sendEmail("高考加油！", """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8" />
                        <meta name="viewport" content="width=device-width,initial-scale=1.0,user-scalable=0">
                        <title>Lite-GPT</title>
                    </head>
                    <body>
                    <div style="
                                                max-width: 600px;
                                                background-color: white;
                                                border: #8c56fc solid 1px;
                                                border-radius: 10px;
                                                margin: 0 auto;
                                                overflow: hidden;
                                    ">
                        <h1 style="color: white; margin-top: 0; border-bottom: #d5beff solid 1px; padding: 10px; background-color: #7a4cf2">
                            <span style="vertical-align: middle; font-size: 25px; color: white">Lite-GPT</span>
                        </h1>
                        <p style="color: #6c6c6c; padding: 10px">
                            没有什么比努力更重要，每一份努力都不会被辜负。你的未来由你自己决定，你的付出一定会有回报，坚持到底，成功就在前方。加油！
                        </p>
                        <p style="text-align: center; color: #6c6c6c; border-top: #d5beff solid 1px; padding-top: 10px; padding-bottom: 10px; margin-top: 40px; margin-bottom: 0;font-size: 12px; background-color: #7a4cf2">
                            <a href="https://lite.icoding.ink" style="color: white; margin: 0 20px">Home</a>
                            <a href="https://lite.icoding.ink/#/bbs" style="color: white; margin: 0 20px">BBS</a>
                            <a href="https://github.com/onlyGuo/lite-chatgpt" style="color: white; margin: 0 20px">GitHub</a>
                        </p>
                    </div>
                    </body>
                    </html>
                                
                """, email);
        }
    }

}