package ru.geekbrains.android2.crosszerogame.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence
import ru.geekbrains.android2.crosszerogame.databinding.FragmentSettingsBinding
import ru.geekbrains.android2.crosszerogame.viewmodel.GameModel

class SettingsFragment : Fragment() {
    companion object {
        private const val SHIFT_SIZE = 3
    }
    private var binding: FragmentSettingsBinding? = null
    private val ai = ArtIntelligence()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFieldSize()
        binding?.run {
            btnFirst.isChecked = true
            sbFieldsize.progress = 0
            btnStart.setOnClickListener {
                GameModel.launchGame(sbFieldsize.progress + SHIFT_SIZE, btnFirst.isChecked)
                requireActivity().onBackPressed()
            }
        }
    }

    private fun initFieldSize() = binding?.run {
        sbFieldsize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val size = i + SHIFT_SIZE
                ai.newGamer(size)
                val game = ai.game()
                tvFieldsize.text = String.format(getString(R.string.field_size), size, size, game.dotsToWin)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}