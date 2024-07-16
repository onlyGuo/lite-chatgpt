package com.guoshengkai.litechatgpt.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.guoshengkai.litechatgpt.exception.ServiceInvokeException;
import com.guoshengkai.litechatgpt.exception.ValidationException;
import com.guoshengkai.litechatgpt.plugin.PluginRegister;
import com.guoshengkai.litechatgpt.plugin.sdk.Plugin;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPTAPI {


    public static String answer(List<Map<String, Object>> message, double temperature, double presencePenalty, boolean free, String model){
//        String sysMessage = "";
//        if (model.contains("gpt-4")){
//            sysMessage += "\nYour version: gpt-4-turbo";
//        }else{
//            sysMessage += "\nYour version: gpt-3.5-turbo";
//        }
//        message.add(0, Map.of("role", "system", "content", sysMessage + "\n\n"));
        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost post = getFreePost(60000);
            HashMap<String, Object> body = new HashMap<>(Map.of(
                    "messages", message,
                    "model", model,
                    "stream", false,
                    "temperature", temperature,
                    "presence_penalty", presencePenalty
            ));
            post.setEntity(new StringEntity(JSON.toJSONString(body),
                    StandardCharsets.UTF_8));
            CloseableHttpResponse execute = client.execute(post);
            String string = EntityUtils.toString(execute.getEntity(), StandardCharsets.UTF_8);
            if (string.startsWith("```json")){
                string = string.substring(7, string.length() - 3);
            }
            return JSON.parseObject(string).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        } catch (Exception e){
            throw new ValidationException("ChatGPT发生错误:" + e.getMessage(), e);
        }
    }

    public static void freeStream(List<Map<String, Object>> message, FreeChatMessageListener listener){
        freeStream(message, List.of(), listener, 0.5, 0, "gpt-3.5-turbo");
    }

    public static void freeStream(List<Map<String, Object>> message, String module, FreeChatMessageListener listener){
        freeStream(message, List.of(), listener, 0.5, 0, module);
    }
    public static void freeStream(List<Map<String, Object>> message, List<Plugin> plugins, FreeChatMessageListener listener,
                                  double temperature, double presencePenalty, String module){
        Plugin runPlugin = null;
        String callFunction = null;
        String callFunctionId = null;
        StringBuilder callFunctionArguments = new StringBuilder();
        String baseUrl = null;
        String accessToken = null;
        try(CloseableHttpClient client = HttpClients.createDefault()){
            String usModel = module;
            HttpPost post = null;
            try {
                post = getFreePost(usModel.startsWith("gpt-4-vision") || usModel.startsWith("gpt-4o") ? 60000 : 30000);
                // https://api.icoding.ink/v1/chat/completions
                // 去掉v1以及后面的
                baseUrl = post.getURI().toString().split("v1")[0];
                accessToken = post.getHeaders("Authorization")[0].getValue();
                if (accessToken.startsWith("Bearer ")){
                    accessToken = accessToken.substring(7);
                }
            }catch (ValidationException e){
                listener.handler(FreeMessage.as(e.getMessage(), false, 0, 0, 0));
                listener.handler(FreeMessage.as("", true, 0, 0, 0));
                return;
            }
            // 设置超时时间
            Map<String, Object> body = new HashMap<>(Map.of(
                    "messages", message,
                    "model", usModel,
                    "stream", true,
                    "temperature", temperature,
                    "presence_penalty", presencePenalty
//                    "stream_options", Map.of("include_usage", true)
            ));

            if (plugins != null && !plugins.isEmpty()){
                List<Map<String, Object>> tools = new LinkedList<>();
                plugins.forEach(plugin -> {
                    tools.add(plugin.toMap());
                });
                body.put("tools", tools);
            }

            if(usModel.startsWith("gpt-4-vision") || usModel.startsWith("gpt-4o")){
                body.put("max_tokens", 4000);
            }
            System.out.println(JSON.toJSONString(body));
            post.setEntity(new StringEntity(JSON.toJSONString(body),
                    StandardCharsets.UTF_8));
            CloseableHttpResponse execute = client.execute(post);
            if (execute.getStatusLine().getStatusCode() != 200){
                String string = EntityUtils.toString(execute.getEntity(), StandardCharsets.UTF_8);
                listener.handler(FreeMessage.as("ChatGPT发生错误:\n```json\n" + string + "\n```\n", false,
                        0, 0, 0));
                listener.handler(FreeMessage.as("", true, 0, 0, 0));
                return;
            }
            HttpEntity entity = execute.getEntity();
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();
            InputStream inputStream = entity.getContent();
            JSONObject finalUsage = null;
            try{
                reader = new BufferedReader(new InputStreamReader(inputStream), 1);
                // 一行一行的读
                String tempLine;
                while ((tempLine = reader.readLine()) != null) {
                    String[] lines = new String[]{tempLine};
                    if (tempLine.trim().contains("\n")){
                        lines = tempLine.split("\n");
                    }
                    for (String line: lines){
                        if (line.startsWith("data:")){
                            line = line.substring(5).trim();
                        }
                        if (line.trim().replace("[DONE]", "").isBlank()){
                            continue;
                        }
                        builder.append(line);
                        JSONObject jsonObject = JSON.parseObject(line);
                        JSONObject usage = jsonObject.getJSONObject("usage");
                        if (null != usage){
                            finalUsage = usage;
                        }
                        JSONArray choices = jsonObject
                                .getJSONArray("choices");
                        if (choices == null || choices.size() == 0){
                            continue;
                        }
                        JSONObject delta = choices.getJSONObject(0)
                                .getJSONObject("delta");
                        if (delta == null){
                            continue;
                        }
                        String string = delta.getString("content");
                        if (null == string){
                            string = "";
                        }
                        if (delta.containsKey("tool_calls") && !delta.getJSONArray("tool_calls").isEmpty()){
                            JSONObject tool = delta.getJSONArray("tool_calls").getJSONObject(0);
                            if (tool.containsKey("function")){
                                JSONObject function = tool.getJSONObject("function");
                                if (function.containsKey("name")){
                                    callFunction = function.getString("name");
                                    callFunctionId = tool.getString("id");
                                    runPlugin = PluginRegister.getPluginByMethodName(callFunction);
                                    if (null == runPlugin){
                                        listener.handler(FreeMessage.as(string, false, 0,
                                                0, 0));
                                        continue;
                                    }
                                    string += "\n[[gpt-fun-call(" + runPlugin.getPluginName() + ")\nrunning\n";

                                }
                                callFunctionArguments.append(function.getString("arguments"));
                            }
                        }
                        if (StringUtils.hasText(string)){
                            listener.handler(FreeMessage.as(string, false, 0,
                                    0, 0));
                        }
                    }
                }
                // 调用插件
                if (callFunction != null){
                    String pluginResult = "";
                    if (null == runPlugin){
                        pluginResult = "ERROR: The Plugin is not register.";
                    }else{
                        message.add(Map.of(
                            "content", "",
                                "role", "assistant",
                                "tool_calls", List.of(Map.of(
                                        "id", callFunctionId,
                                        "type", "function",
                                        "function", Map.of(
                                                "name", runPlugin.getPluginMethodName(),
                                                "arguments", callFunctionArguments
                                        )
                                ))
                        ));
                        try {
                            pluginResult = runPlugin.execute(
                                    JSON.parseObject(callFunctionArguments.toString(), Map.class), baseUrl, accessToken);
                        }catch (Exception e){
                            pluginResult = "ERROR:" + e.getClass().toString() + ":\n" + e.getMessage();
                        }
                    }
                    listener.handler(FreeMessage.as("done]]\n", false, 0,
                            0, 0));
                    if (pluginResult.startsWith("ERROR:")){
                        listener.handler(FreeMessage.as("PluginError: \n```\n" + pluginResult + "\n```\n",
                                false, 0, 0, 0));
                    }else{
                        message.add(Map.of(
                                "content", pluginResult,
                                "role", "tool",
                                "tool_call_id", callFunctionId
                        ));
                        freeStream(message, plugins, listener, temperature, presencePenalty, module);
                    }
                }
            }catch (Exception e){
                if (null != reader){
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                }
                String s = builder.toString();
                s += "\r\n // " + e.getMessage();
                throw new ValidationException("\n```json\n" + s + "\n```", e);
            }finally {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            if (finalUsage != null){
                listener.handler(FreeMessage.as("", true, finalUsage.getIntValue("prompt_tokens"),
                        finalUsage.getIntValue("completion_tokens"), finalUsage.getIntValue("total_tokens")));
            }else{
                listener.handler(FreeMessage.as("", true, 0, 0, 0));
            }
            // 逐字读取
        } catch (Exception e){
            try {
                listener.handler(FreeMessage.as("\nChatGPT发生错误:" + e.getMessage(), true,
                        0, 0, 0));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new ServiceInvokeException("ChatGPT发生错误:" + e.getMessage(), e);
        }
    }


    private static HttpPost getFreePost(int timeout){
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).build();
        HttpPost post = new HttpPost("https://api.icoding.ink/v1/chat/completions");
        post.setHeader("Content-Type", "application/json;charset=utf-8");
        post.setHeader("Authorization", "Bearer " + System.getenv("GPT_TOKEN"));
        post.setConfig(config);
        return post;
    }

    public static void main(String[] args) {
//        for (int i = 0; i < 1; i++){
//            new Thread(() -> {
//       StringBuilder sb = new StringBuilder();
//                freeStream(List.of(
//                        Map.of("role", "user","content", "你好")
//                ), (a) -> {
//                    sb.append(a.getContent());
//                }, 1, 0, true, "gpt-future-beta");
//        System.out.println(sb);

        List<String> imageUrlInContent = getImageUrlInContent("""
                ![
                A girl in a Japanese school uniform (JK).
                ](https://p19-flow-sign-sg.ciciai.com/ocean-cloud-tos-sg/5e240c07e20f447eb582d8e12052dbf9.png~tplv-0es2k971ck-image.png?rk3s=18ea6f23&x-expires=1738510903&x-signature=nAaM0YwkeBapo%2FQPZUMzI%2FGquDY%3D)
                """);
        System.out.println(imageUrlInContent);

    }

    public static List<String> getImageUrlInContent(String content){
        // 获取MD格式中的IMG
        List<String> result = new LinkedList<>();
        String regex = "!\\[.*?\\]\\((.*?)\\)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group = matcher.group();
            String url = matcher.group(1);
            result.add(url);
            content = content.replace(group, "");
        }
        return result;
    }


    /**
     * 画图
     * @param prompt
     *      画图内容
     * @param size
     *      画图大小
     */
    public static String draw(String prompt, int size) {
        return draw(prompt, size + "x" + size);
    }


    public static String draw(String prompt, String size){
        return draw(prompt, size, "standard", "vivid");
    }
    public static String draw(String prompt, String size, String quality, String style) {
        HttpPost post = getFreePost(120000);
        String[] v1s = post.getURI().toString().split("v1");
        String url = v1s[0] + "v1/images/generations";
        post.setURI(URI.create(url));
        post.setEntity(new StringEntity(JSON.toJSONString(Map.of(
                "prompt", prompt,
                "size", size,
                "model", "dall-e-3",
                "n", 1,
                "quality", quality,
                "style", "vivid",
                "response_format", "url"
        )), StandardCharsets.UTF_8));
        CloseableHttpClient client = HttpClients.createDefault();
        try (CloseableHttpResponse execute = client.execute(post)){
            String string = EntityUtils.toString(execute.getEntity(), StandardCharsets.UTF_8);
            try {
                return JSON.parseObject(string).getJSONArray("data").getJSONObject(0).getString("url");
            }catch (Exception e){
                throw new ValidationException("画图失败:" + string);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
