package com.rc.wellnestmodule.graphview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.rc.wellnestmodule.R
import com.rc.wellnestmodule.databinding.LayoutGraphLoaderBinding

/**
 * Created by Hussain on 30/11/22.
 */
class EcgLoaderView(context : Context) : ParentEcgView(context) {

    private lateinit var binding : LayoutGraphLoaderBinding


    override fun initialize(context: Context?) {
        val inflater = LayoutInflater.from(context)
        binding = LayoutGraphLoaderBinding.inflate(inflater,this,false)
    }

    fun getLoaderView() : View {
        val shimmerLayouts = mutableListOf<ShimmerFrameLayout>()

        shimmerLayouts.add(binding.shimmer1.shimmerLayout)
        shimmerLayouts.add(binding.shimmer2.shimmerLayout)
        shimmerLayouts.add(binding.shimmer3.shimmerLayout)
        shimmerLayouts.add(binding.shimmer4.shimmerLayout)
        shimmerLayouts.add(binding.shimmer5.shimmerLayout)
        shimmerLayouts.add(binding.shimmer6.shimmerLayout)
        shimmerLayouts.add(binding.shimmer7.shimmerLayout)
        shimmerLayouts.add(binding.shimmer8.shimmerLayout)
        shimmerLayouts.add(binding.shimmer9.shimmerLayout)
        shimmerLayouts.add(binding.shimmer10.shimmerLayout)
        shimmerLayouts.add(binding.shimmer11.shimmerLayout)
        shimmerLayouts.add(binding.shimmer12.shimmerLayout)
        for (sLayout in shimmerLayouts) {
            sLayout.setShimmer(
                Shimmer.AlphaHighlightBuilder()
                    .setBaseAlpha(0.4f)
                    .setDuration(300)
                    .build())

            sLayout.startShimmer()
        }

        return binding.root


    }
}