package com.back.global.util

/**
 * 한글 미완성 음절(받침 없는 음절) 및 독립 초성(자음) 자소 범위 계산 유틸리티
 */
object HangulSearchUtils {

    private val CHOSUNG_LIST = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    /**
     * 키워드의 마지막 글자가
     * 1) 독립 초성 자음(예: 'ㅊ' -> [차-칳])이거나
     * 2) 받침 없는 조합중 음절(예: '처' -> [처-첳])인 경우
     * 자소 범위 REGEXP 패턴(예: "김[차-칳]", "김[처-첳]")을 생성한다.
     * 완성형 음절이거나 한글이 아닌 경우 null을 반환한다.
     */
    fun makeHangulIncompleteRegex(keyword: String): String? {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) return null
        val lastChar = trimmed.last()
        val prefix = trimmed.substring(0, trimmed.length - 1)

        // Case 1: 독립 초성 자음인 경우 (예: 'ㅊ', 'ㄱ', 'ㄴ') -> '차'~'칗' 범위 생성
        val chosungIndex = CHOSUNG_LIST.indexOf(lastChar)
        if (chosungIndex != -1) {
            val startChar = (0xAC00 + chosungIndex * 588).toChar()
            val endChar = (startChar.code + 587).toChar()
            return "$prefix[$startChar-$endChar]"
        }

        // Case 2: 받침이 없는 완성중 음절인 경우 (예: '처', '가', '이') -> '처'~'첳' 범위 생성
        if (lastChar in '\uAC00'..'\uD7A3') {
            val unicodeIndex = lastChar.code - 0xAC00
            val jongsung = unicodeIndex % 28
            if (jongsung == 0) {
                val startChar = lastChar
                val endChar = (lastChar.code + 27).toChar()
                return "$prefix[$startChar-$endChar]"
            }
        }
        return null
    }
}
