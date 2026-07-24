package com.sleepdashboard.ai.controller;

import com.sleepdashboard.ai.service.AiAssistantService;
import com.sleepdashboard.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "AI智能助手接口")
@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error(400, "提问内容不能为空");
        }
        try {
            String answer = aiAssistantService.chat(message);
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error(500, "AI助手暂时不可用: " + e.getMessage());
        }
    }
}
