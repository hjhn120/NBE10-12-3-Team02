package com.back.global.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * HangulSearchUtils 단위 테스트.
 * 독립 초성 및 미완성 받침 음절에 대한 자소 범위 REGEXP 패턴 생성 로직을 검증한다.
 */
@DisplayName("HangulSearchUtils 한글 자소 범위 REGEXP 생성 단위 테스트")
class HangulSearchUtilsTest {

    @Nested
    @DisplayName("독립 초성 자음 입력 케이스")
    inner class ChosungCases {

        @Test
        @DisplayName("독립 초성 ㅊ -> [차-칳] 범위 패턴 생성")
        fun chosung_ㅊ_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("김ㅊ")
            assertThat(result).isEqualTo("김[차-칳]")
        }

        @Test
        @DisplayName("독립 초성 ㄱ -> [가-깋] 범위 패턴 생성")
        fun chosung_ㄱ_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("서ㄱ")
            assertThat(result).isEqualTo("서[가-깋]")
        }

        @Test
        @DisplayName("독립 초성 ㅎ -> [하-힣] 범위 패턴 생성")
        fun chosung_ㅎ_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("ㅎ")
            assertThat(result).isEqualTo("[하-힣]")
        }

        @Test
        @DisplayName("단독 초성 ㅅ -> [사-싷] 범위 패턴 생성")
        fun chosung_ㅅ_alone_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("ㅅ")
            assertThat(result).isEqualTo("[사-싷]")
        }
    }

    @Nested
    @DisplayName("받침 없는 미완성 음절 입력 케이스")
    inner class IncompleteSyllableCases {

        @Test
        @DisplayName("미완성 음절 처 -> [처-첳] 범위 패턴 생성")
        fun incompleteSyllable_처_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("김처")
            assertThat(result).isEqualTo("김[처-첳]")
        }

        @Test
        @DisplayName("미완성 음절 서 -> [서-섷] 범위 패턴 생성")
        fun incompleteSyllable_서_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("서")
            assertThat(result).isEqualTo("[서-섷]")
        }

        @Test
        @DisplayName("미완성 음절 가 -> [가-갛] 범위 패턴 생성")
        fun incompleteSyllable_가_generatesCorrectRange() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("가")
            assertThat(result).isEqualTo("[가-갛]")
        }
    }

    @Nested
    @DisplayName("REGEXP 생성 불가 케이스 (null 반환)")
    inner class NullCases {

        @Test
        @DisplayName("완성형 음절(받침 있는 글자: 천)이면 null 반환")
        fun completeSyllable_returnsNull() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("김천")
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("영문 알파벳이면 null 반환")
        fun englishChar_returnsNull() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("BTS")
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("숫자이면 null 반환")
        fun digit_returnsNull() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("2024")
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("공백만 있으면 null 반환")
        fun blankKeyword_returnsNull() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("   ")
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("한영 혼합이지만 마지막 글자가 완성형 한글이면 null 반환")
        fun mixedKeyword_lastCharComplete_returnsNull() {
            val result = HangulSearchUtils.makeHangulIncompleteRegex("BTS 서울")
            assertThat(result).isNull()
        }
    }
}
