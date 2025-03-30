package com.aihelper.service.impl;

import com.aihelper.config.JwtTokenUtil;
import com.aihelper.mapper.UserMapper;
import com.aihelper.model.LoginRequest;
import com.aihelper.model.LoginResponse;
import com.aihelper.model.User;
import com.aihelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) throws Exception {
        User user = userMapper.findByUsername(loginRequest.getUsername());
        
        System.out.println("user: " + user.getPassword());
        System.out.println("loginRequest: " + loginRequest.getPassword());
        System.out.println("user: " + user.getUsername());
        System.out.println("loginRequest: " + loginRequest.getUsername());
        
        if (user == null) {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }
        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        if ("INACTIVE".equals(user.getStatus())) {
            throw new Exception("비활성화된 계정입니다.");
        }
        
        // 마지막 로그인 시간 업데이트
        userMapper.updateLastLogin(user.getUserId());
        
        // JWT 토큰 생성
        String token = jwtTokenUtil.generateToken(user.getUsername());
        
        // 로그인 응답 생성
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        
        return response;
    }
    
    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }
    
    @Override
    public Map<String, Object> getUsersWithPaging(int page, int size, String search) {
        Map<String, Object> result = new HashMap<>();
        
        int offset = (page - 1) * size;
        List<User> users;
        int total;
        
        if (search != null && !search.trim().isEmpty()) {
            users = userMapper.search(search, offset, size);
            total = userMapper.countSearch(search);
        } else {
            users = userMapper.findAllWithPaging(offset, size);
            total = userMapper.countAll();
        }
        
        result.put("users", users);
        result.put("totalItems", total);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        result.put("currentPage", page);
        
        return result;
    }
    
    @Override
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 기본값 설정
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    @Transactional
    public User updateUser(User user) {
        // status가 null인 경우 기본값 설정
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        
        userMapper.update(user);
        return userMapper.findById(user.getUserId());
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long id, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        return userMapper.updatePassword(id, encodedPassword) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        return userMapper.findByUsername(username) == null;
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        return userMapper.findByEmail(email) == null;
    }
} 