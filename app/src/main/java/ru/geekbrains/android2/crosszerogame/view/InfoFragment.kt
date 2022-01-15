package ru.geekbrains.android2.crosszerogame.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.databinding.*
import ru.geekbrains.android2.crosszerogame.view.list.WelcomeSlidesViewPagerAdapter
import ru.geekbrains.android2.crosszerogame.viewmodel.InfoViewModel


class InfoFragment : Fragment() {

    private lateinit var viewModel: InfoViewModel
    private var myAdapter: WelcomeSlidesViewPagerAdapter? = null

    private var vb: FragmentInfoBinding? = null
    private var welcomeSlideOneBinding: WelcomingSlidesFragmentSlideOneBinding? = null
    private var welcomeSlideTwoBinding: WelcomingSlidesFragmentSlideTwoBinding? = null
    private var welcomeSlideThreeBinding: WelcomingSlidesFragmentSlideThreeBinding? = null
    private var welcomeSlideFourBinding: WelcomingSlidesFragmentSlideFourBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        welcomeSlideOneBinding = WelcomingSlidesFragmentSlideOneBinding.inflate(inflater, container, false)
        welcomeSlideTwoBinding = WelcomingSlidesFragmentSlideTwoBinding.inflate(inflater, container, false)
        welcomeSlideThreeBinding = WelcomingSlidesFragmentSlideThreeBinding.inflate(inflater, container, false)
        welcomeSlideFourBinding = WelcomingSlidesFragmentSlideFourBinding.inflate(inflater, container, false)
        return FragmentInfoBinding.inflate(inflater, container, false).also {
            vb = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        myAdapter = WelcomeSlidesViewPagerAdapter()
        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)
        viewModel.eventNextSlide.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                setIndicators(it)
                showNextSlide(it)
            }
        })

        vb!!.viewPager.adapter = myAdapter
        vb?.imgNext?.setOnClickListener {
            viewModel.showNextSlide(getSize())
            viewModel.eventStartProject.observe(viewLifecycleOwner, { event ->
                event?.getContentIfNotHandledOrReturnNull()?.let {
                    (activity as MainActivity).openMainProject()

                }
            })
        }
    }

    private fun initListeners() {
        welcomeSlideOneBinding!!.txtSkip.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideOneBinding!!.txtNeverShow.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideTwoBinding!!.txtSkip.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideTwoBinding!!.txtNeverShow.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideThreeBinding!!.txtSkip.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideThreeBinding!!.txtNeverShow.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideFourBinding!!.txtSkip.setOnClickListener {
            viewModel.skipInfoSlides()
        }
        welcomeSlideFourBinding!!.txtNeverShow.setOnClickListener {
            viewModel.skipInfoSlides()
        }
    }

    fun showNextSlide(current: Int) {
        vb!!.viewPager.currentItem = current
    }

    private fun getSize(): Int = myAdapter?.layouts?.size ?: 0

    fun getCurrentItem(): Int = vb!!.viewPager.currentItem

    fun setIndicators(slideNumber: Int) {
        when (slideNumber) {
            0 -> vb?.firstIndicator?.setImageResource(R.drawable.swipe_indicator_active)
            1 -> vb?.secondIndicator?.setImageResource(R.drawable.swipe_indicator_active)
            2 -> vb?.thirdIndicator?.setImageResource(R.drawable.swipe_indicator_active)
            3 -> vb?.fourthIndicator?.setImageResource(R.drawable.swipe_indicator_active)
        }
    }

    companion object {

        fun newInstance() =
            InfoFragment()
    }
}