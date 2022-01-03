package ru.geekbrains.android2.crosszerogame.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.databinding.ItemGameBinding
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings

class GameAdapter(
    private val strings: GameStrings,
    private val onItemClick: ((Int) -> Unit)
) : RecyclerView.Adapter<GameHolder>() {
    private val games = mutableListOf<Game>()

    fun setItems(items: List<Game>) {
        games.clear()
        games.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GameHolder(
            ItemGameBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            strings,
            onItemClick
        )

    override fun onBindViewHolder(holder: GameHolder, position: Int) =
        holder.setItem(games[position])

    override fun getItemCount() = games.size
}