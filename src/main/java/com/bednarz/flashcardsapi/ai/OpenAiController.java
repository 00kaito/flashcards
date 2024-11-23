package com.bednarz.flashcardsapi.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class OpenAiController {

    private final OpenAIService openAIService;

    @Autowired
    public OpenAiController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/healthz")
    public String checkGrammar() {
        return "OK";
    }

    @PostMapping("/check-sentence")
    public CorrectnessResponse checkCorrectness(@RequestParam String originalSentence, @RequestParam String sentenceToCheck) {
        CorrectnessResponse correctnessResponse = openAIService.checkCorrectness(originalSentence, sentenceToCheck);
        if (correctnessResponse.result) {
            //if is correct do extra check with gpt-4o model:
            return openAIService.extraCheckWithBetterModel(sentenceToCheck);
        } else {
            return correctnessResponse;
        }
    }

    @PostMapping("/dialogue-quiz")
    public OpenAiController.DialogueResponse getDialogueQuiz(@RequestBody List<String> phrases) {
        return openAIService.generateQuiz(phrases);
    }

    public record DialogueResponse(List<Dialogue> dialogueList) {
    }

    public record Dialogue(String A1, String B, String A2, String gap) {
    }


    record CorrectnessResponse(
            boolean result,
            String type,
            String correction,
            String explanation
    ) {
    }


}
