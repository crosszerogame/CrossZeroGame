package ru.geekbrains.android2.crosszerogame.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsImpl(context: Context) : Settings {
    companion object {
        private const val NAME = "Settings"
        private const val BEGIN_AS_FIRST = "BeginAsFirst"
        private const val FIELD_SIZE = "FieldSize"
        private const val NICK = "Nick"
        private const val GAME_LEVEL = "GameLevel"
        private const val DEFAULT_BEGIN_AS_FIRST = true
        private const val DEFAULT_FIELD_SIZE = 0
        private const val DEFAULT_NICK = ""
        private const val DEFAULT_GAME_LEVEL = 2
    }

    private var isChanged = false
    private val pref: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor by lazy {
        pref.edit()
    }

    override fun save() {
        if (isChanged) {
            editor.apply()
            isChanged = false
        }
    }

    override fun getBeginAsFirst(): Boolean =
        pref.getBoolean(BEGIN_AS_FIRST, DEFAULT_BEGIN_AS_FIRST)

    override fun setBeginAsFirst(value: Boolean) {
        editor.putBoolean(BEGIN_AS_FIRST, value)
        isChanged = true
    }

    override fun getFieldSize(): Int =
        pref.getInt(FIELD_SIZE, DEFAULT_FIELD_SIZE)

    override fun setFieldSize(value: Int) {
        editor.putInt(FIELD_SIZE, value)
        isChanged = true
    }

    override fun getNick(): String =
        pref.getString(NICK, DEFAULT_NICK) ?: DEFAULT_NICK

    override fun setNick(value: String) {
        editor.putString(NICK, value)
        isChanged = true
    }

    override fun getGameLevel(): Int =
        pref.getInt(GAME_LEVEL, DEFAULT_GAME_LEVEL)

    override fun setGameLevel(value: Int) {
        editor.putInt(GAME_LEVEL, value)
        isChanged = true
    }
}