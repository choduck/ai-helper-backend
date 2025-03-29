package com.aihelper.service;

import com.aihelper.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // findByUsername() 대신 UserMapper를 통해 직접 사용자 조회
        User user = null;
        try {
            // 사용자명으로 조회
            for (User u : userService.getAllUsers()) {
                if (u.getUsername().equals(username)) {
                    user = u;
                    break;
                }
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("유저 검색 중 오류 발생: " + e.getMessage());
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("유저를 찾을 수 없습니다: " + username);
        }
        
        // 역할에 따른 권한 부여
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
        
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()), // ID를 사용자 식별자로 사용
                user.getPassword(),
                authorities
        );
    }
} 