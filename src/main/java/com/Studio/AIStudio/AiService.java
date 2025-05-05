package com.Studio.AIStudio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiConfig aiConfig;


    public String getChat(String prompt) {
        return aiConfig.callGeminiApi(prompt);
    }

    public String getRecipe(String request) {
        String response =  aiConfig.callGeminiApi(request);
        return response;
    }

    public String getWorkout(String request) {
        return aiConfig.callGeminiApi(request);
    }

}