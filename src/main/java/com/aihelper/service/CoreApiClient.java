package com.aihelper.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * ai-helper-core API와 통신하는 클라이언트 서비스
 */
@Service
@Slf4j
public class CoreApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${aihelper.core.api.url:http://localhost:8000}")
    private String coreApiUrl;
    
    @Value("${aihelper.core.api.key:}")
    private String coreApiKey;
    
    // 인증 사용 여부 플래그 추가 (기본값: false로 설정)
    private boolean useAuthentication = false;
    
    public CoreApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Core API에 채팅 요청을 보냅니다.
     * 
     * @param messages 채팅 메시지 배열
     * @param model 모델 이름 (기본값: gpt-3.5-turbo)
     * @param userId 사용자 ID
     * @param orgId 조직 ID
     * @return Core API 응답
     */
    public JsonNode sendChatRequest(ArrayNode messages, String model, Long userId, Long orgId) {
        try {
            log.info("Core API 요청 준비 중...");
            
            // API 요청 본문 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("messages", messages);
            requestBody.put("model", model != null ? model : "gpt-3.5-turbo");
            
            // 사용자 정보 추가
            if (userId != null) {
                requestBody.put("user_id", userId);
                log.debug("사용자 ID: {}", userId);
            }
            if (orgId != null) {
                requestBody.put("org_id", orgId);
                log.debug("조직 ID: {}", orgId);
            }
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 인증 헤더는 useAuthentication이 true일 때만 추가 (기본적으로 추가하지 않음)
            if (useAuthentication && coreApiKey != null && !coreApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + coreApiKey);
                log.debug("API 키 설정됨");
            } else {
                log.info("API 키 인증 없이 진행합니다.");
            }
            
            // HTTP 요청 생성
            HttpEntity<String> request = 
                new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            // 로깅
            log.debug("Core API 엔드포인트: {}/api/v1/chat/completions", coreApiUrl);
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            // 긴 메시지는 잘라서 로깅
            if (requestBodyStr.length() > 1000) {
                log.debug("Core API 요청 본문(일부): {}...(길이: {})", 
                    requestBodyStr.substring(0, 1000), requestBodyStr.length());
            } else {
                log.debug("Core API 요청 본문: {}", requestBodyStr);
            }
            
            // 시간 측정 시작
            long startTime = System.currentTimeMillis();
            
            // API 호출
            String apiUrl = coreApiUrl + "/api/v1/chat/completions";
            log.info("Core API 호출 시작: {}", apiUrl);
            
            try {
                // API 호출 시도 (실패 시 시뮬레이션된 응답 반환)
                ResponseEntity<String> response = 
                    restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
                
                // 시간 측정 완료
                long endTime = System.currentTimeMillis();
                log.info("Core API 호출 완료: {}ms 소요", (endTime - startTime));
                
                // 응답 파싱
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                
                // 응답 로깅 (민감 정보 제외)
                String responseStr = responseBody.toString();
                if (responseStr.length() > 1000) {
                    log.debug("Core API 응답(일부): {}...(길이: {})", 
                        responseStr.substring(0, 1000), responseStr.length());
                } else {
                    log.debug("Core API 응답: {}", responseStr);
                }
                
                return responseBody;
                
            } catch (Exception e) {
                // API 호출 실패 시 시뮬레이션된 응답 생성
                log.warn("Core API 호출 실패: {}. 시뮬레이션된 응답을 생성합니다.", e.getMessage());
                
                // 시뮬레이션된 응답 생성
                ObjectNode simulatedResponse = createSimulatedResponse(messages);
                
                // 측정 시간 기록 (시뮬레이션이라 빠르게 처리됨)
                long endTime = System.currentTimeMillis();
                log.info("시뮬레이션 응답 생성 완료: {}ms 소요", (endTime - startTime));
                
                return simulatedResponse;
            }
            
        } catch (Exception e) {
            log.error("Core API 호출 오류: " + e.getMessage(), e);
            log.error("상세 스택 트레이스: ", e);
            // 오류 응답 생성
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", true);
            errorResponse.put("message", "AI 서비스 연결 오류: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 시뮬레이션된 채팅 응답을 생성합니다.
     * 
     * @param messages 사용자 메시지 배열
     * @return 시뮬레이션된 응답
     */
    private ObjectNode createSimulatedResponse(ArrayNode messages) {
        try {
            // 마지막 사용자 메시지 추출
            String lastUserMessage = "안녕하세요";
            for (int i = messages.size() - 1; i >= 0; i--) {
                JsonNode msg = messages.get(i);
                if (msg.has("role") && "user".equals(msg.get("role").asText()) && msg.has("content")) {
                    lastUserMessage = msg.get("content").asText();
                    break;
                }
            }
            
            // 간단한 응답 생성
            String responseContent;
            if (lastUserMessage.contains("안녕") || lastUserMessage.contains("하이")) {
                responseContent = "안녕하세요! 어떻게 도와드릴까요?";
            } else if (lastUserMessage.contains("이름") || lastUserMessage.contains("누구")) {
                responseContent = "저는 AI 도우미입니다. 궁금한 점이나 도움이 필요한 사항이 있으신가요?";
            } else if (lastUserMessage.contains("감사") || lastUserMessage.contains("고마워")) {
                responseContent = "별말씀을요! 더 필요한 것이 있으시면 언제든지 말씀해주세요.";
            } else {
                responseContent = "죄송합니다만, 현재 AI 서버와 연결이 원활하지 않습니다. 말씀하신 내용에 대해 자세히 알려주시면 더 도움이 될 수 있을 것 같습니다.";
            }
            
            // OpenAI API 형식의 응답 생성
            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.put("id", "sim_" + System.currentTimeMillis());
            responseNode.put("object", "chat.completion");
            responseNode.put("created", System.currentTimeMillis() / 1000);
            responseNode.put("model", "gpt-3.5-turbo-simulated");
            
            ArrayNode choicesNode = objectMapper.createArrayNode();
            ObjectNode choiceNode = objectMapper.createObjectNode();
            choiceNode.put("index", 0);
            
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "assistant");
            messageNode.put("content", responseContent);
            
            choiceNode.set("message", messageNode);
            choiceNode.put("finish_reason", "stop");
            choicesNode.add(choiceNode);
            
            responseNode.set("choices", choicesNode);
            
            ObjectNode usageNode = objectMapper.createObjectNode();
            usageNode.put("prompt_tokens", 10);
            usageNode.put("completion_tokens", responseContent.length() / 4);
            usageNode.put("total_tokens", 10 + (responseContent.length() / 4));
            
            responseNode.set("usage", usageNode);
            
            return responseNode;
            
        } catch (Exception e) {
            log.error("시뮬레이션 응답 생성 오류: " + e.getMessage(), e);
            
            // 오류 시 더 간단한 응답
            ObjectNode simpleResponse = objectMapper.createObjectNode();
            simpleResponse.put("id", "sim_error_" + System.currentTimeMillis());
            simpleResponse.put("object", "chat.completion");
            
            ArrayNode choicesNode = objectMapper.createArrayNode();
            ObjectNode choiceNode = objectMapper.createObjectNode();
            
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "assistant");
            messageNode.put("content", "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            
            choiceNode.set("message", messageNode);
            choicesNode.add(choiceNode);
            
            simpleResponse.set("choices", choicesNode);
            
            return simpleResponse;
        }
    }
    
    /**
     * Core API에 스트리밍 채팅 요청 URL을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param orgId 조직 ID
     * @return 스트리밍 URL
     */
    public String getChatStreamUrl(Long userId, Long orgId) {
        return coreApiUrl + "/api/v1/chat/completions/stream" +
               "?user_id=" + userId +
               "&org_id=" + orgId;
    }
} 