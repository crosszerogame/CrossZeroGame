package ru.geekbrains.android2.crosszerogame.view.list

import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.App
import ru.geekbrains.android2.crosszerogame.xdata.Gamer
import ru.geekbrains.android2.crosszerogame.databinding.ItemGameBinding
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings
import ru.geekbrains.android2.crosszerogame.viewmodel.SettingsModel.Companion.SHIFT_LEVEL

class OpponentHolder(
    private val binding: ItemGameBinding,
    private val strings: GameStrings,
    private val onItemClick: ((Gamer) -> Unit)
) : RecyclerView.ViewHolder(binding.root) {

    fun setItem(opponent: Gamer) = binding.run {
        tvNick.text = opponent.nikGamer
        pbLevel.progress = opponent.levelGamer.ordinal + SHIFT_LEVEL
        tvSize.text = String.format(
            strings.fieldSizeFormat,
            opponent.gameFieldSize,
            opponent.gameFieldSize,
            App.grAi.dotsToWin(opponent.gameFieldSize).second
        )
        root.setOnClickListener {
            onItemClick(opponent)
        }
    }
}