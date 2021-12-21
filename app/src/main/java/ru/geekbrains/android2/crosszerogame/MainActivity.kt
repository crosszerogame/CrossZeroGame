package ru.geekbrains.android2.crosszerogame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.geekbrains.android2.crosszerogame.data.CellField
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameStatus
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.data.GameRepository
import ru.geekbrains.android2.crosszerogame.data.remote.CrossZeroDBFake

class MainActivity : AppCompatActivity() {
    private var remoteOpponent = true
    private val db = CrossZeroDBFake()
    private var gr1 = GameRepository(remoteOpponent, db)
    private var gr2 = GameRepository(remoteOpponent, db)
    private var gamer1 = Gamer()
    private var opponent1 = Gamer()
    private var gamer2 = Gamer()
    private var opponent2 = Gamer()
    private var game1 = Game()
    private var game2 = Game()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAIHuman.setOnClickListener {
            if (btnAIHuman.text.toString() == "AI") {
                btnAIHuman.text = "Human"
                remoteOpponent = true
            } else {
                btnAIHuman.text = "AI"
                remoteOpponent = false
            }
            gr1 = GameRepository(remoteOpponent, db)
            gr2 = GameRepository(remoteOpponent, db)
        }

        btnGamer1.setOnClickListener {
            gamer1 = gr1.gamer(gameFieldSize = edFieldDim1.text.toString().toInt())
            edFieldDim1.setText(gamer1.gameFieldSize.toString())

        }
        btnGamer2.setOnClickListener {
            gamer2 = gr2.gamer(gameFieldSize = edFieldDim2.text.toString().toInt())
            edFieldDim2.setText(gamer2.gameFieldSize.toString())

        }

        btnNewGame1.setOnClickListener {
            if (!remoteOpponent) {
                game1 = gr1.game(
                    gameStatus = when (edFirstStep1.text.toString().toInt()) {
                        0 -> GameStatus.NEW_GAME
                        1 -> GameStatus.NEW_GAME_FIRST_GAMER
                        2 -> GameStatus.NEW_GAME_FIRST_OPPONENT
                        else -> GameStatus.NEW_GAME
                    }
                )
                setGameTexts1()
                edPlayer1X.setText("0")
                edPlayer1Y.setText("0")
            }

        }
        btnNewGame2.setOnClickListener {
            if (!remoteOpponent) {
                game2 = gr2.game(
                    gameStatus = when (edFirstStep2.text.toString().toInt()) {
                        0 -> GameStatus.NEW_GAME
                        1 -> GameStatus.NEW_GAME_FIRST_GAMER
                        2 -> GameStatus.NEW_GAME_FIRST_OPPONENT
                        else -> GameStatus.NEW_GAME
                    }
                )
                setGameTexts2()
                edPlayer2X.setText("0")
                edPlayer2Y.setText("0")
            }

        }

        btnGameIsOn1.setOnClickListener {
            game1 = gr1.game(
                motionXIndex = edPlayer1X.text.toString().toInt() - 1,
                motionYIndex = edPlayer1Y.text.toString().toInt() - 1,
                gameStatus = GameStatus.GAME_IS_ON
            )
            setGameTexts1()
        }
        btnGameIsOn2.setOnClickListener {
            game2 = gr2.game(
                motionXIndex = edPlayer2X.text.toString().toInt() - 1,
                motionYIndex = edPlayer2Y.text.toString().toInt() - 1,
                gameStatus = GameStatus.GAME_IS_ON
            )
            setGameTexts2()
        }

        btnGetOpp1.setOnClickListener {
            opponent1 = gr1.getOpponent() ?: Gamer()
            textGetOpp1.text = opponent1.keyGamer
        }
        btnGetOpp2.setOnClickListener {
            opponent2 = gr2.getOpponent() ?: Gamer()
            textGetOpp2.text = opponent2.keyGamer
        }

        btnSetOpp1.setOnClickListener {
            gamer1.keyOpponent = edSetOpp1.text.toString()
            gr1.setOpponent(gamer1.keyOpponent)
        }
        btnSetOpp2.setOnClickListener {
            gamer2.keyOpponent = edSetOpp2.text.toString()
            gr2.setOpponent(gamer2.keyOpponent)
        }

        btnLst1.setOnClickListener {
            val opLst = gr1.opponentsList()
            textLst1.text = if (opLst.size > 0) opLst[0].keyGamer else ""
        }
        btnLst2.setOnClickListener {
            val opLst = gr2.opponentsList()
            textLst2.text = if (opLst.size > 0) opLst[0].keyGamer else ""
        }

    }

    private fun setGameTexts1() {
        edOpponent1X.setText((game1.motionXIndex + 1).toString())
        edOpponent1Y.setText((game1.motionYIndex + 1).toString())
        textForWinning1.setText("Фишек для выигрыша: ${game1.dotsToWin}")
        textResult1.text = when (game1.gameStatus) {
            GameStatus.GAME_IS_ON -> "GAME_IS_ON"
            GameStatus.WIN_GAMER -> "WIN_GAMER"
            GameStatus.WIN_OPPONENT -> "WIN_OPPONENT"
            GameStatus.DRAWN_GAME -> "DRAWN_GAME"
            GameStatus.ABORTED_GAME -> "ABORTED_GAME"
            GameStatus.NEW_GAME_FIRST_OPPONENT -> "NEW_GAME_FIRST_OPPONENT"
            GameStatus.NEW_GAME -> "NEW_GAME"
            GameStatus.NEW_GAME_FIRST_GAMER -> "NEW_GAME_FIRST_GAMER"
            GameStatus.NEW_GAME_ACCEPT -> "NEW_GAME_ACCEPT"

        }

        edGameField1.setText("")
        var s = ""
        var j = 1
        for (arrField in game1.gameField) {
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
        edGameField1.setText(s)
    }

    private fun setGameTexts2() {
        edOpponent2X.setText((game2.motionXIndex + 1).toString())
        edOpponent2Y.setText((game2.motionYIndex + 1).toString())
        textForWinning2.setText("Фишек для выигрыша: ${game2.dotsToWin}")
        textResult2.text = when (game2.gameStatus) {
            GameStatus.GAME_IS_ON -> "GAME_IS_ON"
            GameStatus.WIN_GAMER -> "WIN_GAMER"
            GameStatus.WIN_OPPONENT -> "WIN_OPPONENT"
            GameStatus.DRAWN_GAME -> "DRAWN_GAME"
            GameStatus.ABORTED_GAME -> "ABORTED_GAME"
            GameStatus.NEW_GAME_FIRST_OPPONENT -> "NEW_GAME_FIRST_OPPONENT"
            GameStatus.NEW_GAME -> "NEW_GAME"
            GameStatus.NEW_GAME_FIRST_GAMER -> "NEW_GAME_FIRST_GAMER"
            GameStatus.NEW_GAME_ACCEPT -> "NEW_GAME_ACCEPT"
        }

        edGameField2.setText("")
        var s = ""
        var j = 1
        for (arrField in game2.gameField) {
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
        edGameField2.setText(s)
    }

}