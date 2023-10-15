package cat.moki.acute

fun Int.formatSecond(): String {
    val h: Int = this / 3600
    val m: Int = this % 3600 / 60
    val s: Int = this % 60
    val result: String = if (h > 0) {
        "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m}:${s.toString().padStart(2, '0')}"
    }
    return result
}

fun String.extractComplexMediaId(): Pair<String, String> {
    val values = this.split("/")
    return Pair(values[0], values[1])
}

fun String.isAlbum(): Boolean {
    return this.startsWith("al-")
}

fun String.isSong(): Boolean {
    return this.startsWith("tr-")
}