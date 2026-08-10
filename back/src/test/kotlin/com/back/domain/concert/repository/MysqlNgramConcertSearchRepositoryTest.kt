package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

/**
 * MysqlNgramConcertSearchRepository 단위 테스트.
 * MySQL InnoDB ngram FULLTEXT 쿼리가 올바른 정제 파라미터로 실행되는지 검증한다.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("MysqlNgramConcertSearchRepository MATCH/AGAINST 정제 쿼리 단위 테스트")
class MysqlNgramConcertSearchRepositoryTest {

    @Mock
    private lateinit var entityManager: EntityManager

    @Mock
    private lateinit var nativeQuery: Query

    @Mock
    private lateinit var jpqlQuery: TypedQuery<Concert>

    private lateinit var repository: MysqlNgramConcertSearchRepository

    @BeforeEach
    fun setUp() {
        repository = MysqlNgramConcertSearchRepository(entityManager)
    }

    @Test
    @DisplayName("keyword가 null이면 전체 조회 JPQL이 실행된다")
    fun findAll_whenKeywordIsNull() {
        `when`(entityManager.createQuery(anyString(), eq(Concert::class.java)))
            .thenReturn(jpqlQuery)
        `when`(jpqlQuery.resultList).thenReturn(emptyList())

        repository.findByKeyword(null)

        verify(entityManager).createQuery("SELECT c FROM Concert c", Concert::class.java)
    }

    @Test
    @DisplayName("keyword가 빈 문자열이면 전체 조회 JPQL이 실행된다")
    fun findAll_whenKeywordIsBlank() {
        `when`(entityManager.createQuery(anyString(), eq(Concert::class.java)))
            .thenReturn(jpqlQuery)
        `when`(jpqlQuery.resultList).thenReturn(emptyList())

        repository.findByKeyword("   ")

        verify(entityManager).createQuery("SELECT c FROM Concert c", Concert::class.java)
    }

    @Test
    @DisplayName("특수 연산 문자만(+++, ***) 입력되면 정제 후 전체 조회 JPQL이 실행된다")
    fun findAll_whenOnlySpecialCharactersEntered() {
        `when`(entityManager.createQuery(anyString(), eq(Concert::class.java)))
            .thenReturn(jpqlQuery)
        `when`(jpqlQuery.resultList).thenReturn(emptyList())

        repository.findByKeyword("+++ *** () <>")

        verify(entityManager).createQuery("SELECT c FROM Concert c", Concert::class.java)
    }

    @Test
    @DisplayName("keyword가 있으면 MATCH/AGAINST 네이티브 쿼리가 '+keyword' Boolean Mode 형식으로 실행된다")
    fun search_withKeyword_usesBooleanModePrefix() {
        `when`(entityManager.createNativeQuery(anyString(), eq(Concert::class.java)))
            .thenReturn(nativeQuery)
        `when`(nativeQuery.setParameter(eq("keyword"), any())).thenReturn(nativeQuery)
        `when`(nativeQuery.resultList).thenReturn(mutableListOf<Any?>())

        repository.findByKeyword("서울")

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(nativeQuery).setParameter(eq("keyword"), captor.capture() as Any?)
        assertThat(captor.value).isEqualTo("+서울")
    }

    @Test
    @DisplayName("특수문자가 포함된 (BTS) 검색어가 안전하게 정제되어 '+BTS' 로 전달된다")
    fun search_sanitizesSpecialCharactersInKeyword() {
        `when`(entityManager.createNativeQuery(anyString(), eq(Concert::class.java)))
            .thenReturn(nativeQuery)
        `when`(nativeQuery.setParameter(eq("keyword"), any())).thenReturn(nativeQuery)
        `when`(nativeQuery.resultList).thenReturn(mutableListOf<Any?>())

        repository.findByKeyword("(BTS)")

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(nativeQuery).setParameter(eq("keyword"), captor.capture() as Any?)
        assertThat(captor.value).isEqualTo("+BTS")
    }

    @Test
    @DisplayName("다중 단어 검색어는 각 단어마다 '+단어' 로 조합되어 전달된다")
    fun search_multiWordKeyword_combinesEachToken() {
        `when`(entityManager.createNativeQuery(anyString(), eq(Concert::class.java)))
            .thenReturn(nativeQuery)
        `when`(nativeQuery.setParameter(eq("keyword"), any())).thenReturn(nativeQuery)
        `when`(nativeQuery.resultList).thenReturn(mutableListOf<Any?>())

        repository.findByKeyword("아이유  서울")

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(nativeQuery).setParameter(eq("keyword"), captor.capture() as Any?)
        assertThat(captor.value).isEqualTo("+아이유 +서울")
    }
}
