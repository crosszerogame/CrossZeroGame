package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ru.geekbrains.android2.crosszerogame.App.Companion.gr
import ru.geekbrains.android2.crosszerogame.App.Companion.grAi
import ru.geekbrains.android2.crosszerogame.utils.Settings
import ru.geekbrains.android2.crosszerogame.utils.strings.SettingsStrings
import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer
import java.util.regex.Pattern

class SettingsModel : ViewModel() {
    enum class Tab {
        SINGLE, REMOTE_CONNECT
    }

    companion object {
        const val SHIFT_LEVEL = 1
        private const val MIN_LENGTH_NICK = 3
        private const val MAX_LENGTH_NICK = 20
        private const val TIME_FOR_FILTER = 1000L
        private val DEFAULT_TAB = Tab.SINGLE
        private const val NICK_FORMAT = "^[\\w\\s]+\$"
    }


    private lateinit var strings: SettingsStrings
    private lateinit var settings: Settings
    private val _state: MutableLiveData<SettingsState> = MutableLiveData()
    val state: LiveData<SettingsState> = _state
    private var nickJob: Job? = null
    var tab: Tab = DEFAULT_TAB

    private val scope = CoroutineScope(
        Dispatchers.Main
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
        scope.launch {
            _state.postValue(
                SettingsState.Settings(
                    gamer = initGamer()
                )
            )
        }
    }

    private suspend fun initGamer(): Gamer {
        var gamer = settings.getGamer()
        if (gamer.isOnLine)
            gamer = gr.gamer(
                gamer
            ) else {
            gamer = gr.gamer(
                gamer
            )
            gamer = grAi.gamer(
                gamer
            )
        }
        settings.setGamer(gamer)
        return gamer
    }

    fun launchGame(opponent: Gamer? = null) {
        scope.launch {
            settings.save()
            GameModel.launchGame(GameParameters.Launch(initGamer(), opponent))
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    fun getFieldSizeString(value: Int): String {
        settings.setFieldSize(value)
        return String.format(strings.fieldSizeFormat, value, value, grAi.dotsToWin(value).second)
    }

    fun getGameLevelString(value: Int): String {
        settings.setGameLevel(GameConstants.GameLevel.values()[value])
        val level = value + SHIFT_LEVEL
        return String.format(strings.gameLevelFormat, level)
    }

    fun loadOpponents() {
        scope.launch {
            _state.postValue(SettingsState.Opponents(gr.opponentsList()))
        }
    }

    fun checkNick(nick: String) {
        //TODO проверить на сервере доступность ника через 2 секунды после запуска
        nickJob?.cancel()
        nickJob = scope.launch {
            delay(TIME_FOR_FILTER)
            if (nick.length < MIN_LENGTH_NICK || nick.length > MAX_LENGTH_NICK)
                _state.postValue(SettingsState.UnavailableNick)
            else {
                val pattern = Pattern.compile(NICK_FORMAT)
                val m = pattern.matcher(nick)
                if (m.find()) {
                    settings.setNick(nick)
                    _state.postValue(SettingsState.AvailableNick)
                } else
                    _state.postValue(SettingsState.UnavailableNick)
            }
        }
    }

    fun setFirst(value: Boolean) {
        settings.setBeginAsFirst(value)
    }

    fun setOnline(value: Boolean) {
        settings.setOnline(value)
    }
}