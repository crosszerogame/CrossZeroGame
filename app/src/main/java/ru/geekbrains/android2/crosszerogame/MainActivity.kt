package ru.geekbrains.android2.crosszerogame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.geekbrains.android2.crosszerogame.data.*
import ru.geekbrains.android2.crosszerogame.data.remote.CrossZeroDBFake

class MainActivity : AppCompatActivity() {
    private var remoteOpponent = true //если true то репозиторий работает в режиме двух реальных игроков
    private var gamer2AI = false // если true - режим HUMAN-AI - играет 1 игрок со вторым, у которого суфлер ai
    private var db = CrossZeroDBFake()  //фейковая БД
    private var gr1 = GameRepositoryImpl(remoteOpponent, db) //репозиторий 1 игрока
    private var gr2 = GameRepositoryImpl(remoteOpponent, db) //репозиторий 2 игрока
    private var gr2ai = GameRepositoryImpl(false, db) //репозиторий ai-суфлера 2 игрока
    private var gamer1 = Gamer()  //1 игрок
    private var opponent1 = Gamer() //проттивник 1 игрока
    private var gamer2 = Gamer() //2 игрок
    private var gamer2ai = Gamer() //игрок для ai-суфлера 2 игрока
    private var opponent2 = Gamer() //проттивник 2 игрока
    private var game1 = Game() //Игра 1 игрока
    private var game2 = Game() //Игра 2 игрока
    private var game2ai = Game() //Игра ai-суфлера 2 игрока


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // кнопка выбора режима игры
        btnAIHuman.setOnClickListener {
            if (btnAIHuman.text.toString() == "AI") {
                btnAIHuman.text = "Human"  //играют два реальных игрока
                remoteOpponent = true
                gamer2AI = false
            } else
                if (btnAIHuman.text.toString() == "Human") {
                    btnAIHuman.text = "HUMAN-AI" //играет 1 игрок со вторым, для которого считает ходы AI
                    remoteOpponent = true       //в этом режиме нажимаем кнопки только 1-го игрока
                    gamer2AI = true
                } else {
                    btnAIHuman.text = "AI"  //каждый игрок играет со своим AI
                    remoteOpponent = false
                    gamer2AI = false
                }
            db = CrossZeroDBFake()
            gr1 = GameRepositoryImpl(remoteOpponent, db)
            gr2 = GameRepositoryImpl(remoteOpponent, db)
            gr2ai = GameRepositoryImpl(false, db)
        }

        //регистрируем 1 игрока
        btnGamer1.setOnClickListener {
            gamer1 = gr1.gamer(gameFieldSize = edFieldDim1.text.toString().toInt())
            edFieldDim1.setText(gamer1.gameFieldSize.toString())
            if (gamer2AI) {
                gamer2 = gr2.gamer(gameFieldSize = edFieldDim2.text.toString().toInt())
                edFieldDim2.setText(gamer2.gameFieldSize.toString())
            }
        }

        //регистрируем 2 игрока
        btnGamer2.setOnClickListener {
            gamer2 = gr2.gamer(gameFieldSize = edFieldDim2.text.toString().toInt())
            edFieldDim2.setText(gamer2.gameFieldSize.toString())
        }

        // используется для начала новой игры 1 игроком только в режиме AI
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
        // используется для начала новой игры 2 игроком только в режиме AI
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
        // нажимаем после ввода координат хода 1 игрока во всех режимах
        // или когда 1 игрок запрашивает ход противника в режимах HUMAN, HUMAN-AI
        btnGameIsOn1.setOnClickListener {
            //первый игрок делает ход
            game1 = gr1.game(
                motionXIndex = edPlayer1X.text.toString().toInt() - 1,
                motionYIndex = edPlayer1Y.text.toString().toInt() - 1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            )

            if (gamer2AI && !game1.turnOfGamer) {  //режим HUMAN-AI
                //передаем координаты хода 1 игрока в ai
                game2ai = gr2ai.game(
                    motionXIndex = edPlayer1X.text.toString().toInt() - 1,
                    motionYIndex = edPlayer1Y.text.toString().toInt() - 1,
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                )
                //второй игрок запрашивает ход противника
                game2 = gr2.game(
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                )
                //второй игрок делает ход с "подсказки" ai
                game2 = gr2.game(
                    motionXIndex = game2ai.motionXIndex,
                    motionYIndex = game2ai.motionYIndex,
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                )
                setGameTexts2()
            }
            setGameTexts1()

        }

