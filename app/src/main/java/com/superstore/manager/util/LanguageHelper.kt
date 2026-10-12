package com.superstore.manager.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {
    const val LANG_ENGLISH = "en"
    const val LANG_URDU = "ur"

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun isUrdu(languageCode: String): Boolean = languageCode == LANG_URDU
}
