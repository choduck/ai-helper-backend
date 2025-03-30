package com.aihelper.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aihelper.model.User;
import com.aihelper.service.CoreApiClient;
import com.aihelper.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final CoreApiClient coreApiClient;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    /**
     * 채팅 완성 요청을 처리합니다.
     * 
     * @param requestBody 채팅 요청 본문
     * @param authentication 인증 정보
     * @return AI 응답
     */
    @PostMapping("/completions")
    public ResponseEntity<JsonNode> chatCompletions(
            @RequestBody JsonNode requestBody,
            Authentication authentication) {
        
        log.info("채팅 API 호출 - 사용자: {}", authentication.getName());
        log.debug("채팅 API 요청 본문: {}", requestBody.toString());
        
        try {
            // 현재 인증된 사용자 정보 조회
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                log.warn("인증된 사용자 정보를 찾을 수 없음: {}", authentication.getName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            log.info("사용자 정보 조회 성공 - 사용자 ID: {}, 조직 ID: {}", user.getUserId(), user.getOrgId());
            
            // 요청에서 메시지 배열 추출
            ArrayNode messages = (ArrayNode) requestBody.get("messages");
            if (messages == null || messages.size() == 0) {
                log.warn("메시지 배열이 비어있거나 존재하지 않음");
                return ResponseEntity.badRequest().build();
            }
            
            log.debug("메시지 개수: {}", messages.size());
            
            // 모델 파라미터 추출
            String model = requestBody.has("model") ? requestBody.get("model").asText() : null;
            log.debug("요청 모델: {}", model != null ? model : "기본값");
            
            // Core API 호출
            log.info("Core API 호출 시작...");
            JsonNode response = coreApiClient.sendChatRequest(
                messages, 
                model, 
                user.getUserId(), 
                user.getOrgId()
            );
            log.info("Core API 호출 완료");
            
            // 응답 로깅 (민감 정보 제외)
            if (response.has("id")) {
                log.debug("응답 ID: {}", response.get("id").asText());
            }
            
            // 응답 반환
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("채팅 요청 처리 중 오류: " + e.getMessage(), e);
            JsonNode errorResponse = objectMapper.createObjectNode()
                .put("error", true)
                .put("message", "채팅 요청 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 스트리밍 채팅 URL을 반환합니다.
     * 
     * @param authentication 인증 정보
     * @return 스트리밍 URL
     */
    @GetMapping("/stream-url")
    public ResponseEntity<JsonNode> getChatStreamUrl(Authentication authentication) {
        try {
            // 현재 인증된 사용자 정보 조회
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 스트리밍 URL 생성
            String streamUrl = coreApiClient.getChatStreamUrl(user.getUserId(), user.getOrgId());
            
            // 응답 생성
            JsonNode response = objectMapper.createObjectNode()
                .put("url", streamUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("스트림 URL 생성 중 오류: " + e.getMessage(), e);
            JsonNode errorResponse = objectMapper.createObjectNode()
                .put("error", true)
                .put("message", "스트림 URL 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 