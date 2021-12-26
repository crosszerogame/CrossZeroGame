package ru.geekbrains.android2.crosszerogame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.geekbrains.android2.crosszerogame.data.*
import ru.geekbrains.android2.crosszerogame.data.remote.CrossZeroDBFake

class MainActivity : AppCompatActivity() {
    private var remoteOpponent = true
    private var gamer2AI = false
    private val db = CrossZeroDBFake()
    private var gr1 = GameRepositoryImpl(remoteOpponent, db)
    private var gr2 = GameRepositoryImpl(remoteOpponent, db)
    private var gr2ai = GameRepositoryImpl(false, db)
    private var gamer1 = Gamer()
    private var opponent1 = Gamer()
    private var gamer2 = Gamer()
    private var gamer2ai = Gamer()
    private var opponent2 = Gamer()
    private var game1 = Game()
    private var game2 = Game()
    private var game2ai = Game()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAIHuman.setOnClickListener {
            if (btnAIHuman.text.toString() == "AI") {
                btnAIHuman.text = "Human"
                remoteOpponent = true
                gamer2AI = false
            } else
                if (btnAIHuman.text.toString() == "Human") {
                    btnAIHuman.text = "Human-AI"
                    remoteOpponent = true
                    gamer2AI = true
                } else {
                    btnAIHuman.text = "AI"
                    remoteOpponent = false
                    gamer2AI = false
                }
            gr1 = GameRepositoryImpl(remoteOpponent, db)
            gr2 = GameRepositoryImpl(remoteOpponent, db)
            gr2ai = GameRepositoryImpl(false, db)
        }

        btnGamer1.setOnClickListener {
            gamer1 = gr1.gamer(gameFieldSize = edFieldDim1.text.toString().toInt())
            edFieldDim1.setText(gamer1.gameFieldSize.toString())
            if (gamer2AI) {
                gamer2 = gr2.gamer(gameFieldSize = edFieldDim2.text.toString().toInt())
                edFieldDim2.setText(gamer2.gameFieldSize.toString())
            }
        }
        btnGamer2.setOnClickListener {
            gamer2 = gr2.gamer(gameFieldSize = edFieldDim2.text.toString().toInt())
            edFieldDim2.setText(gamer2.gameFieldSize.toString())
        }

