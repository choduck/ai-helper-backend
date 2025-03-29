package com.aihelper.mapper;

import com.aihelper.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    // 사용자 목록 조회
    List<User> findAll();
    
    // 페이징된 사용자 목록 조회
    List<User> findAllWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    // 사용자 총 수 조회
    int countAll();
    
    // ID로 사용자 조회
    User findById(@Param("id") Long id);
    
    // 사용자명으로 사용자 조회
    User findByUsername(@Param("username") String username);
    
    // 이메일로 사용자 조회
    User findByEmail(@Param("email") String email);
    
    // 사용자 추가
    int insert(User user);
    
    // 사용자 정보 업데이트
    int update(User user);
    
    // 비밀번호 업데이트
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    // 마지막 로그인 시간 업데이트
    int updateLastLogin(@Param("id") Long id);
    
    // 사용자 삭제
    int deleteById(@Param("id") Long id);
    
    // 검색 쿼리
    List<User> search(@Param("query") String query, @Param("offset") int offset, @Param("limit") int limit);
    
    // 검색 결과 총 수
    int countSearch(@Param("query") String query);
} 