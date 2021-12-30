package ru.geekbrains.android2.crosszerogame.game.opponents

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import ru.geekbrains.android2.crosszerogame.game.GameRepositoryImpl
import ru.geekbrains.android2.crosszerogame.structure.GameRepository
import ru.geekbrains.android2.crosszerogame.structure.Opponent
import ru.geekbrains.android2.crosszerogame.structure.data.Cell
import ru.geekbrains.android2.crosszerogame.structure.data.Player

class AiOpponent(private val fieldSize: Int, private val isCross: Boolean) : Opponent {
    companion object {
        private const val DELAY_TIME: Long = 100
    }

    private val repository: GameRepository = GameRepositoryImpl()
    private val chipPlayer = if (isCross) Cell.ZERO else Cell.CROSS
    private val chipAI = if (isCross) Cell.CROSS else Cell.ZERO
    private val _state = MutableStateFlow<Opponent.State>(Opponent.State.Created)
    override val state: Flow<Opponent.State>
        get() = _state

    private var playerStep = GameRepository.Step(0, 0, 0)
    private var aiStep = GameRepository.Step(0, 0, 0)

    override fun preparing(): Flow<Player> = flow {
        repository.newGame(fieldSize)
        if (isCross)
            doMove()
        emit(Player(nick = "AI"))
    }

    override suspend fun sendMove(x: Int, y: Int) {
        val result = if (chipPlayer == Cell.CROSS)
            repository.pasteCross(x, y)
        else
            repository.pasteZero(x, y)
        if (result == GameRepository.Result.CONTINUE) {
            delay(DELAY_TIME)
            doMove()
        }
    }

    private suspend fun doMove() {
        val move = aiMove()
        if (chipAI == Cell.CROSS)
            repository.pasteCross(move.first, move.second)
        else
            repository.pasteZero(move.first, move.second)
        _state.tryEmit(Opponent.State.Move(move.first, move.second))
    }

    override suspend fun sendBye() {
    }

    private suspend fun aiMove(): Pair<Int, Int> {
        playerStep = GameRepository.Step(0, 0, 0)
        aiStep = GameRepository.Step(0, 0, 0)
        var playerMove = Pair(-1, -1)
        var aiMove = Pair(-1, -1)
        var move: Pair<Int, Int>

        for (y in 0 until fieldSize) {
            for (x in 0 until fieldSize) {
                if (repository.field[y][x] != Cell.EMPTY)
                    continue
                move = calcMove(true, x, y)
                if (move.first > -1)
                    playerMove = move
                move = calcMove(false, x, y)
                if (move.first > -1)
                    aiMove = move
            }
        }

        return if (aiStep.step >= playerStep.step)
            aiMove
        else
            playerMove
    }

    private suspend fun calcMove(isPlayer: Boolean, x: Int, y: Int): Pair<Int, Int> {
        val max: GameRepository.Step
        val newMax: GameRepository.Step
        if (isPlayer) {
            max = playerStep
            newMax = repository.calcStep(chipPlayer, x, y)
        } else {
            max = aiStep
            newMax = repository.calcStep(chipAI, x, y)
        }
        with(newMax) {
            if (step > max.step ||
                (step == max.step && freedomWithFr > max.freedomWithFr) ||
                (step == max.step && freedomWithFr == max.freedomWithFr && freedom > max.freedom)
            ) {
                if (isPlayer)
                    playerStep = newMax
                else
                    aiStep = newMax
                return Pair(x, y)
            }
        }
        return Pair(-1, -1)
    }
}