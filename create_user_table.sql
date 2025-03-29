-- 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  fullname VARCHAR(100),
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  last_login TIMESTAMP NULL
);

-- 기존 테스트 사용자가 있으면 삭제
DELETE FROM users WHERE username = 'user' OR username = 'admin';

-- 테스트 사용자 추가 (BCrypt로 암호화된 비밀번호: 'user'와 'admin')
INSERT INTO users (username, password, email, fullname, role, status)
VALUES 
('user', '$2a$10$vY5Tm1J/Z4Nh2Xk.NeZnVeufhuU9PlF4VVoU7aGZQs8tALCjxmGQS', 'user@example.com', '일반 사용자', 'USER', 'ACTIVE'),
('admin', '$2a$10$3A9u2wQZRK2pHJTc0PGQpejHnRPEQaktXDQqVc7PwDsHUYXhGaXFa', 'admin@example.com', '관리자', 'ADMIN', 'ACTIVE');

-- 사용자 확인
SELECT * FROM users; 