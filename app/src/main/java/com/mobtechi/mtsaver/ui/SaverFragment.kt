@file:Suppress("DEPRECATION")

package com.mobtechi.mtsaver.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TabHost
import android.widget.TabHost.TabSpec
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobtechi.mtsaver.Functions.getAppPath
import com.mobtechi.mtsaver.Functions.getSavedList
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.adapter.GridSpacingItemDecoration
import com.mobtechi.mtsaver.adapter.SaverAdapter
import com.mobtechi.mtsaver.databinding.FragmentSaverBinding

class SaverFragment : Fragment() {

    private var _binding: FragmentSaverBinding? = null
    private lateinit var statusAdapter: SaverAdapter
    private lateinit var reelAdapter: SaverAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSaverBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val tabHost = root.findViewById<TabHost>(R.id.tabHost)
        tabHost.setup()

        var spec: TabSpec = tabHost.newTabSpec(getString(R.string.title_status))
        spec.setContent(R.id.status)
        spec.setIndicator(getString(R.string.title_status))
        tabHost.addTab(spec)

        spec = tabHost.newTabSpec(getString(R.string.title_reel))
        spec.setContent(R.id.reel)
        spec.setIndicator(getString(R.string.title_reel))
        tabHost.addTab(spec)

        statusAdapter = SaverAdapter(requireActivity())
        reelAdapter = SaverAdapter(requireActivity())

        // status list
        val savedStatusPath = getAppPath() + "/status"
        val savedStatusList = getSavedList(requireActivity(), savedStatusPath)
        val noStatusText = root.findViewById<TextView>(R.id.no_status)
        if (savedStatusList.isNotEmpty()) {
            // show the status in the recycler view
            noStatusText.visibility = View.GONE
            val statusRecyclerView: RecyclerView = root.findViewById(R.id.statusRecyclerView)
            statusAdapter.setDataList(savedStatusList)
            statusRecyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = statusAdapter
                addItemDecoration(
                    GridSpacingItemDecoration(
                        2, 25, true
                    )
                )
            }
        } else {
            noStatusText.visibility = View.VISIBLE
        }

        // reel list
        val savedReelPath = getAppPath() + "/reels"
        val savedReelList = getSavedList(requireActivity(), savedReelPath)
        val noReelsText = root.findViewById<TextView>(R.id.no_reels)
        if (savedReelList.isNotEmpty()) {
            // show the status in the recycler view
            noReelsText.visibility = View.GONE
            val reelRecyclerView: RecyclerView = root.findViewById(R.id.reelRecyclerView)
            reelAdapter.setDataList(savedReelList)
            reelRecyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = reelAdapter
                addItemDecoration(
                    GridSpacingItemDecoration(
                        2, 25, true
                    )
                )
            }
        } else {
            noReelsText.visibility = View.VISIBLE
        }
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        statusAdapter.notifyDataSetChanged()
        reelAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}