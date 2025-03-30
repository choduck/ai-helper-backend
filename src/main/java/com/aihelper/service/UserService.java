package com.aihelper.service;

import com.aihelper.model.LoginRequest;
import com.aihelper.model.LoginResponse;
import com.aihelper.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    
    // 로그인 처리
    LoginResponse login(LoginRequest loginRequest) throws Exception;
    
    // 사용자 목록 조회
    List<User> getAllUsers();
    
    // 페이징된 사용자 목록 조회
    Map<String, Object> getUsersWithPaging(int page, int size, String search);
    
    // ID로 사용자 조회
    User getUserById(Long id);
    
    // 사용자명으로 사용자 조회
    User findByUsername(String username);
    
    // 사용자 생성
    User createUser(User user);
    
    // 사용자 정보 업데이트
    User updateUser(User user);
    
    // 비밀번호 변경
    boolean changePassword(Long id, String newPassword);
    
    // 사용자 삭제
    boolean deleteUser(Long id);
    
    // 사용자명 중복 체크
    boolean isUsernameAvailable(String username);
    
    // 이메일 중복 체크
    boolean isEmailAvailable(String email);
} 