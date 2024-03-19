package cat.moki.acute.utils

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import cat.moki.acute.AcuteApplication
import com.google.gson.Gson
import java.util.regex.Pattern

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

fun Long.formatMS(): String {
    return (this / 1000).toInt().formatSecond()
}

fun String.extractComplexMediaId(): Pair<String, String> {
    val values = this.split("/", limit = 2)
    return Pair(values[0], if (values.size == 1) "" else values[1])
}

//fun String.isAlbum(): Boolean {
//    return this.startsWith("al-")
//}
//
//fun String.isSong(): Boolean {
//    return this.startsWith("tr-")
//}

@Composable
fun Modifier.conditional(condition: Boolean, modifier: @Composable Modifier.() -> Modifier) =
    then(if (condition) modifier.invoke(this) else this)

val pattern: Pattern = Pattern.compile("(.+)([({\\[（［｛⟨<〈⟪《【〖〔「『~][^({\\[（［｛⟨<〈⟪《【〖〔「『)}\\]）］｝⟩>〉⟫》】〗〕」』~]+[)}\\]）］｝⟩>〉⟫》】〗〕」』~])$")

val gson = Gson()