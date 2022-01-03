package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.geekbrains.android2.crosszerogame.game.GameManagerImpl
import ru.geekbrains.android2.crosszerogame.game.GameRepositoryImpl
import ru.geekbrains.android2.crosszerogame.structure.GameManager
import ru.geekbrains.android2.crosszerogame.structure.GameRepository
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
        private const val MIN_LENGTH_NICK = 3
        private const val MAX_LENGTH_NICK = 20
        private const val TIME_FOR_FILTER = 2000L
        private val DEFAULT_TAB = Tab.SINGLE
        private const val NICK_FORMAT = "^[\\w\\s]+\$"
    }

    private val repository: GameRepository = GameRepositoryImpl()
    private val manager: GameManager = GameManagerImpl(repository)
    private lateinit var strings: SettingsStrings
    private lateinit var settings: Settings
    private val _state: MutableLiveData<SettingsState> = MutableLiveData()
    val state: LiveData<SettingsState> = _state
    private var nickJob: Job? = null
    var tab: Tab = DEFAULT_TAB

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
        _state.postValue(
            SettingsState.Settings(
                beginAsFirst = settings.getBeginAsFirst(),
                fieldSize = settings.getFieldSize(),
                nick = settings.getNick(),
                gameLevel = settings.getGameLevel()
            )
        )
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    fun save() {
        settings.save()
    }

    fun getFieldSizeString(value: Int): String {
        settings.setFieldSize(value)
        val size = value + SHIFT_SIZE
        val chipsForWin = repository.getChipsForWin(size)
        return String.format(strings.fieldSizeFormat, size, size, chipsForWin)
    }

    fun getGameLevelString(value: Int): String {
        settings.setGameLevel(value)
        val level = value + SHIFT_LEVEL
        return String.format(strings.gameLevelFormat, level)
    }

    fun launchGame(fieldSize: Int, beginAsFirst: Boolean) {
        GameModel.launchGame(
            GameParameters.SingleLaunch(fieldSize + SHIFT_SIZE, beginAsFirst)
        )
    }

    fun launchGame(
        fieldSize: Int, beginAsFirst: Boolean, nick: String, level: Int
    ) {
        val chipsForWin = repository.getChipsForWin(fieldSize)
        GameModel.launchGame(
            GameParameters.RemoteLaunch(
                fieldSize = fieldSize + SHIFT_SIZE,
                chipsForWin = chipsForWin,
                beginAsFirst = beginAsFirst,
                nick = nick,
                level = level
            )
        )
    }

    fun launchGame(nick: String, idGame: Int) {
        GameModel.launchGame(
            GameParameters.RemoteConnect(nick, idGame)
        )
    }

    fun loadGames() {
        scope.launch {
            manager.getGames().collect {
                _state.postValue(SettingsState.Games(it))
            }
        }
    }

    fun checkNick(nick: String) {
        //TODO проверить на сервере доступность ника через 2 секунды после запуска
        settings.setNick(nick)
        nickJob?.cancel()
        nickJob = scope.launch {
            delay(TIME_FOR_FILTER)
            if (nick.length < MIN_LENGTH_NICK || nick.length > MAX_LENGTH_NICK)
                _state.postValue(SettingsState.UnavailableNick)
            else {
                val pattern = Pattern.compile(NICK_FORMAT)
                val m = pattern.matcher(nick)
                if (m.find())
                    _state.postValue(SettingsState.AvailableNick)
                else
                    _state.postValue(SettingsState.UnavailableNick)
            }
        }
    }

    fun setFirst(value: Boolean) {
        settings.setBeginAsFirst(value)
    }
}