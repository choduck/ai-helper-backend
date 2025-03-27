package com.aihelper.service;

import com.aihelper.config.JwtTokenUtil;
import com.aihelper.model.LoginRequest;
import com.aihelper.model.LoginResponse;
import com.aihelper.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 하드코딩된 사용자 정보 (1단계)
    private static final String HARDCODED_USERNAME = "user";
    private static final String HARDCODED_PASSWORD = "user";
    private static final String HARDCODED_NAME = "AI Helper User";

    @Override
    public LoginResponse login(LoginRequest loginRequest) throws Exception {
        // 1단계에서는 MySQL 연결 없이 하드코딩된 user/user로만 로그인 가능
        if (HARDCODED_USERNAME.equals(loginRequest.getUsername()) && HARDCODED_PASSWORD.equals(loginRequest.getPassword())) {
            // 로그인 성공시 JWT 토큰 생성
            String token = jwtTokenUtil.generateToken(loginRequest.getUsername());
            
            // 로그인 응답 생성
            return LoginResponse.builder()
                    .token(token)
                    .username(loginRequest.getUsername())
                    .name(HARDCODED_NAME)
                    .build();
        } else {
            throw new Exception("Invalid username or password");
        }
    }

    @Override
    public User findByUsername(String username) {
        // 1단계에서는 하드코딩된 사용자 정보 반환
        if (HARDCODED_USERNAME.equals(username)) {
            return User.builder()
                    .id(1L)
                    .username(HARDCODED_USERNAME)
                    .password(passwordEncoder.encode(HARDCODED_PASSWORD)) // 실제로는 암호화된 비밀번호를 저장
                    .name(HARDCODED_NAME)
                    .email("user@aihelper.com")
                    .enabled(true)
                    .build();
        }
        return null;
    }
} 