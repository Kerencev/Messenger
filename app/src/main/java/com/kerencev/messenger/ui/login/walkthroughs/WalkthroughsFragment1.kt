package com.kerencev.messenger.ui.login.walkthroughs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kerencev.messenger.databinding.FragmentWalkthroughs1Binding

class WalkthroughsFragment1 : WalkthroughsFragment() {

    private var _binding: FragmentWalkthroughs1Binding? = null
    private val binding: FragmentWalkthroughs1Binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalkthroughs1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSkip1.setOnClickListener {
            onActionSkipClick()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}