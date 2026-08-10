package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert

/**
 * 콘서트 키워드 검색 전략 인터페이스.
 * MySQL(Mroonga) 환경과 H2(테스트) 환경에서 각각 다른 구현체가 주입된다.
 */
interface ConcertSearchRepository {
    fun findByKeyword(keyword: String?): List<Concert>
}
