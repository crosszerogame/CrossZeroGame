package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ru.geekbrains.android2.crosszerogame.game.GameRepositoryImpl
import ru.geekbrains.android2.crosszerogame.structure.GameRepository
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.Player
import ru.geekbrains.android2.crosszerogame.utils.Settings
import ru.geekbrains.android2.crosszerogame.utils.strings.SettingsStrings

class SettingsModel : ViewModel() {
    enum class Tab {
        SINGLE, REMOTE_CREATE, REMOTE_CONNECT
    }

    companion object {
        private const val SHIFT_SIZE = 3
        private const val SHIFT_LEVEL = 1
        private const val MIN_LENGTH_NICK = 3
        private val DEFAULT_TAB = Tab.SINGLE
    }

    private val repository: GameRepository = GameRepositoryImpl()
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
        settings.save()
        super.onCleared()
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
            val game1 = Game(
                id = 1,
                fieldSize = 3,
                chipsForWin = 3,
                level = 3,
                playerCross = Player(
                    id = 1,
                    nick = "Nick1",
                    lastTimeActive = 0
                ),
                playerZero = null
            )
            val game2 = Game(
                id = 2,
                fieldSize = 4,
                chipsForWin = 3,
                level = 4,
                playerCross = null,
                playerZero = Player(
                    id = 2,
                    nick = "Nick2",
                    lastTimeActive = 0
                )
            )
            val game3 = Game(
                id = 3,
                fieldSize = 6,
                chipsForWin = 4,
                level = 2,
                playerCross = null,
                playerZero = Player(
                    id = 3,
                    nick = "Nick3",
                    lastTimeActive = 0
                )
            )
            val game4 = Game(
                id = 4,
                fieldSize = 9,
                chipsForWin = 4,
                level = 1,
                playerCross = Player(
                    id = 4,
                    nick = "Nick4Nick4Nick4Nick4",
                    lastTimeActive = 0
                ),
                playerZero = null
            )
            _state.postValue(
                SettingsState.Games(
                    listOf(game1, game2, game3, game4)
                )
            )
        }
    }

    fun checkNick(nick: String) {
        //TODO проверить на сервере доступность ника через 2 секунды после запуска
        settings.setNick(nick)
        nickJob?.cancel()
        nickJob = scope.launch {
            delay(2000)
            if (nick.length < MIN_LENGTH_NICK || nick.contains(" "))
                _state.postValue(SettingsState.UnavailableNick)
            else
                _state.postValue(SettingsState.AvailableNick)
        }
    }

    fun setFirst(value: Boolean) {
        settings.setBeginAsFirst(value)
    }
}