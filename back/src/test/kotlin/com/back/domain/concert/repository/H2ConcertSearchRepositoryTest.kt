package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import com.back.global.RedisTestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Import(RedisTestConfig::class)
@DisplayName("H2ConcertSearchRepository (!prod 프로필) LIKE & 한글 미완성 자소 검색 통합 테스트")
class H2ConcertSearchRepositoryTest {

    @Autowired
    private lateinit var concertSearchRepository: ConcertSearchRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @BeforeEach
    fun setUp() {
        concertRepository.deleteAll()
        concertRepository.saveAll(
            listOf(
                Concert.create("아이유 서울 콘서트", null, LocalDateTime.now(), LocalDateTime.now().plusDays(10), null),
                Concert.create("BTS World Tour 서울", null, LocalDateTime.now(), LocalDateTime.now().plusDays(20), null),
                Concert.create("Coldplay Music of the Spheres", null, LocalDateTime.now(), LocalDateTime.now().plusDays(30), null),
                Concert.create("NewJeans 팝업 콘서트", null, LocalDateTime.now(), LocalDateTime.now().plusDays(5), null),
                Concert.create("김천 시립 예술단 콘서트", null, LocalDateTime.now(), LocalDateTime.now().plusDays(15), null),
            )
        )
    }

    @Test
    @DisplayName("keyword가 null이면 전체 콘서트를 반환한다")
    fun findAll_whenKeywordIsNull() {
        val result = concertSearchRepository.findByKeyword(null)
        assertThat(result).hasSize(5)
    }

    @Test
    @DisplayName("keyword가 빈 문자열이면 전체 콘서트를 반환한다")
    fun findAll_whenKeywordIsBlank() {
        val result = concertSearchRepository.findByKeyword("  ")
        assertThat(result).hasSize(5)
    }

    @Test
    @DisplayName("한글 키워드로 부분 검색이 동작한다")
    fun search_withKoreanKeyword() {
        val result = concertSearchRepository.findByKeyword("서울")
        assertThat(result).hasSize(2)
        assertThat(result.map { it.concertName }).containsExactlyInAnyOrder(
            "아이유 서울 콘서트",
            "BTS World Tour 서울"
        )
    }

    @Test
    @DisplayName("한글 미완성 받침 입력(김처) 시 김천 콘서트가 정상 검색된다")
    fun search_hangulIncompleteSyllable_matchesTargetWord() {
        val result = concertSearchRepository.findByKeyword("김처")
        assertThat(result).hasSize(1)
        assertThat(result.first().concertName).isEqualTo("김천 시립 예술단 콘서트")
    }

    @Test
    @DisplayName("한글 독립 초성 입력(김ㅊ) 시 김천 콘서트가 정상 검색된다")
    fun search_hangulIsolatedChosung_matchesTargetWord() {
        val result = concertSearchRepository.findByKeyword("김ㅊ")
        assertThat(result).hasSize(1)
        assertThat(result.first().concertName).isEqualTo("김천 시립 예술단 콘서트")
    }

    @Test
    @DisplayName("영어 키워드로 부분 검색이 동작한다")
    fun search_withEnglishKeyword() {
        val result = concertSearchRepository.findByKeyword("BTS")
        assertThat(result).hasSize(1)
        assertThat(result.first().concertName).isEqualTo("BTS World Tour 서울")
    }

    @Test
    @DisplayName("한/영 혼합 콘서트 제목에서 한글 키워드로 검색이 동작한다")
    fun search_withKoreanKeywordOnMixedTitle() {
        val result = concertSearchRepository.findByKeyword("팝업")
        assertThat(result).hasSize(1)
        assertThat(result.first().concertName).isEqualTo("NewJeans 팝업 콘서트")
    }

    @Test
    @DisplayName("일치하는 콘서트가 없으면 빈 리스트를 반환한다")
    fun search_returnsEmpty_whenNoMatch() {
        val result = concertSearchRepository.findByKeyword("없는키워드xyz")
        assertThat(result).isEmpty()
    }
}
