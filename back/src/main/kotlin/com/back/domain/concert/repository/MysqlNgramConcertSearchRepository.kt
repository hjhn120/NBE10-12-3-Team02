package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * MySQL(InnoDB) 환경 전용 FULLTEXT 인덱스(ngram 파서) 검색 구현체.
 * concert_name 컬럼에 ngram 파서 기반 FULLTEXT 인덱스가 필요하다:
 *   ALTER TABLE concert ADD FULLTEXT INDEX ft_concert_name (concert_name) WITH PARSER ngram;
 *
 * BOOLEAN MODE를 사용하며 특수문자 이스케이프 및 다중 단어별 '+단어' 조합을 수행한다.
 * ngram_token_size=2 특성상 1글자 검색어는 FULLTEXT 인덱스가 동작하지 않으므로 LIKE %k% 폴백을 수행한다.
 * keyword가 null이거나 blank, 또는 정제 후 빈 값이면 전체 목록을 반환한다.
 */
@Repository
@Profile("prod")
class MysqlNgramConcertSearchRepository(
    private val entityManager: EntityManager
) : ConcertSearchRepository {

    override fun findByKeyword(keyword: String?): List<Concert> {
        if (keyword.isNullOrBlank()) {
            return findAllConcerts()
        }

        val cleaned = keyword.replace(Regex("[+\\-*~()<>\":@%]"), " ").trim()
        if (cleaned.isBlank()) {
            return findAllConcerts()
        }

        // 1글자 검색어는 ngram_token_size=2 파서 특성상 FULLTEXT 인덱스 토큰 미매칭 방지를 위해 LIKE %k% 폴백
        if (cleaned.length < 2) {
            return findByLikeKeyword(cleaned)
        }

        val booleanKeyword = sanitizeBooleanKeyword(cleaned)

        @Suppress("UNCHECKED_CAST")
        return entityManager.createNativeQuery(
            """
            SELECT * FROM concert
            WHERE MATCH(concert_name) AGAINST(:keyword IN BOOLEAN MODE)
            """.trimIndent(),
            Concert::class.java
        )
            .setParameter("keyword", booleanKeyword)
            .resultList as List<Concert>
    }

    private fun findAllConcerts(): List<Concert> {
        return entityManager
            .createQuery("SELECT c FROM Concert c", Concert::class.java)
            .resultList
    }

    private fun findByLikeKeyword(rawKeyword: String): List<Concert> {
        return entityManager
            .createQuery(
                "SELECT c FROM Concert c WHERE c.concertName LIKE :keyword",
                Concert::class.java
            )
            .setParameter("keyword", "%$rawKeyword%")
            .resultList
    }

    /**
     * MySQL Boolean Mode 특수문자 정제 및 다중 단어별 '+단어' 파라미터 조합
     */
    private fun sanitizeBooleanKeyword(cleanedKeyword: String): String {
        val tokens = cleanedKeyword.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" ") { "+$it" }
    }
}
