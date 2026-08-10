package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * H2 인메모리 테스트 환경 전용 LIKE 검색 구현체.
 * H2는 Mroonga/MySQL FULLTEXT 구문을 지원하지 않으므로 기존 JPQL LIKE 쿼리를 유지한다.
 */
@Repository
@Profile("test")
class H2ConcertSearchRepository(
    private val entityManager: EntityManager
) : ConcertSearchRepository {

    override fun findByKeyword(keyword: String?): List<Concert> {
        return if (keyword.isNullOrBlank()) {
            entityManager
                .createQuery("SELECT c FROM Concert c", Concert::class.java)
                .resultList
        } else {
            entityManager
                .createQuery(
                    "SELECT c FROM Concert c WHERE c.concertName LIKE :keyword",
                    Concert::class.java
                )
                .setParameter("keyword", "%${keyword.trim()}%")
                .resultList
        }
    }
}