        btnNewGame1.setOnClickListener {
            if (!remoteOpponent) {
                game1 = gr1.game(
                    gameStatus = when (edFirstStep1.text.toString().toInt()) {
                        0 -> GameConstants.GameStatus.NEW_GAME
                        1 -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                        2 -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                        else -> GameConstants.GameStatus.NEW_GAME
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
                        0 -> GameConstants.GameStatus.NEW_GAME
                        1 -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                        2 -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                        else -> GameConstants.GameStatus.NEW_GAME
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
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            )

            if (gamer2AI) {
                game2ai = gr2ai.game(
                    motionXIndex = edPlayer1X.text.toString().toInt() - 1,
                    motionYIndex = edPlayer1Y.text.toString().toInt() - 1,
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                )
                game2 = gr2.game(
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                )
                game2 = gr2.game(
                    motionXIndex = game2ai.motionXIndex,
                    motionYIndex = game2ai.motionYIndex,
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                )
                setGameTexts2()
            }
            setGameTexts1()

        }

        btnGameIsOn2.setOnClickListener {
            game2 = gr2.game(
                motionXIndex = edPlayer2X.text.toString().toInt() - 1,
                motionYIndex = edPlayer2Y.text.toString().toInt() - 1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            )
            setGameTexts2()
        }

        btnGetOpp1.setOnClickListener {
            if (gamer2AI) {
                gamer2.keyOpponent = gamer1.keyGamer
                game2 = gr2.setOpponent(
                    key = gamer2.keyOpponent,
                    gameStatus = when (edFirstStep2.text.toString().toInt()) {
                        0 -> GameConstants.GameStatus.NEW_GAME
                        1 -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                        2 -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                        else -> GameConstants.GameStatus.NEW_GAME
                    }
                ) ?: Game()
                gamer2ai = gr2ai.gamer(gameFieldSize = gamer2.gameFieldSize)
                game2ai = gr2ai.game(
                    gameStatus = if (game2.turnOfGamer) GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                    else GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                )
                setGameTexts2()
                edPlayer2X.setText("0")
                edPlayer2Y.setText("0")
            }

            opponent1 = gr1.getOpponent() ?: Gamer()
            textGetOpp1.text = opponent1.keyGamer
            if (opponent1.keyGamer != "") {
                game1 = gr1.game(
                    gameStatus = GameConstants.GameStatus.NEW_GAME_ACCEPT
                )

            }
            setGameTexts1()
            edPlayer1X.setText("0")
            edPlayer1Y.setText("0")
        }


        btnGetOpp2.setOnClickListener {
            opponent2 = gr2.getOpponent() ?: Gamer()
            textGetOpp2.text = opponent2.keyGamer
            if (opponent2.keyGamer != "") {
                game2 = gr2.game(
                    gameStatus = GameConstants.GameStatus.NEW_GAME_ACCEPT
                )
                setGameTexts2()
                edPlayer2X.setText("0")
                edPlayer2Y.setText("0")
            }
        }

        btnSetOpp1.setOnClickListener {
            gamer1.keyOpponent = edSetOpp1.text.toString()
            game1 = gr1.setOpponent(
                key = gamer1.keyOpponent,
                gameStatus = when (edFirstStep1.text.toString().toInt()) {
                    0 -> GameConstants.GameStatus.NEW_GAME
                    1 -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                    2 -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                    else -> GameConstants.GameStatus.NEW_GAME
                }
            ) ?: Game()
            setGameTexts1()
            edPlayer1X.setText("0")
            edPlayer1Y.setText("0")

            if (gamer2AI) {
                opponent2 = gr2.getOpponent() ?: Gamer()
                textGetOpp2.text = opponent2.keyGamer
                if (opponent2.keyGamer != "") {
                    game2 = gr2.game(
                        gameStatus = GameConstants.GameStatus.NEW_GAME_ACCEPT
                    )
                    setGameTexts2()
                    edPlayer2X.setText("0")
                    edPlayer2Y.setText("0")
                }
                gamer2ai = gr2ai.gamer(gameFieldSize = game2.gameFieldSize)
                game2ai = gr2ai.game(
                    gameStatus = if (game2.turnOfGamer) GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                else GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                )

            }
        }
        btnSetOpp2.setOnClickListener {
            gamer2.keyOpponent = edSetOpp2.text.toString()
            game2 = gr2.setOpponent(
                key = gamer2.keyOpponent,
                gameStatus = when (edFirstStep2.text.toString().toInt()) {
                    0 -> GameConstants.GameStatus.NEW_GAME
                    1 -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                    2 -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                    else -> GameConstants.GameStatus.NEW_GAME
                }
            ) ?: Game()
            setGameTexts2()
            edPlayer2X.setText("0")
            edPlayer2Y.setText("0")
        }

        btnLst1.setOnClickListener {
            val opLst = gr1.opponentsList()
            textLst1.text = if (opLst.size > 0) opLst[0].keyGamer else ""
            if (gamer2AI) {
                val opLst = gr2.opponentsList()
                textLst2.text = if (opLst.size > 0) opLst[0].keyGamer else ""
            }
        }
        btnLst2.setOnClickListener {
            val opLst = gr2.opponentsList()
            textLst2.text = if (opLst.size > 0) opLst[0].keyGamer else ""
        }

    }

    private fun setGameTexts1() {
        if (game1.turnOfGamer) {
            textPlayer1.text = "Ход игрока:"
            edPlayer1X.setEnabled(true)
            edPlayer1Y.setEnabled(true)
            edPlayer1X.setText("0")
            edPlayer1Y.setText("0")
            textOpponent1X.text =(game1.motionXIndex + 1).toString()
            textOpponent1Y.text= (game1.motionYIndex + 1).toString()
        } else {
            edPlayer1X.setEnabled(false)
            edPlayer1Y.setEnabled(false)
            textPlayer1.text = "Ход противника"
        }


        textForWinning1.setText("Фишек для выигрыша: ${game1.dotsToWin}")
        textResult1.text = when (game1.gameStatus) {
            GameConstants.GameStatus.GAME_IS_ON -> "GAME_IS_ON"
            GameConstants.GameStatus.WIN_GAMER -> "WIN_GAMER"
            GameConstants.GameStatus.WIN_OPPONENT -> "WIN_OPPONENT"
            GameConstants.GameStatus.DRAWN_GAME -> "DRAWN_GAME"
            GameConstants.GameStatus.ABORTED_GAME -> "ABORTED_GAME"
            GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT -> "NEW_GAME_FIRST_OPPONENT"
            GameConstants.GameStatus.NEW_GAME -> "NEW_GAME"
            GameConstants.GameStatus.NEW_GAME_FIRST_GAMER -> "NEW_GAME_FIRST_GAMER"
            GameConstants.GameStatus.NEW_GAME_ACCEPT -> "NEW_GAME_ACCEPT"

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
                    GameConstants.CellField.GAMER -> "x"
                    GameConstants.CellField.OPPONENT -> "o"
                    else -> "="
                }
            }
            s += "\n"
            j++
        }
        edGameField1.setText(s)
    }

    private fun setGameTexts2() {
        if (game2.turnOfGamer) {
            edPlayer2X.setEnabled(true)
            edPlayer2Y.setEnabled(true)
            textPlayer2.text = "Ход игрока:"
            edPlayer2X.setText("0")
            edPlayer2Y.setText("0")
            textOpponent2X.text=(game2.motionXIndex + 1).toString()
            textOpponent2Y.text =(game2.motionYIndex + 1).toString()
        } else {
            edPlayer2X.setEnabled(false)
            edPlayer2Y.setEnabled(false)
            textPlayer2.text = "Ход противника"
        }


        textForWinning2.setText("Фишек для выигрыша: ${game2.dotsToWin}")
        textResult2.text = when (game2.gameStatus) {
            GameConstants.GameStatus.GAME_IS_ON -> "GAME_IS_ON"
            GameConstants.GameStatus.WIN_GAMER -> "WIN_GAMER"
            GameConstants.GameStatus.WIN_OPPONENT -> "WIN_OPPONENT"
            GameConstants.GameStatus.DRAWN_GAME -> "DRAWN_GAME"
            GameConstants.GameStatus.ABORTED_GAME -> "ABORTED_GAME"
            GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT -> "NEW_GAME_FIRST_OPPONENT"
            GameConstants.GameStatus.NEW_GAME -> "NEW_GAME"
            GameConstants.GameStatus.NEW_GAME_FIRST_GAMER -> "NEW_GAME_FIRST_GAMER"
            GameConstants.GameStatus.NEW_GAME_ACCEPT -> "NEW_GAME_ACCEPT"
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
                    GameConstants.CellField.GAMER -> "x"
                    GameConstants.CellField.OPPONENT -> "o"
                    else -> "="
                }
            }
            s += "\n"
            j++
        }
        edGameField2.setText(s)
    }

}