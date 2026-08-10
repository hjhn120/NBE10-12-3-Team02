package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import com.back.global.util.HangulSearchUtils
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * MySQL(InnoDB) 환경 전용 FULLTEXT 인덱스(ngram 파서) 및 한글 미완성 음절 자소 REGEXP 검색 구현체.
 * concert_name 컬럼에 ngram 파서 기반 FULLTEXT 인덱스가 필요하다:
 *   ALTER TABLE concert ADD FULLTEXT INDEX ft_concert_name (concert_name) WITH PARSER ngram;
 *
 * 1. 한글 독립 초성(예: "김ㅊ" -> "김[차-칗]") 또는 미완성 받침(예: "김처" -> "김[처-첳]") 시 자소 범위 REGEXP 검색 수행
 * 2. 1글자 검색어 시 LIKE %k% 폴백
 * 3. 2글자 이상 완성형은 FULLTEXT IN BOOLEAN MODE 초고속 검색 ➔ 결과 0건 시 LIKE 폴백
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

        val cleaned = keyword.replace(SPECIAL_CHAR_REGEX, " ").trim()
        if (cleaned.isBlank()) {
            return findAllConcerts()
        }

        // 1. 한글 독립 초성(김ㅊ) 또는 미완성 받침(김처) 자소 범위 REGEXP 검색
        val hangulRegex = HangulSearchUtils.makeHangulIncompleteRegex(cleaned)
        if (hangulRegex != null) {
            return findByRegexOrLike(hangulRegex, cleaned)
        }

        // 2. 1글자 검색어는 ngram_token_size=2 파서 특성상 FULLTEXT 인덱스 토큰 미매칭 방지를 위해 바로 LIKE 폴백
        if (cleaned.length < 2) {
            return findByLikeKeyword(cleaned)
        }

        val booleanKeyword = sanitizeBooleanKeyword(cleaned)

        @Suppress("UNCHECKED_CAST")
        val fullTextResults = entityManager.createNativeQuery(
            """
            SELECT * FROM concert
            WHERE MATCH(concert_name) AGAINST(:keyword IN BOOLEAN MODE)
            """.trimIndent(),
            Concert::class.java
        )
            .setParameter("keyword", booleanKeyword)
            .resultList as List<Concert>

        // 1차 FULLTEXT 결과가 존재하면 즉시 반환, 0건이면 2차 LIKE 쿼리로 스마트 구원
        if (fullTextResults.isNotEmpty()) {
            return fullTextResults
        }

        return findByLikeKeyword(cleaned)
    }

    private fun findAllConcerts(): List<Concert> {
        return entityManager
            .createQuery("SELECT c FROM Concert c", Concert::class.java)
            .resultList
    }

    private fun findByRegexOrLike(regex: String, rawKeyword: String): List<Concert> {
        @Suppress("UNCHECKED_CAST")
        return entityManager.createNativeQuery(
            "SELECT * FROM concert WHERE concert_name REGEXP :regex OR concert_name LIKE :likeKeyword",
            Concert::class.java
        )
            .setParameter("regex", regex)
            .setParameter("likeKeyword", "%$rawKeyword%")
            .resultList as List<Concert>
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
        val tokens = cleanedKeyword.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" ") { "+$it" }
    }

    companion object {
        private val SPECIAL_CHAR_REGEX = Regex("[+\\-*~()<>\":@%]")
        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}
