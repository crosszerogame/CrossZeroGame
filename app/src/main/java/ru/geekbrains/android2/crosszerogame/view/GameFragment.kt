package ru.geekbrains.android2.crosszerogame.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.view.list.GameAdapter

class GameFragment : Fragment() {
    private val SIZE = 5
    private lateinit var rvField: RecyclerView
    private lateinit var adapter: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvField = view.findViewById(R.id.rv_field) as RecyclerView
        initField()
    }

    private fun initField() {
        val layoutManager = GridLayoutManager(requireContext(), SIZE)
        rvField.layoutManager = layoutManager
        adapter = GameAdapter(SIZE) { x, y ->
            adapter.setCrossOn(x, y)
        }
        rvField.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}