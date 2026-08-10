package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * H2 인메모리 로컬/테스트 환경 전용 검색 구현체.
 * H2는 MySQL FULLTEXT 구문을 지원하지 않으므로 JPQL LIKE 및 한글 미완성 음절 REGEXP 쿼리를 수행한다.
 */
@Repository
@Profile("!prod")
class H2ConcertSearchRepository(
    private val entityManager: EntityManager
) : ConcertSearchRepository {

    override fun findByKeyword(keyword: String?): List<Concert> {
        if (keyword.isNullOrBlank()) {
            return entityManager
                .createQuery("SELECT c FROM Concert c", Concert::class.java)
                .resultList
        }

        val trimmed = keyword.trim()
        val hangulRegex = HangulSearchUtils.makeHangulIncompleteRegex(trimmed)

        return if (hangulRegex != null) {
            @Suppress("UNCHECKED_CAST")
            entityManager.createNativeQuery(
                "SELECT * FROM concert WHERE concert_name REGEXP :regex OR concert_name LIKE :likeKeyword",
                Concert::class.java
            )
                .setParameter("regex", hangulRegex)
                .setParameter("likeKeyword", "%$trimmed%")
                .resultList as List<Concert>
        } else {
            entityManager
                .createQuery(
                    "SELECT c FROM Concert c WHERE c.concertName LIKE :keyword",
                    Concert::class.java
                )
                .setParameter("keyword", "%$trimmed%")
                .resultList
        }
    }
}
