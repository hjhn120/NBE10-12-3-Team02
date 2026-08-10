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
 * BOOLEAN MODE를 사용하므로 검색어 뒤에 '*'를 붙여 전방 일치(prefix) 검색을 지원한다.
 * keyword가 null이거나 blank이면 전체 목록을 반환한다.
 */
@Repository
@Profile("!test")
class MroongaConcertSearchRepository(
    private val entityManager: EntityManager
) : ConcertSearchRepository {

    override fun findByKeyword(keyword: String?): List<Concert> {
        if (keyword.isNullOrBlank()) {
            return entityManager
                .createQuery("SELECT c FROM Concert c", Concert::class.java)
                .resultList
        }

        // Boolean Mode: '+키워드*' → 해당 토큰으로 시작하는 모든 ngram을 포함한 결과 반환
        val booleanKeyword = "+${keyword.trim()}*"

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
}
