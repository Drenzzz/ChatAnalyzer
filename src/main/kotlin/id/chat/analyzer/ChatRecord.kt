package id.chat.analyzer

data class ChatRecord(
    val message: String,
    val occurredAtUtc: String,
) {
    fun toJson(): String = "{\"message\":\"${message.jsonEscape()}\",\"occurred_at_utc\":\"${occurredAtUtc.jsonEscape()}\"}"
}

private fun String.jsonEscape(): String = buildString(length + 16) {
    for (character in this@jsonEscape) {
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> if (character.code < 0x20) append("\\u%04x".format(character.code)) else append(character)
        }
    }
}
