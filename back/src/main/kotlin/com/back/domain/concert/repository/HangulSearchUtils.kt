package com.back.domain.concert.repository

/**
 * 한글 미완성 음절(받침이 없는 조합중인 음절) 자소 범위 계산 및 REGEXP 패턴 생성 유틸리티
 */
object HangulSearchUtils {

    /**
     * 키워드의 마지막 글자가 받침이 없는 한글 음절(예: '처', '가', '이')인 경우
     * 'ㅎ' 받침까지의 범위 정규식(예: "김[처-첨]", "[가-갛]")을 생성한다.
     * 이미 받침이 완성된 음절('천', '울')이거나 한글이 아닌 경우 null을 반환한다.
     */
    fun makeHangulIncompleteRegex(keyword: String): String? {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) return null
        val lastChar = trimmed.last()

        // 한글 가(\uAC00) ~ 힣(\uD7A3) 범위 확인
        if (lastChar in '\uAC00'..'\uD7A3') {
            val unicodeIndex = lastChar.code - 0xAC00
            val jongsung = unicodeIndex % 28
            if (jongsung == 0) { // 받침이 없는 음절 (종성 0)
                val prefix = trimmed.substring(0, trimmed.length - 1)
                val startChar = lastChar
                val endChar = (lastChar.code + 27).toChar() // 종성 27 ('ㅎ' 받침)
                return "$prefix[$startChar-$endChar]"
            }
        }
        return null
    }
}
