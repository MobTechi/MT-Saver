package com.mobtechi.mtsaver.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobtechi.mtsaver.Functions
import com.mobtechi.mtsaver.Functions.getStatusList
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.adapter.GridSpacingItemDecoration
import com.mobtechi.mtsaver.adapter.StatusAdapter
import com.mobtechi.mtsaver.databinding.FragmentStatusBinding

class StatusFragment : Fragment() {

    private var _binding: FragmentStatusBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val waDirPath = Functions.getStatusPath()
        val statusList = getStatusList(requireActivity(), waDirPath)
        // show the status in the recycler view
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        val statusAdapter = StatusAdapter(requireActivity())
        statusAdapter.setDataList(statusList)
        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = statusAdapter
            addItemDecoration(
                GridSpacingItemDecoration(
                    2,
                    25,
                    true
                )
            )
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}