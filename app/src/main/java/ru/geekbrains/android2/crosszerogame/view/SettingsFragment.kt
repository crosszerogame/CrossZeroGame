package ru.geekbrains.android2.crosszerogame.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null

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
        }
    }

    private fun initFieldSize() = binding?.run {
        sbFieldsize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                tvFieldsize.text = String.format(getString(R.string.field_size), i + 3, i + 3)
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