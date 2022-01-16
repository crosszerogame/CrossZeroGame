package ru.geekbrains.android2.crosszerogame.game

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import ru.geekbrains.android2.crosszerogame.game.remote.RemoteDataBaseImpl
import ru.geekbrains.android2.crosszerogame.game.opponents.AiOpponent
import ru.geekbrains.android2.crosszerogame.game.opponents.NullOpponent
import ru.geekbrains.android2.crosszerogame.game.opponents.RemoteOpponent
import ru.geekbrains.android2.crosszerogame.structure.*
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

class GameManagerImpl(private val repository: GameRepository) : GameManager {
    private val _state = MutableStateFlow<GameManager.State>(GameManager.State.Created)
    override val state: Flow<GameManager.State>
        get() = _state
    override var lastState: GameManager.State = GameManager.State.Created
        private set
    override var gameIsFinish = true
        private set
    private var opponent: Opponent = NullOpponent()
    private var player: Player? = null
    private var iIsCross = false
    private var isPlayerMove = false

    private val client: GameClient by lazy {
        GameClientImpl(RemoteDataBaseImpl())
    }

    override suspend fun createSingleGame(fieldSize: Int, iIsCross: Boolean) {
        this.iIsCross = iIsCross
        createGame(fieldSize, AiOpponent(fieldSize, !iIsCross))
    }

    override suspend fun createRemoteGame(game: Game) {
        iIsCross = game.playerZero == null
        val opponent = RemoteOpponent(game, iIsCross, client)
        createGame(game.fieldSize, opponent)
    }

    override suspend fun connectTo(game: Game, player: Player) {
        iIsCross = if (game.playerCross == null) {
            game.playerCross = player
            true
        } else {
            game.playerZero = player
            false
        }
        val opponent = RemoteOpponent(game, iIsCross, client)
        createGame(game.fieldSize, opponent)
    }

    private suspend fun createGame(fieldSize: Int, opponent: Opponent) {
        gameIsFinish = true
        isPlayerMove = false
        repository.newGame(fieldSize)
        this.opponent = opponent
        opponent.preparing().collect {
            updatePlayer(it)
            if (it != null) {
                isPlayerMove = iIsCross
                if (!isPlayerMove)
                    waitOpponentMove()
            }
            return@collect
        }
    }

    private suspend fun updatePlayer(player: Player?) {
        if (player == null) {
            emitState(GameManager.State.Error(ConnectionLostError))
            return
        }
        this.player = player
        val state = when (player.state) {
            Player.State.CREATED -> {
                if (gameIsFinish.not()) return
                gameIsFinish = false
                GameManager.State.Ready(player.nick)
            }
            Player.State.PLAYING -> {
                if (gameIsFinish) {
                    //в случае быстрого хода Player.State.CREATED может не успеть сработать
                    gameIsFinish = false
                    GameManager.State.Ready(player.nick)
                    delay(100)
                }
                pasteChip(player.moveX, player.moveY, false)
            }
            Player.State.LEFT ->
                abortedGame()
        }
        emitState(state)
    }

    private suspend fun waitOpponentMove() {
        opponent.waitMove().collect {
            updatePlayer(it)
        }
    }

    private fun emitState(value: GameManager.State) {
        _state.tryEmit(value)
        lastState = value
    }

    override fun getGames(): Flow<List<Game>> = client.loadGamesList().map {
        //TODO remove it when chipForWin is defined in loadGamesList
        for (game in it) {
            game.chipsForWin = repository.getChipsForWin(game.fieldSize)
        }
        it
    }

    private fun abortedGame(): GameManager.State {
        gameIsFinish = true
        return GameManager.State.AbortedGame
    }

    override suspend fun finishGame() {
        if (gameIsFinish) return
        gameIsFinish = true
        opponent.sendBye()
    }

    override fun getCell(x: Int, y: Int) = repository.getCell(x, y)

    override suspend fun doMove(x: Int, y: Int) {
        if (isPlayerMove.not() || opponent.IsReady.not()) {
            emitState(GameManager.State.Move(x, y, iIsCross, MoveResult.CANCEL))
            return
        }
        val move = pasteChip(x, y, true)
        emitState(move)
        opponent.sendMove(x, y, move.result)
        if (move.result == MoveResult.MOVE_OPPONENT)
            waitOpponentMove()
    }

    private suspend fun pasteChip(x: Int, y: Int, isPlayer: Boolean): GameManager.State.Move {
        val isCross = if (isPlayer) iIsCross else !iIsCross
        val result = if (isCross)
            repository.pasteCross(x, y)
        else
            repository.pasteZero(x, y)
        if (result == GameRepository.Result.NOT_PASTE) {
            if (isPlayer.not()) waitOpponentMove()
        } else
            isPlayerMove = !isPlayerMove
        return GameManager.State.Move(x, y, isCross, getMoveResult(result, isPlayer))
    }

    private fun getMoveResult(
        result: GameRepository.Result,
        isPlayer: Boolean
    ): MoveResult =
        when (result) {
            GameRepository.Result.CONTINUE ->
                if (isPlayer) MoveResult.MOVE_OPPONENT
                else MoveResult.MOVE_PLAYER
            GameRepository.Result.NOT_PASTE ->
                MoveResult.CANCEL
            GameRepository.Result.WIN -> {
                gameIsFinish = true
                if (isPlayer) MoveResult.WIN_PLAYER
                else MoveResult.WIN_OPPONENT
            }
            GameRepository.Result.DRAWN -> {
                gameIsFinish = true
                MoveResult.DRAWN
            }
        }
}