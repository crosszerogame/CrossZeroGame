package ru.geekbrains.android2.crosszerogame.game

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import ru.geekbrains.android2.crosszerogame.game.opponents.AiOpponent
import ru.geekbrains.android2.crosszerogame.game.opponents.NullOpponent
import ru.geekbrains.android2.crosszerogame.structure.GameManager
import ru.geekbrains.android2.crosszerogame.structure.GameRepository
import ru.geekbrains.android2.crosszerogame.structure.Opponent
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.Player

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

    override fun getGames(): Flow<List<Game>> = flow{
        delay(2000)
        val game1 = Game(
            id = 1,
            fieldSize = 3,
            chipsForWin = 3,
            level = 3,
            moveTime = 30,
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
            moveTime = 20,
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
            moveTime = 50,
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
            moveTime = 40,
            playerCross = Player(
                id = 4,
                nick = "Nick4Nick4Nick4Nick4",
                lastTimeActive = 0
            ),
            playerZero = null
        )
        emit(listOf(game1, game2, game3, game4))
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
        gameIsFinish = true
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