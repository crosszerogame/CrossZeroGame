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
        val move = aiTurn()
        if (chipAI == Cell.CROSS)
            repository.pasteCross(move.first, move.second)
        else
            repository.pasteZero(move.first, move.second)
        _state.tryEmit(Opponent.State.Move(move.first, move.second))
    }

    override suspend fun sendBye() {
    }

    // Функция Игоря Алексеенко, рефакторинг Юрия Тихомирова:
// у противника выбирает элемент с максимальным количеством шагов до победы - stepMax,
// при равных значениях stepMaxPlayer выбирает элемент с максимальным количеством проставленных фишек, входящих в победные комбинации - freedomWithFrMax,
// при равных значениях freedomWithFrMax выбирает элемент с максимальным количество победных комбинаций - freedomMax
// у себя выбирает элемент с максимальным количеством шагов до победы - stepMaxAi,
// при равных значениях stepMaxAi выбирает элемент с максимальным количеством проставленных фишек, входящих в победные комбинации - freedomWithFrMax,
// при равных значениях freedomWithFrMax выбирает элемент с максимальным количество победных комбинаций - freedomMax
// для хода, если  stepMaxAi >= stepMaxPlayer, выбирается лучший элемент для себя, иначе выбирается лучший элемент для противника
    private suspend fun aiTurn(): Pair<Int, Int> {
        val map = repository.field
        var xPlayer = 0
        var yPlayer = 0
        var xAi = 0
        var yAi = 0
        var stepMaxPlayer = 0
        var stepMaxAi = 0
        var freedomMax = 0
        var freedomWithFrMax = 0
        for (y in 0 until fieldSize) {
            for (x in 0 until fieldSize) {
                if (map[y][x] == Cell.EMPTY) {
                    with(repository.calcStep(chipPlayer, x, y)) {
                        if (step > stepMaxPlayer) {
                            stepMaxPlayer = step
                            freedomMax = freedom
                            freedomWithFrMax = freedomWithFr
                            xPlayer = x
                            yPlayer = y
                        } else if (step == stepMaxPlayer && freedomWithFr > freedomWithFrMax) {
                            freedomWithFrMax = freedomWithFr
                            freedomMax = freedom
                            xPlayer = x
                            yPlayer = y
                        } else if (step == stepMaxPlayer && freedomWithFr == freedomWithFrMax && freedom > freedomMax) {
                            freedomMax = freedom
                            xPlayer = x
                            yPlayer = y
                        }
                    }

                    freedomMax = 0
                    freedomWithFrMax = 0
                    with(repository.calcStep(chipAI, x, y)) {
                        if (step > stepMaxAi) {
                            stepMaxAi = step
                            freedomMax = freedom
                            xAi = x
                            yAi = y
                        } else if (step == stepMaxAi && freedomWithFr > freedomWithFrMax) {
                            freedomWithFrMax = freedomWithFr
                            freedomMax = freedom
                            xAi = x
                            yAi = y
                        } else if (step == stepMaxAi && freedomWithFr == freedomWithFrMax && freedom > freedomMax) {
                            freedomMax = freedom
                            xAi = x
                            yAi = y
                        }
                    }

                }
            }
        }

        val x: Int
        val y: Int
        if (stepMaxAi >= stepMaxPlayer) {
            x = xAi
            y = yAi
        } else {
            x = xPlayer
            y = yPlayer
        }
        return Pair(x, y)
    }
}