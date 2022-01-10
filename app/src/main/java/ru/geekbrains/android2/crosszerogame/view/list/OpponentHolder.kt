package ru.geekbrains.android2.crosszerogame.view.list

import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.App
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.databinding.ItemGameBinding
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings

class OpponentHolder(
    private val binding: ItemGameBinding,
    private val strings: GameStrings,
    private val onItemClick: ((Gamer) -> Unit)
) : RecyclerView.ViewHolder(binding.root) {

    fun setItem(opponent: Gamer) = binding.run {
        tvNick.text = opponent.nikGamer
        pbLevel.progress = opponent.levelGamer
        tvWait.text = if (opponent.chipImageId == 1) strings.waitCrossPlayer else strings.waitZeroPlayer
        tvSize.text = String.format(strings.fieldSizeFormat, opponent.gameFieldSize, opponent.gameFieldSize, App.grAi.dotsToWin(opponent.gameFieldSize).second)
        tvTime.text = String.format(strings.moveTimeFormat, opponent.timeForTurn)
        root.setOnClickListener {
            onItemClick(opponent)
        }
    }
}