package com.docflow.llm;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleLlmClient implements LlmClient{

    private final RestClient restClient;
    private final LlmClientProperties props;

    public OpenAiCompatibleLlmClient(LlmClientProperties props, RestClient.Builder builder){
        this.props = props;

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(props.timeout());

        String k = props.apiKey();
        System.out.println(k == null ? "NULL" : k.substring(0, Math.min(3, k.length())) + " | tamanho: " + k.length());

        this.restClient = builder
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .requestFactory(factory)
                .build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {

        Map<String, Object> body = Map.of(
          "model", props.model(),
          "temeprature", props.temperature(),
          "messages", List.of(
                  Map.of("role", "system", "content", systemPrompt),
                  Map.of("role", "user", "content", userPrompt)
        ));

        try{
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(ChatResponse.class);
            if(response == null || response.choices() == null || response.choices().isEmpty()){
                throw new LlmException("Resposta vazia do provedor");
            }

            return response.choices().get(0).message().content();
        } catch (RestClientException e) {
            throw new LlmException("Falha ao chamar o provedor de LLM", e);
        }

    }

    record ChatResponse(List<Choice> choices) {}
    record Choice(Message message) {}
    record Message(String content) {}

}
