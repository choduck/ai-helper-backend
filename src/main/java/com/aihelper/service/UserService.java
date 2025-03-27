package com.aihelper.service;

import com.aihelper.model.LoginRequest;
import com.aihelper.model.LoginResponse;
import com.aihelper.model.User;

public interface UserService {
    LoginResponse login(LoginRequest loginRequest) throws Exception;
    User findByUsername(String username);
} 