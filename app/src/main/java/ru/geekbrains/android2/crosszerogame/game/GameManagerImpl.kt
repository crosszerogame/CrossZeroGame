package ru.geekbrains.android2.crosszerogame.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import ru.geekbrains.android2.crosszerogame.game.opponents.AiOpponent
import ru.geekbrains.android2.crosszerogame.game.opponents.NullOpponent
import ru.geekbrains.android2.crosszerogame.structure.GameManager
import ru.geekbrains.android2.crosszerogame.structure.GameRepository
import ru.geekbrains.android2.crosszerogame.structure.Opponent
import ru.geekbrains.android2.crosszerogame.structure.data.Game

class GameManagerImpl(private val repository: GameRepository) : GameManager {
    private val _state = MutableStateFlow<GameManager.State>(GameManager.State.Created)
    override val state: Flow<GameManager.State>
        get() = _state
    override var lastState: GameManager.State = GameManager.State.Created
        private set
    override var gameIsFinish: Boolean = true
        private set
    private var opponent: Opponent = NullOpponent()
    private var iIsCross: Boolean = false

    override suspend fun createSingleGame(fieldSize: Int, iIsCross: Boolean) {
        createGame(fieldSize, iIsCross, AiOpponent(fieldSize, !iIsCross))
    }

    override suspend fun createRemoteGame(
        fieldSize: Int,
        myNick: String,
        iIsCross: Boolean,
        gameLevel: Int
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun connectTo(game: Game, myNick: String) {
        TODO("Not yet implemented")
    }

    private suspend fun createGame(fieldSize: Int, iIsCross: Boolean, opponent: Opponent) {
        gameIsFinish = false
        repository.newGame(fieldSize)
        this.iIsCross = iIsCross
        emitState(GameManager.State.WaitOpponent)
        this.opponent = opponent
        opponent.preparing().collect {
            emitState(GameManager.State.Ready)
            observeOpponentState()
        }
    }

    private fun emitState(value: GameManager.State) {
        _state.tryEmit(value)
        lastState = value
    }

    override fun getGames(): Flow<List<Game>> {
        TODO("Not yet implemented")
    }

    private suspend fun observeOpponentState() {
        opponent.state.collect {
            if (it == Opponent.State.Created)
                return@collect
            val state = when (it) {
                is Opponent.State.Move ->
                    pasteChip(it.x, it.y, false)
                Opponent.State.Sleep ->
                    GameManager.State.WaitOpponent
                Opponent.State.Created ->
                    GameManager.State.Ready
                is Opponent.State.Error ->
                    abortedGame()
                Opponent.State.Leave ->
                    abortedGame()
            }
            emitState(state)
        }
    }

    private fun abortedGame(): GameManager.State {
        gameIsFinish = true
        return GameManager.State.AbortedGame
    }

    override suspend fun finishGame() {
        opponent.sendBye()
    }

    override fun getCell(x: Int, y: Int) = repository.field[y][x]

    override suspend fun doMove(x: Int, y: Int) {
        val move = pasteChip(x, y, true)
        emitState(move)
        opponent.sendMove(x, y)
    }

    private suspend fun pasteChip(x: Int, y: Int, isPlayer: Boolean): GameManager.State.Move {
        val isCross = if (isPlayer) iIsCross else !iIsCross
        val result = if (isCross)
            repository.pasteCross(x, y)
        else
            repository.pasteZero(x, y)
        return GameManager.State.Move(x, y, isCross, getMoveResult(result, isPlayer))
    }

    private fun getMoveResult(
        result: GameRepository.Result,
        isPlayer: Boolean
    ): GameManager.Result =
        when (result) {
            GameRepository.Result.CONTINUE ->
                if (isPlayer) GameManager.Result.TURN_OPPONENT
                else GameManager.Result.TURN_PLAYER
            GameRepository.Result.NOT_PASTE ->
                GameManager.Result.CANCEL
            GameRepository.Result.WIN -> {
                gameIsFinish = true
                if (isPlayer) GameManager.Result.WIN_PLAYER
                else GameManager.Result.WIN_OPPONENT
            }
            GameRepository.Result.DRAWN -> {
                gameIsFinish = true
                GameManager.Result.DRAWN
            }
        }
}