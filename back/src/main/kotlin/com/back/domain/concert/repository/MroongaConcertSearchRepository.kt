package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * MySQL(Mroonga) 환경 전용 Full-Text Search 구현체.
 * concert_name 컬럼에 TokenBigram 파서 기반 FULLTEXT 인덱스가 필요하다:
 *   ALTER TABLE concert ADD FULLTEXT INDEX ft_concert_name (concert_name) COMMENT 'parser "TokenBigram"';
 *
 * BOOLEAN MODE를 사용하며 특수문자 이스케이프 및 다중 단어별 '+단어*' 전방 일치(prefix) 조합을 수행한다.
 * keyword가 null이거나 blank, 또는 정제 후 빈 값이면 전체 목록을 반환한다.
 */
@Repository
@Profile("prod")
class MroongaConcertSearchRepository(
    private val entityManager: EntityManager
) : ConcertSearchRepository {

    override fun findByKeyword(keyword: String?): List<Concert> {
        if (keyword.isNullOrBlank()) {
            return findAllConcerts()
        }

        val booleanKeyword = sanitizeBooleanKeyword(keyword)
        if (booleanKeyword.isBlank()) {
            return findAllConcerts()
        }

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

    /**
     * Mroonga Boolean Mode 특수문자 정제 및 다중 단어별 '+단어*' 전방 일치 파라미터 조합
     */
    private fun sanitizeBooleanKeyword(keyword: String): String {
        val cleaned = keyword.replace(Regex("[+\\-*~()<>\":@%]"), " ")
        val tokens = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" ") { "+$it*" }
    }
}
