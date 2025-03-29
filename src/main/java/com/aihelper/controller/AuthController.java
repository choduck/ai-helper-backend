package com.aihelper.controller;

import com.aihelper.model.LoginRequest;
import com.aihelper.model.LoginResponse;
import com.aihelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("로그인 시도: username={}", loginRequest.getUsername());
        
        try {
            LoginResponse response = userService.login(loginRequest);
            logger.info("로그인 성공: username={}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("로그인 실패: username={}, 에러={}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인 실패: " + e.getMessage());
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> check() {
        logger.info("인증 API 상태 확인");
        return ResponseEntity.ok("Authentication API is working");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        logger.info("로그아웃 요청 처리");
        // 클라이언트 측에서 토큰 삭제가 주요 로그아웃 메커니즘이지만,
        // 향후 토큰 블랙리스트 등 서버 측 로그아웃 로직을 여기에 구현할 수 있습니다.
        return ResponseEntity.ok(Map.of(
            "message", "로그아웃 성공",
            "status", "success"
        ));
    }
} 