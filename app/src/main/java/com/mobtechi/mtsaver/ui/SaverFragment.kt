@file:Suppress("DEPRECATION")

package com.mobtechi.mtsaver.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TabHost
import android.widget.TabHost.TabSpec
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobtechi.mtsaver.Functions.getAppPath
import com.mobtechi.mtsaver.Functions.getStatusList
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.adapter.GridSpacingItemDecoration
import com.mobtechi.mtsaver.adapter.SaverAdapter
import com.mobtechi.mtsaver.databinding.FragmentSaverBinding

class SaverFragment : Fragment() {

    private var _binding: com.mobtechi.mtsaver.databinding.FragmentSaverBinding? = null
    private lateinit var saverAdapter: SaverAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSaverBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val tabHost = root.findViewById<TabHost>(R.id.tabHost)
//        tabHost.setup()
//
//        var spec: TabSpec = tabHost.newTabSpec(getString(R.string.title_status))
//        spec.setContent(R.id.status)
//        spec.setIndicator(getString(R.string.title_status))
//        tabHost.addTab(spec)
//
//        spec = tabHost.newTabSpec(getString(R.string.title_reel))
//        spec.setContent(R.id.reel)
//        spec.setIndicator(getString(R.string.title_reel))
//        tabHost.addTab(spec)

        // status list
        val savedStatusPath = getAppPath() + "/status"
        val savedStatusList = getStatusList(requireActivity(), savedStatusPath)
        if (savedStatusList != null) {
            // show the status in the recycler view
            val statusRecyclerView: RecyclerView = root.findViewById(R.id.statusRecyclerView)
            saverAdapter = SaverAdapter(requireActivity())
            saverAdapter.setDataList(savedStatusList)
            statusRecyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = saverAdapter
                addItemDecoration(
                    GridSpacingItemDecoration(
                        2,
                        25,
                        true
                    )
                )
            }
        }

        // reel list
//        val savedReelPath = getAppPath() + "/reels"
//        val savedReelList = getStatusList(requireActivity(), savedReelPath)
//        if (savedReelList != null) {
//            // show the status in the recycler view
//            val reelRecyclerView: RecyclerView = root.findViewById(R.id.reelRecyclerView)
//            saverAdapter = SaverAdapter(requireActivity())
//            saverAdapter.setDataList(savedReelList)
//            reelRecyclerView.apply {
//                layoutManager = GridLayoutManager(requireContext(), 2)
//                adapter = saverAdapter
//                addItemDecoration(
//                    GridSpacingItemDecoration(
//                        2,
//                        25,
//                        true
//                    )
//                )
//            }
//        }
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        saverAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}