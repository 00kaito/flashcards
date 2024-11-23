package com.bednarz.flashcardsapi.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    private String processWithAI(String systemRole, String instruction, String text, String aiModel) {
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemRole));
        messages.add(createMessage("user", instruction + text));
        Map<String, Object> body = createBody(messages, aiModel);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, String> message = (Map<String, String>) firstChoice.get("message");

        return message.get("content").toString().trim();
    }

    private Map<String, Object> createBody(List<Map<String, String>> messages, String aiModel) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiModel);
        body.put("messages", messages);
        return body;
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

//    public String checkGrammar(String text) {
//        String systemRole = "You act as grammar check that ignore punctuation marks mistakes and capitalization, just check sentence syntactically";
//        String instruction = "Check if the sentence makes sense, ignoring grammar. If correct, return true. If incorrect, return false, the sentence, and an explanation. Respond in JSON format";
//        String textToProcess = text;
//        return this.processWithAI(systemRole, instruction, textToProcess);
//    }


    public OpenAiController.CorrectnessResponse checkCorrectness(String originalSentence, String sentenceToCheck) {
        String systemRole = "you act as helpful assistant";
        String instruction = """
                Dostarczę Ci 2 zdania, oryginalne oraz podobne. Wykonaj poniższe kroki:
                1. Sprawdź czy mimo użycia innych słów wyrażają zbliżony sens.
                2. Jeśli tak odpowiedz w formacie JSON {result: true}.
                3. Jeśli nie, wybierz który error type został popełniony z listy: [grammar, punctuation, capitalization] odpowiedz w formacie json i zwróć poprawne zdanie numer dwa wraz z objaśnieniem błedu, np.: {result: boolean, type:string, correction: string, explanation: string}
                4. double check if json is valid, check that all 4 fields are filled, escape quotes inside
                """;
        StringBuilder sb = new StringBuilder();
        sb.append("original sentence: ")
                .append(originalSentence)
                .append("sentence to check: ")
                .append(sentenceToCheck);
        String textToProcess = sb.toString();
        String response = this.processWithAI(systemRole, instruction, textToProcess, "gpt-3.5-turbo");

        return handleOpenAiResponse(response);
    }

    public OpenAiController.DialogueResponse generateQuiz(List<String> phrases) {
        String systemRole = "you act as helpful assistant that response with valid json";
        String intro = "1. Based on provided phrases create " + phrases.size() + " dialogues";
        String instruction = """
                2. Replace that phrases with gaps __________.
                3. Each dialogue contain only 1 gap.
                Provide response as dialogue json:
                {[
                A1: "" //person 1
                B:"" //person 2
                A2:"" //person 1
                gap:"" //gap phrase matched to this dialogue
                ]}
                4. provide answer in 1 JSON with array and make sure it's valid json format
                 """;
        StringBuilder sb = new StringBuilder();
        sb.append("phrases: \n")
                .append(phrases.stream().collect(Collectors.joining("\n- ")));
        String textToProcess = sb.toString();
        String response = this.processWithAI(systemRole, intro + instruction, textToProcess, "gpt-3.5-turbo");

        try {
            String jsonResponse = extractJson(response);
            OpenAiController.DialogueResponse dialogueResponse = mapper.readValue(jsonResponse, OpenAiController.DialogueResponse.class);
            return dialogueResponse;
        } catch (JsonProcessingException e) {
            throw new InvalidParameterException("parse open ai repsonse error");
        }
    }

    private String extractJson(String input) {
        Pattern pattern = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return "{ \"dialogueList\": [" + matcher.group(1) + "]}";
        }
        throw new IllegalArgumentException("cannon extract Json from provided string");
    }

    public OpenAiController.CorrectnessResponse extraCheckWithBetterModel(String sentenceToCheck) {
        String systemRole = "you act as helpful assistant";
        String instruction = """
                Check grammar, if its correct provide result true , but if there are any grammatical issues explain in json:
                JSON:
                {result: boolean, type: 'error type if applied', correction: 'correct/improved sentence if applied', explanation: 'details if applied'}
                make sure JSON if correctly parsed (without any additional quotations)
                """;
        StringBuilder sb = new StringBuilder();
        sb.append("sentence to check: ")
                .append(sentenceToCheck);
        String textToProcess = sb.toString();
        String response = this.processWithAI(systemRole, instruction, textToProcess, "gpt-4o");
        return handleOpenAiResponse(response);

    }


    private OpenAiController.CorrectnessResponse handleOpenAiResponse(String response) {
        try {
            OpenAiController.CorrectnessResponse correctnessResponse = mapper.readValue(response, OpenAiController.CorrectnessResponse.class);
            if (!correctnessResponse.result() && !correctnessResponse.type().equalsIgnoreCase("grammar")) {
                return new OpenAiController.CorrectnessResponse(true, "", "", "");
            } else {
                return correctnessResponse;
            }
        } catch (JsonProcessingException e) {
            throw new InvalidParameterException("parse open ai repsonse error");
        }
    }


}
