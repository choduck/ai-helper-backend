package com.p6spy.engine.spy.appender;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

/**
 * SQL 쿼리 포맷팅을 위한 커스텀 클래스
 */
public class CustomLineFormat implements MessageFormattingStrategy {
    
    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, 
                               String prepared, String sql, String url) {
        
        // SQL이 없는 경우 처리
        if (sql == null || sql.trim().equals("")) {
            return "";
        }
        
        // SQL 문 가독성 향상을 위한 포맷팅
        String formattedSql = formatSql(sql);
        
        return String.format("SQL 실행 [%s] | 소요시간: %dms | %s", 
                now, elapsed, formattedSql);
    }
    
    /**
     * SQL 문을 정렬하여 더 읽기 쉽게 표시
     */
    private String formatSql(String sql) {
        // 이미 포맷팅 된 경우 그대로 반환
        if (sql.contains("\n")) {
            return sql;
        }
        
        // 주요 SQL 키워드 기준으로 줄바꿈 처리
        sql = sql.replaceAll("(?i)SELECT", "\nSELECT")
                 .replaceAll("(?i)FROM", "\nFROM")
                 .replaceAll("(?i)WHERE", "\nWHERE")
                 .replaceAll("(?i)AND", "\n  AND")
                 .replaceAll("(?i)OR", "\n  OR")
                 .replaceAll("(?i)ORDER BY", "\nORDER BY")
                 .replaceAll("(?i)GROUP BY", "\nGROUP BY")
                 .replaceAll("(?i)HAVING", "\nHAVING")
                 .replaceAll("(?i)LIMIT", "\nLIMIT")
                 .replaceAll("(?i)INSERT INTO", "\nINSERT INTO")
                 .replaceAll("(?i)VALUES", "\nVALUES")
                 .replaceAll("(?i)UPDATE", "\nUPDATE")
                 .replaceAll("(?i)SET", "\nSET")
                 .replaceAll("(?i)DELETE FROM", "\nDELETE FROM")
                 .replaceAll("(?i)JOIN", "\nJOIN")
                 .replaceAll("(?i)LEFT JOIN", "\nLEFT JOIN")
                 .replaceAll("(?i)RIGHT JOIN", "\nRIGHT JOIN")
                 .replaceAll("(?i)INNER JOIN", "\nINNER JOIN");
        
        return sql;
    }
} 