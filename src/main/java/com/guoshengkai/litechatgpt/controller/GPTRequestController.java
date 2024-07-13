package com.guoshengkai.litechatgpt.controller;

import com.alibaba.fastjson.JSON;
import com.guoshengkai.litechatgpt.core.beans.Method;
import com.guoshengkai.litechatgpt.core.cache.Keys;
import com.guoshengkai.litechatgpt.core.sql.where.C;
import com.guoshengkai.litechatgpt.core.util.ThreadUtil;
import com.guoshengkai.litechatgpt.core.util.cacheValue.CacheValueUtil;
import com.guoshengkai.litechatgpt.dao.MessageDao;
import com.guoshengkai.litechatgpt.dao.ModelDao;
import com.guoshengkai.litechatgpt.entity.Message;
import com.guoshengkai.litechatgpt.entity.Model;
import com.guoshengkai.litechatgpt.entity.vo.MessageRequest;
import com.guoshengkai.litechatgpt.exception.ValidationException;
import com.guoshengkai.litechatgpt.util.GPTAPI;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/v1/gpt")
public class GPTRequestController {

    @Resource
    private ModelDao modelDao;

    @Resource
    private MessageDao messageDao;

    @PostMapping("message")
    @SneakyThrows
    public void requestMessage(@RequestBody MessageRequest request, HttpServletResponse response) {
        Message userMessage = request.getMessages().get(request.getMessages().size() - 1);
        userMessage.setCreateTime(new Date());
        userMessage.setUserId(ThreadUtil.getUserId());
        userMessage.setRole("user");
        userMessage.setChatId(request.getChatId());
        messageDao.add(userMessage);

        // stream
        response.setContentType("text/event-stream; charset=utf-8");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter writer = response.getWriter();

        List<Map<String, Object>> messages = new ArrayList<>();
        for (Message message : request.getMessages()) {
            messages.add(new HashMap<>(Map.of("role", message.getRole(), "content", message.getContent())));
        }
        Model model = CacheValueUtil.getValue(() ->
                        modelDao.get(Method.where(Model::getName, C.EQ, request.getModel())),
                Keys.keys("MODEL", request.getModel()), TimeUnit.SECONDS, 60);
        StringBuilder result = new StringBuilder();


        if (null == model){
            result.append(getErrorStr("Model not found"));
            writeResponseError(writer, result.toString(), response);
        }else{
            GPTAPI.freeStream(messages, (msg) -> {
                result.append(msg.getContent());
                writeResponse(writer, "data: " + JSON.toJSONString(msg.getContent()) + "\n\n");
                if (msg.isOver()){
                    writeResponse(writer, "data: [DONE]\n\n");

                    Message message = new Message();
                    message.setRole("assistant");
                    message.setContent(result.toString());
                    message.setChatId(request.getChatId());
                    message.setUserId(ThreadUtil.getUserId());
                    message.setCreateTime(new Date());
                    messageDao.add(message);

                }
            }, 0.5, 0, model.getFinalName());
        }

    }

    private void writeResponseError(PrintWriter writer, String result, HttpServletResponse response) throws IOException {
        response.setStatus(400);
        writer.write(result);
        writer.flush();
        writer.close();
    }

    private void writeResponse(PrintWriter writer, String message) throws IOException {
        writer.write(message);
        writer.flush();
    }

    private String getErrorStr(String message) {
        return """
                \nError:
                ```json
                {
                    "error": "gpt_token_manager_error",
                    "message": "%s"
                }
                ```\n
                """.formatted(message);
    }

}