        // нажимаем после ввода координат хода 2 игрока только в режиме HUMAN
        // или когда 2 игрок запрашивает ход противника в режиме HUMAN
        btnGameIsOn2.setOnClickListener {
            game2 = gr2.game(
                motionXIndex = edPlayer2X.text.toString().toInt() - 1,
                motionYIndex = edPlayer2Y.text.toString().toInt() - 1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            )
            setGameTexts2()
        }
// нажимаем, когда хотим получить противника, который бросил вызов игроку 1 в режимах HUMAN, HUMAN-AI
        btnGetOpp1.setOnClickListener {
            if (gamer2AI) {   //режим HUMAN-AI
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
            //получаем противника
            opponent1 = gr1.getOpponent() ?: Gamer()
            textGetOpp1.text = opponent1.keyGamer
            if (opponent1.keyGamer != "") {
                //здесь может идти запрос игроку, хочет ли он играть с таким противником
                //игрок принимает предложение поиграть
                game1 = gr1.game(
                    gameStatus = GameConstants.GameStatus.NEW_GAME_ACCEPT
                )

            }
            setGameTexts1()
            edPlayer1X.setText("0")
            edPlayer1Y.setText("0")
        }

// нажимаем, когда хотим получить противника, который бросил вызов игроку 2 в режиме HUMAN
        btnGetOpp2.setOnClickListener {
            //получаем противника
            opponent2 = gr2.getOpponent() ?: Gamer()
            textGetOpp2.text = opponent2.keyGamer
            if (opponent2.keyGamer != "") {
                //здесь может идти запрос игроку, хочет ли он играть с таким противником
                //игрок принимает предложение поиграть
                game2 = gr2.game(
                    gameStatus = GameConstants.GameStatus.NEW_GAME_ACCEPT
                )
                setGameTexts2()
                edPlayer2X.setText("0")
                edPlayer2Y.setText("0")
            }
        }

        // нажимаем, когда игрок 1 выбрал противника и бросает ему вызов
        // до нажатия нужно в edSetOpp1.text вставить ключ противника, который получаем
        // путем нажатия кнопки получения списка противников игрока 1 LST1
        // работает в режимах HUMAN, HUMAN-AI
        btnSetOpp1.setOnClickListener {
            //вносим ключ противника
            gamer1.keyOpponent = edSetOpp1.text.toString()
            //направляем противнику запрос на игру
            //параметры игры: размер поля и кто первый начинает игру берутся у бросившего вызов
            //протитвник может принять или не принять игру с такими условиями
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

            if (gamer2AI) {  //режим HUMAN-AI
                //получаем противника, который бросил вызов игроку 2
                opponent2 = gr2.getOpponent() ?: Gamer()
                textGetOpp2.text = opponent2.keyGamer
                if (opponent2.keyGamer != "") {
                    //принимаем игру с игроком 1
                    game2 = gr2.game(
                        gameStatus = GameConstants.GameStatus.NEW_GAME_ACCEPT
                    )
                    setGameTexts2()
                    edPlayer2X.setText("0")
                    edPlayer2Y.setText("0")
                }
                //инициализируем игрока с ai для подсказок игроку 2
                gamer2ai = gr2ai.gamer(gameFieldSize = game2.gameFieldSize)
                //начинаем игру с ai
                game2ai = gr2ai.game(
                    gameStatus = if (game2.turnOfGamer) GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                else GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                )
            }
        }

        // нажимаем, когда игрок 2 выбрал противника и бросает ему вызов
        // до нажатия нужно в edSetOpp2.text вставить ключ противника, который получаем
        // путем нажатия кнопки получения списка противников игрока 2 LST2
        // работает в режимt HUMAN
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

        // получаем список противников игрока 1
        //так как у нас сейчас всего один противник, заношу в textLst1 ключ первого противника из списка
        // работает в режимах HUMAN, HUMAN-AI
        btnLst1.setOnClickListener {
            val opLst = gr1.opponentsList()
            textLst1.text = if (opLst.size > 0) opLst[0].keyGamer else ""
            if (gamer2AI) {  //HUMAN-AI
              // получаю список противников игрока 2
                val opLst = gr2.opponentsList()
                textLst2.text = if (opLst.size > 0) opLst[0].keyGamer else ""
            }
        }

        // получаем список противников игрока 2
        //так как у нас сейчас всего один противник, заношу в textLst2 ключ первого противника из списка
        // работает в режиме HUMAN
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