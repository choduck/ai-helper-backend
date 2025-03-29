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
        
        // 디버깅 로그 추가
        System.out.println("찾은 사용자: " + user.getUsername());
        System.out.println("비밀번호 존재 여부: " + (user.getPassword() != null && !user.getPassword().isEmpty()));
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            System.out.println("경고: 비밀번호가 null이거나 비어 있습니다!");
            // 임시 비밀번호 설정 (테스트용)
            user.setPassword("{bcrypt}$2a$10$GjAQXeGWXP3aXcT5R/LOOOj.MiNl4WiLYGSYexc3.SYcnHLg7.xtO");
        }
        
        // 역할에 따른 권한 부여
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
        System.out.println("권한: " + authorities);
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // ID 대신 username을 사용자 식별자로 사용
                user.getPassword(),
                authorities
        );
    }
} 