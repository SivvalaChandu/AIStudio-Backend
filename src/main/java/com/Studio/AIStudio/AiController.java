package com.Studio.AIStudio;

import com.Studio.AIStudio.DTOs.RecipeRequest;
import com.Studio.AIStudio.DTOs.WorkoutRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/types")
public class AiController {

    private final AiService aiService;
    private final RateLimitService rateLimitService;
    private final TokenService tokenService;

    @PostMapping("/chat")
    public ResponseEntity<?> getResponse(@RequestBody String request) {
        if (rateLimitService.isRateLimited()) {
            return createRateLimitExceededResponse();
        }

        int estimatedTokens = tokenService.estimateTokens(request);
        if (tokenService.wouldExceedLimit(estimatedTokens)) {
            return createTokenLimitExceededResponse();
        }

        try {
            String answer = aiService.getChat("You are a concise assistant: answer only what’s asked—no outros. Organize your response using Markdown headings if needed.   "+request);

            int actualTokens = tokenService.estimateTokens(answer);
            tokenService.trackUsage(estimatedTokens, actualTokens);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @PostMapping("/recipe")
    public ResponseEntity<?> getRecipe(@RequestBody RecipeRequest request) {

        if (rateLimitService.isRateLimited()) {
            return createRateLimitExceededResponse();
        }


        String prompt = String.format(
                "Generate a recipe with these ingredients: %s. Cuisine: %s. Dietary: %s. " +
                        "Format: Name, Ingredients (bulleted), Instructions (numbered). Keep it under 500 tokens.",
                request.ingredients(),
                request.cuisine(),
                request.diet()
        );



        int estimatedTokens = tokenService.estimateTokens(prompt);
        if (tokenService.wouldExceedLimit(estimatedTokens)) {
            return createTokenLimitExceededResponse();
        }

        try {
            String answer = aiService.getRecipe(prompt);


            int actualTokens = tokenService.estimateTokens(answer);
            tokenService.trackUsage(estimatedTokens, actualTokens);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @PostMapping("/workout")
    public ResponseEntity<?> getWorkout(@RequestBody WorkoutRequest request) {

        if (rateLimitService.isRateLimited()) {
            return createRateLimitExceededResponse();
        }


        String prompt = String.format(
                "Create a %d minute %s workout targeting %s. " +
                        "Format: Warmup, Exercises (name, sets, reps), Cooldown. Keep under 500 tokens.",
                request.duration(),
                request.workoutType(),
                String.join(", ", request.muscleGroups())
        );


        int estimatedTokens = tokenService.estimateTokens(prompt);
        if (tokenService.wouldExceedLimit(estimatedTokens)) {
            return createTokenLimitExceededResponse();
        }

        try {
            String answer = aiService.getWorkout(prompt);

            int actualTokens = tokenService.estimateTokens(answer);
            tokenService.trackUsage(estimatedTokens, actualTokens);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> createRateLimitExceededResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Rate limit exceeded");
        response.put("message", "Too many requests. Please try again later.");

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response);
    }

    private ResponseEntity<Map<String, Object>> createTokenLimitExceededResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Token limit exceeded");
        response.put("message", "Token limit for the current minute exceeded. Please try again later.");
        response.put("retryAfter", tokenService.getRetryAfter());

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", tokenService.getRetryAfter())
                .body(response);
    }
}