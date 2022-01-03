package ru.geekbrains.android2.crosszerogame.view.list

import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.databinding.ItemGameBinding
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings

class GameHolder(
    private val binding: ItemGameBinding,
    private val strings: GameStrings,
    private val onItemClick: ((Int) -> Unit)
) : RecyclerView.ViewHolder(binding.root) {

    fun setItem(game: Game) = binding.run {
        val waitCross: Boolean
        val nick: String
        if (game.playerZero == null) {
            nick = game.playerCross?.nick ?: ""
            waitCross = false
        } else {
            nick = game.playerZero.nick
            waitCross = true
        }
        tvNick.text = nick
        tvWait.text = if (waitCross) strings.waitCrossPlayer else strings.waitZeroPlayer
        pbLevel.progress = game.level
        tvSize.text = String.format(strings.fieldSizeFormat, game.fieldSize, game.fieldSize, game.chipsForWin)
        root.setOnClickListener {
            onItemClick(game.id)
        }
    }
}