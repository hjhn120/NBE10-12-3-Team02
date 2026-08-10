package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * MroongaConcertSearchRepository 단위 테스트.
 * MySQL Mroonga FULLTEXT 쿼리가 올바른 파라미터로 실행되는지 검증한다.
 * (실제 MySQL 연결 없이 EntityManager를 모킹하여 쿼리 파라미터 전달 로직만 검증)
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("MroongaConcertSearchRepository MATCH/AGAINST 쿼리 파라미터 단위 테스트")
class MroongaConcertSearchRepositoryTest {

    @Mock
    private lateinit var entityManager: EntityManager

    @Mock
    private lateinit var nativeQuery: jakarta.persistence.Query

    @Mock
    private lateinit var jpqlQuery: TypedQuery<Concert>

    private lateinit var repository: MroongaConcertSearchRepository

    @BeforeEach
    fun setUp() {
        repository = MroongaConcertSearchRepository(entityManager)
    }

    @Test
    @DisplayName("keyword가 null이면 전체 조회 JPQL이 실행된다")
    fun findAll_whenKeywordIsNull() {
        `when`(entityManager.createQuery(any(String::class.java), eq(Concert::class.java)))
            .thenReturn(jpqlQuery)
        `when`(jpqlQuery.resultList).thenReturn(emptyList())

        repository.findByKeyword(null)

        verify(entityManager).createQuery("SELECT c FROM Concert c", Concert::class.java)
    }

    @Test
    @DisplayName("keyword가 빈 문자열이면 전체 조회 JPQL이 실행된다")
    fun findAll_whenKeywordIsBlank() {
        `when`(entityManager.createQuery(any(String::class.java), eq(Concert::class.java)))
            .thenReturn(jpqlQuery)
        `when`(jpqlQuery.resultList).thenReturn(emptyList())

        repository.findByKeyword("   ")

        verify(entityManager).createQuery("SELECT c FROM Concert c", Concert::class.java)
    }

    @Test
    @DisplayName("keyword가 있으면 MATCH/AGAINST 네이티브 쿼리가 '+keyword*' Boolean Mode 형식으로 실행된다")
    fun search_withKeyword_usesBooleanModePrefix() {
        `when`(entityManager.createNativeQuery(any(String::class.java), eq(Concert::class.java)))
            .thenReturn(nativeQuery)
        `when`(nativeQuery.setParameter(any(String::class.java), any())).thenReturn(nativeQuery)
        `when`(nativeQuery.resultList).thenReturn(mutableListOf<Any?>())

        repository.findByKeyword("서울")

        // '+서울*' Boolean Mode 전방 일치 파라미터가 전달되는지 검증
        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(nativeQuery).setParameter(eq("keyword"), captor.capture() as Any?)
        assertThat(captor.value).isEqualTo("+서울*")
    }

    @Test
    @DisplayName("영어 키워드도 '+keyword*' Boolean Mode 형식으로 전달된다")
    fun search_withEnglishKeyword_usesBooleanModePrefix() {
        `when`(entityManager.createNativeQuery(any(String::class.java), eq(Concert::class.java)))
            .thenReturn(nativeQuery)
        `when`(nativeQuery.setParameter(any(String::class.java), any())).thenReturn(nativeQuery)
        `when`(nativeQuery.resultList).thenReturn(mutableListOf<Any?>())

        repository.findByKeyword("BTS")

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(nativeQuery).setParameter(eq("keyword"), captor.capture() as Any?)
        assertThat(captor.value).isEqualTo("+BTS*")
    }

    @Test
    @DisplayName("keyword 앞뒤 공백은 trim 처리 후 Boolean Mode 형식으로 전달된다")
    fun search_trimsKeywordWhitespace() {
        `when`(entityManager.createNativeQuery(any(String::class.java), eq(Concert::class.java)))
            .thenReturn(nativeQuery)
        `when`(nativeQuery.setParameter(any(String::class.java), any())).thenReturn(nativeQuery)
        `when`(nativeQuery.resultList).thenReturn(mutableListOf<Any?>())

        repository.findByKeyword("  Coldplay  ")

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(nativeQuery).setParameter(eq("keyword"), captor.capture() as Any?)
        assertThat(captor.value).isEqualTo("+Coldplay*")
    }
}
