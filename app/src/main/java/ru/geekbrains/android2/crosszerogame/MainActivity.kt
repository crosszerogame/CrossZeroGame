package ru.geekbrains.android2.crosszerogame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.geekbrains.android2.crosszerogame.data.CellField
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameStatus
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence
import ru.geekbrains.android2.crosszerogame.model.ServerConnection

class MainActivity : AppCompatActivity() {

    private val ai = ArtIntelligence()
    private var gamer = Gamer()
    private var game = Game()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //   ServerConnection().insertVariableOnServer("var", "10")

      //  ServerConnection().insertVariableOnServer("score", "24")


        ServerConnection().liveDataVariableValue.observe(this, {
            println("vari = ${it.variableName} ${it.variableValue}")
        })

        btnNewGamer.setOnClickListener {
            gamer = ai.newGamer(edFieldDim.text.toString().toInt())
            edFieldDim.setText(gamer.gameFieldSize.toString())

        }
        btnNewGame.setOnClickListener {
            game = ai.game(
                gameStatus = when (edFirstStep.text.toString().toInt()) {
                    0 -> GameStatus.NEW_GAME
                    1 -> GameStatus.NEW_GAME_FIRST_GAMER
                    2 -> GameStatus.NEW_GAME_FIRST_OPPONENT
                    else -> GameStatus.NEW_GAME
                }
            )
            setGameTexts()
            edPlayerX.setText("0")
            edPlayerY.setText("0")
        }
        btnGameIsOn.setOnClickListener {
            game = ai.game(
                motionXIndex = edPlayerX.text.toString().toInt() - 1,
                motionYIndex = edPlayerY.text.toString().toInt() - 1,
                gameStatus = GameStatus.GAME_IS_ON
            )
            setGameTexts()
        }

    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        ServerConnection().getVariableFromServer("score")
    }

    private fun setGameTexts() {
        edIntelligenceX.setText((game.motionXIndex + 1).toString())
        edIntelligenceY.setText((game.motionYIndex + 1).toString())
        textForWinning.setText("Фишек для выигрыша: ${game.dotsToWin}")
        textResult.text = when (game.gameStatus) {
            GameStatus.GAME_IS_ON -> "GAME_IS_ON"
            GameStatus.WIN_GAMER -> "WIN_GAMER"
            GameStatus.WIN_OPPONENT -> "WIN_OPPONENT"
            GameStatus.DRAWN_GAME -> "DRAWN_GAME"
            GameStatus.ABORTED_GAME -> "ABORTED_GAME"
            GameStatus.NEW_GAME_FIRST_OPPONENT -> "NEW_GAME_FIRST_OPPONENT"
            GameStatus.NEW_GAME -> "NEW_GAME"
            GameStatus.NEW_GAME_FIRST_GAMER -> "NEW_GAME_FIRST_GAMER"
        }

        edGameField.setText("")
        var s = ""
        var j = 1
        for (arrField in game.gameField) {
            if (j == 1) {
                var i = 1
                s += "0"
                for (field in arrField) {
                    s += i.toString()
                    i++
                }
                s += "\n"
            }
            s += j.toString()
            for (field in arrField) {
                s += when (field) {
                    CellField.GAMER -> "x"
                    CellField.OPPONENT -> "o"
                    else -> "="
                }
            }
            s += "\n"
            j++
        }
        edGameField.setText(s)
    }
}