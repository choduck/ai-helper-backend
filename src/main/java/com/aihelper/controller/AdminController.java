package com.aihelper.controller;

import com.aihelper.model.User;
import com.aihelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    /**
     * 관리자 권한 체크 - 임시로 모든 요청 허용
     * AdminController의 각 API 엔드포인트(/api/admin/*)가 호출될 때마다 실행되어 권한을 검사
     * 현재는 getUsers()와 getUserById() 메서드에서 호출됨
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.warn("권한 확인 실패: 인증 정보 없음 (임시로 접근 허용)");
            return true; // 임시로 허용
        }
        
        String username = authentication.getName();
        logger.info("인증된 사용자: {}, 임시로 모든 접근 허용", username);
        
        // 임시로 모든 요청 허용
        return true;
    }

    /**
     * 사용자 목록 조회 (페이징)
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자 권한이 필요합니다.");
        }
        
        logger.info("사용자 목록 조회: page={}, size={}, search={}", page, size, search);
        
        Map<String, Object> response = userService.getUsersWithPaging(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 상세 정보 조회
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자 권한이 필요합니다.");
        }
        
        logger.info("사용자 상세 조회: id={}", id);
        
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("사용자를 찾을 수 없습니다.");
        }
        
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 생성
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자 권한이 필요합니다.");
        }
        
        logger.info("사용자 생성: username={}, email={}", user.getUsername(), user.getEmail());
        
        // 사용자명 중복 체크
        if (!userService.isUsernameAvailable(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("이미 사용 중인 사용자명입니다.");
        }
        
        // 이메일 중복 체크
        if (!userService.isEmailAvailable(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("이미 사용 중인 이메일입니다.");
        }
        
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("사용자 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("사용자 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자 권한이 필요합니다.");
        }
        
        logger.info("사용자 수정: id={}", id);
        
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("사용자를 찾을 수 없습니다.");
        }
        
        // ID 설정
        user.setUserId(id);
        
        try {
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("사용자 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("사용자 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 비밀번호 변경
     */
    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData) {
        
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자 권한이 필요합니다.");
        }
        
        logger.info("사용자 비밀번호 변경: id={}", id);
        
        String newPassword = passwordData.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("새 비밀번호가 필요합니다.");
        }
        
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("사용자를 찾을 수 없습니다.");
        }
        
        try {
            boolean success = userService.changePassword(id, newPassword);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("비밀번호 변경에 실패했습니다.");
            }
        } catch (Exception e) {
            logger.error("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자 권한이 필요합니다.");
        }
        
        logger.info("사용자 삭제: id={}", id);
        
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("사용자를 찾을 수 없습니다.");
        }
        
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("사용자 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            logger.error("사용자 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("사용자 삭제 중 오류가 발생했습니다.");
        }
    }
} 