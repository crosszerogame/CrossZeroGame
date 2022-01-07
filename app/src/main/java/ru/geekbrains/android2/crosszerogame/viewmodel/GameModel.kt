package ru.geekbrains.android2.crosszerogame.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import ru.geekbrains.android2.crosszerogame.data.CellField
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameStatus
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence
import ru.geekbrains.android2.crosszerogame.model.repository.Repo
import ru.geekbrains.android2.crosszerogame.view.list.CellValue
import javax.inject.Inject
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.geekbrains.android2.crosszerogame.model.repository.ListData

class GameModel : ViewModel() {
    companion object {
        private const val DEFAULT_SIZE = 3
        private const val DEFAULT_FIRST = true
        private val parameters: MutableLiveData<GameParameters> = MutableLiveData()

        fun launchGame(fieldSize: Int, beginAsFirst: Boolean) {
            parameters.value = GameParameters(fieldSize, beginAsFirst)
        }
    }

    private val state: MutableLiveData<GameState> = MutableLiveData()
    private val ai = ArtIntelligence()
    private var gamer = Gamer()
    private var game = Game()
    private var size: Int = DEFAULT_SIZE
    private var isFirst: Boolean = DEFAULT_FIRST

    private val parametersObserver = Observer<GameParameters> {
        size = it.fieldSize
        isFirst = it.beginAsFirst
        newGame()
    }


    @Inject
    lateinit var provideRepo: Repo

    fun initKeys() {
        //   provideRepo.updateGamerOnServer(Gamer())
    }


    fun testRepo() {
        //  provideRepo.insertGamer(Gamer())

//        provideRepo.getListOfGamers().observeOn(Schedulers.io())
//            .subscribeOn(Schedulers.io())
//            .subscribe({
//                       println("found words : $it")
//            },
//                {
//                    it.printStackTrace()
//                })

//        provideRepo.getGamer("gamer").observeOn(Schedulers.io())
//            .subscribeOn(Schedulers.io())
//            .subscribe({
//                println("found gamer : $it")
//            },{
//                it.printStackTrace()
//            })

        val list = listOf(
            ListData("c1", "d1"),
            ListData("c2", "d2"),
            ListData("c3", "d3"),
            ListData("c4", "d4")
        )

//        provideRepo.insertVariableOnServer("Test", "changed")
//        provideRepo.insertVariableOnServer("toDelete", "changed")

//        provideRepo.deleteVariableFromServer("toDelete")
//
//        provideRepo.deleteTableFromServer("toDelete")

        //  provideRepo.insertVariableOnServer("testName", "testName1 changed")

        //  provideRepo.insertVariableOnServer("testName2", "testName2 changed")


//        provideRepo.insertGamerToServer(
//            Gamer()
//        )


        provideRepo.updateGamerOnServer(
            Gamer()
        )

//        provideRepo.deleteGamerFromServer(
//            Gamer()
//        )


        /*
        provideRepo.getGamersFromServer()!!.findInBackground { objects, e ->
            if (e == null) {
                for (i in 0 until objects.size) {
                    println(" nick : " + objects[i].getString("var_nikGamer"))
                }
            } else {
                Log.d("Error", e.message!!)
            }
        }

        */


//        provideRepo.initKeyGamer()

    }


    fun getState(): LiveData<GameState> = state

    fun getFieldSize() = size

    fun getCell(x: Int, y: Int) = when (game.gameField[y][x]) {
        CellField.GAMER -> if (isFirst) CellValue.CROSS else CellValue.ZERO
        CellField.OPPONENT -> if (isFirst) CellValue.ZERO else CellValue.CROSS
        CellField.EMPTY -> CellValue.EMPTY
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        if (game.gameStatus == GameStatus.NEW_GAME)
            newGame()
        else
            postState()
    }

    override fun onCleared() {
        parameters.removeObserver(parametersObserver)
        super.onCleared()
    }

    private fun newGame() {
        state.value = GameState.NewGame(size)
        gamer = ai.newGamer(size)
        game = ai.game(
            gameStatus = if (isFirst)
                GameStatus.NEW_GAME_FIRST_GAMER
            else
                GameStatus.NEW_GAME_FIRST_OPPONENT
        )
    }

    fun doMove(x: Int, y: Int) {
        if (game.gameStatus == GameStatus.GAME_IS_ON) {
            state.value = GameState.MovePlayer(x, y, isFirst)
            game = ai.game(
                motionXIndex = x,
                motionYIndex = y,
                gameStatus = GameStatus.GAME_IS_ON
            )
        }
        postState()
    }

    private fun postState() {
        when (game.gameStatus) {
            GameStatus.GAME_IS_ON ->
                doAiMove()
            GameStatus.WIN_GAMER ->
                state.value = GameState.WinPlayer
            GameStatus.WIN_OPPONENT -> {
                doAiMove()
                state.value = GameState.WinOpponent
            }
            GameStatus.DRAWN_GAME -> {
                doAiMove()
                state.value = GameState.DrawnGame
            }
            GameStatus.ABORTED_GAME ->
                state.value = GameState.AbortedGame
        }
    }

    private fun doAiMove() {
        if (game.motionXIndex > -1)
            state.value = GameState.MoveOpponent(game.motionXIndex, game.motionYIndex, !isFirst)
    }

    fun readyField() {
        if (game.gameStatus == GameStatus.GAME_IS_ON && !isFirst)
            doAiMove()
    }
}