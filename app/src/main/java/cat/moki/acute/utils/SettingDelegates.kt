package cat.moki.acute.utils

import android.content.Context
import android.util.Log
import cat.moki.acute.AcuteApplication
import kotlin.reflect.KProperty

val SettingString = "setting"

class DelegateString() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        val value = AcuteApplication.application.context.getSharedPreferences(SettingString, Context.MODE_PRIVATE)
            .getString(property.name, "")!!
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {

        AcuteApplication.application.context.getSharedPreferences(SettingString, Context.MODE_PRIVATE)
            .edit().putString(property.name, value).apply()
    }
}

class DelegateBoolean() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        val value = AcuteApplication.application.context.getSharedPreferences(SettingString, Context.MODE_PRIVATE)
            .getBoolean(property.name, false)
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {

        AcuteApplication.application.context.getSharedPreferences(SettingString, Context.MODE_PRIVATE)
            .edit().putBoolean(property.name, value).apply()
    }
}

class DelegateInteger() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        val value = AcuteApplication.application.context.getSharedPreferences(SettingString, Context.MODE_PRIVATE)
            .getInt(property.name, Int.MIN_VALUE)
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {

        AcuteApplication.application.context.getSharedPreferences(SettingString, Context.MODE_PRIVATE)
            .edit().putInt(property.name, value).apply()
    }
}
