package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ru.geekbrains.android2.crosszerogame.App
import ru.geekbrains.android2.crosszerogame.App.Companion.gr
import ru.geekbrains.android2.crosszerogame.utils.Settings
import ru.geekbrains.android2.crosszerogame.utils.strings.SettingsStrings
import java.util.regex.Pattern

class SettingsModel : ViewModel() {
    enum class Tab {
        SINGLE, REMOTE_CREATE, REMOTE_CONNECT
    }

    companion object {
        private const val SHIFT_SIZE = 3
        private const val SHIFT_LEVEL = 1
        private const val SHIFT_TIME = 10
        private const val MIN_LENGTH_NICK = 3
        private const val MAX_LENGTH_NICK = 20
        private const val TIME_FOR_FILTER = 2000L
        private const val DEFAULT_SEC_FOR_MOVE = 20
        private val DEFAULT_TAB = Tab.SINGLE
        private const val NICK_FORMAT = "^[\\w\\s]+\$"
    }

    private lateinit var strings: SettingsStrings
    private lateinit var settings: Settings
    private val _state: MutableLiveData<SettingsState> = MutableLiveData()
    val state: LiveData<SettingsState> = _state
    private var nickJob: Job? = null
    var tab: Tab = DEFAULT_TAB
    private var fieldSize: Int = 0
    var isCalcMoveTime: Boolean = false
        private set
    private var isLoad = true

    private val scope = CoroutineScope(
        Dispatchers.Default
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    private fun handleError(error: Throwable) {
        println("Error SettingsModel:")
        error.printStackTrace()
        _state.postValue(SettingsState.Error(error))
    }

    fun init(settings: Settings, strings: SettingsStrings) {
        this.settings = settings
        this.strings = strings
        fieldSize = settings.getFieldSize()

        val time = if (settings.isCalMoveTime()) {
            isCalcMoveTime = true
            calcMoveTime() - SHIFT_TIME
        } else {
            isCalcMoveTime = false
            settings.getMoveTime()
        }

        _state.postValue(
            SettingsState.Settings(
                beginAsFirst = settings.getBeginAsFirst(),
                fieldSize = fieldSize,
                nick = settings.getNick(),
                gameLevel = settings.getGameLevel(),
                moveTime = time
            )
        )
    }

    private fun calcMoveTime(): Int =
        DEFAULT_SEC_FOR_MOVE + fieldSize * 3

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    fun save() {
        settings.save()
    }

    fun getFieldSizeString(value: Int): String {
        if (!isLoad)
            settings.setFieldSize(value)
        fieldSize = value
        if (!isLoad && isCalcMoveTime)
            _state.postValue(SettingsState.MoveTime(calcMoveTime()))
        val size = value + SHIFT_SIZE
        val chipsForWin = App.grAi.dotsToWin(size).second
        return String.format(strings.fieldSizeFormat, size, size, chipsForWin)
    }

    fun getGameLevelString(value: Int): String {
        if (!isLoad)
            settings.setGameLevel(value)
        val level = value + SHIFT_LEVEL
        return String.format(strings.gameLevelFormat, level)
    }

    fun getMoveTimeString(value: Int): String {
        if (isLoad)
            isLoad = false
        else if (!isCalcMoveTime)
            settings.setMoveTime(value)
        val time = value + SHIFT_TIME
        return String.format(strings.moveTimeFormat, time)
    }

    fun launchGame(fieldSize: Int, beginAsFirst: Boolean) {
        GameModel.launchGame(
            GameParameters.SingleLaunch(fieldSize + SHIFT_SIZE, beginAsFirst)
        )
    }

    fun launchGame(
        nick: String,
        waitZero: Boolean,
        fieldSize: Int,
        level: Int,
        timeForTurn: Int
    ) {
        GameModel.launchGame(
            GameParameters.RemoteLaunch(
                nick = nick,
                waitZero = waitZero,
                fieldSize = fieldSize + SHIFT_SIZE,
                chipsForWin = App.grAi.dotsToWin(fieldSize).second,
                level = level + SHIFT_LEVEL,
                timeForTurn = timeForTurn + SHIFT_TIME
            )
        )
    }

    fun launchGame(
        keyOpponent: String,
        beginAsFirst: Boolean,
        nikOpponent: String,
        levelOpponent: Int
    ) {
        GameModel.launchGame(
            GameParameters.RemoteConnect(
                keyOpponent = keyOpponent,
                beginAsFirst = beginAsFirst,
                nikOpponent = nikOpponent,
                levelOpponent = levelOpponent
            )
        )
    }

    fun loadOpponents() {
        scope.launch {
            _state.postValue(SettingsState.Opponents(gr.opponentsList()))
        }
    }

    fun checkNick(nick: String) {
        nickJob?.cancel()
        nickJob = scope.launch {
            delay(TIME_FOR_FILTER)
            if (nick.length < MIN_LENGTH_NICK || nick.length > MAX_LENGTH_NICK)
                _state.postValue(SettingsState.NewNick(nick, false))
            else {
                val pattern = Pattern.compile(NICK_FORMAT)
                val m = pattern.matcher(nick)
                if (m.find()) {
                    //TODO проверить на сервере доступность ника через 2 секунды после запуска
                    _state.postValue(SettingsState.NewNick(nick, true))
                    settings.setNick(nick)
                } else
                    _state.postValue(SettingsState.NewNick(nick, false))
            }
        }
    }

    fun setFirst(value: Boolean) {
        settings.setBeginAsFirst(value)
    }

    fun switchCalcMoveTime(isOn: Boolean) {
        isCalcMoveTime = isOn
        val time = calcMoveTime()
        if (isCalcMoveTime) {
            settings.onCalcMoveTime()
            _state.postValue(SettingsState.MoveTime(time))
        } else
            settings.setMoveTime(time - SHIFT_TIME)
    }
}