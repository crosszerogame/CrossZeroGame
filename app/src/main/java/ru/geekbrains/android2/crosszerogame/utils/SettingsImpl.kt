package ru.geekbrains.android2.crosszerogame.utils

import android.content.Context
import android.content.SharedPreferences
import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer

class SettingsImpl(context: Context) : Settings {
    companion object {
        private const val NAME = "Settings"
        private const val FIELD_SIZE = "FieldSize"
        private const val NICK = "Nick"
        private const val GAME_LEVEL = "GameLevel"
        private const val KEY_GAMER = "KeyGamer"
        private const val CHIP_IMAGE_ID = "ChipImageId"
        private const val TIME_FOR_TURN = "TimeForTurn"
        private const val KEY_OPPONENT = "KeyOpponent"
        private const val KEY_GAME = "KeyGame"
        private const val IS_ONLINE = "IsOnLine"
        private const val IS_FIRST = "IsFirst"
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
        pref.getBoolean(IS_FIRST, GameConstants.DEFAULT_IS_FIRST)

    override fun setBeginAsFirst(value: Boolean) {
        editor.putBoolean(IS_FIRST, value)
        isChanged = true
    }

    override fun getOnline(): Boolean =
        pref.getBoolean(IS_ONLINE, false)

    override fun setOnline(value: Boolean) {
        editor.putBoolean(IS_ONLINE, value)
        isChanged = true
    }

    override fun getFieldSize(): Int =
        pref.getInt(FIELD_SIZE, GameConstants.DEFAULT_FIELD_SIZE)

    override fun setFieldSize(value: Int) {
        editor.putInt(FIELD_SIZE, value)
        isChanged = true
    }

    override fun getNick(): String =
        pref.getString(NICK, GameConstants.DEFAULT_NICK_GAMER) ?: GameConstants.DEFAULT_NICK_GAMER

    override fun setNick(value: String) {
        editor.putString(NICK, value)
        isChanged = true
    }

    override fun getGameLevel(): GameConstants.GameLevel =
        GameConstants.GameLevel.values()[pref.getInt(GAME_LEVEL, 0)]

    override fun setGameLevel(value: GameConstants.GameLevel) {
        editor.putInt(GAME_LEVEL, value.ordinal)
        isChanged = true
    }

    override fun getGamer() =
        Gamer(
            keyGamer = pref.getString(KEY_GAMER, "") ?: "",
            nikGamer = pref.getString(NICK, GameConstants.DEFAULT_NICK_GAMER)
                ?: GameConstants.DEFAULT_NICK_GAMER,
            gameFieldSize = pref.getInt(FIELD_SIZE, GameConstants.DEFAULT_FIELD_SIZE),
            levelGamer = GameConstants.GameLevel.values()[pref.getInt(GAME_LEVEL, 0)],
            chipImageId = pref.getInt(CHIP_IMAGE_ID, 0),
            timeForTurn = pref.getInt(TIME_FOR_TURN, GameConstants.DEFAULT_TIME_FOR_TURN),
            keyOpponent = pref.getString(KEY_OPPONENT, "") ?: "",
            keyGame = pref.getString(KEY_GAME, "") ?: "",
            isOnLine = pref.getBoolean(IS_ONLINE, false),
            isFirst = pref.getBoolean(IS_FIRST, GameConstants.DEFAULT_IS_FIRST)
        )

    override fun setGamer(value: Gamer) {
        editor.putString(KEY_GAMER, value.keyGamer)
        editor.putString(NICK, value.nikGamer)
        editor.putInt(FIELD_SIZE, value.gameFieldSize)
        editor.putInt(GAME_LEVEL, value.levelGamer.ordinal)
        editor.putInt(CHIP_IMAGE_ID, value.chipImageId)
        editor.putInt(TIME_FOR_TURN, value.timeForTurn)
        editor.putString(KEY_OPPONENT, value.keyOpponent)
        editor.putString(KEY_GAME, value.keyGame)
        editor.putBoolean(IS_ONLINE, value.isOnLine)
        editor.putBoolean(IS_FIRST, value.isFirst)
        isChanged = true
    }
}