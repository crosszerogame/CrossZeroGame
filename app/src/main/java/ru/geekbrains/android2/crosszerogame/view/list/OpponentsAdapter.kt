package ru.geekbrains.android2.crosszerogame.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.databinding.ItemGameBinding
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings

class OpponentsAdapter(
    private val strings: GameStrings,
    private val onItemClick: ((Gamer) -> Unit)
) : RecyclerView.Adapter<OpponentHolder>() {
    private val opponents = mutableListOf<Gamer>()

    fun setItems(items: List<Gamer>) {
        opponents.clear()
        opponents.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OpponentHolder(
            ItemGameBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            strings,
            onItemClick
        )

    override fun onBindViewHolder(holder: OpponentHolder, position: Int) =
        holder.setItem(opponents[position])

    override fun getItemCount() = opponents.size
}