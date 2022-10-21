package com.kerencev.messenger.ui.login.walkthroughs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kerencev.messenger.databinding.FragmentWalkthroughs3Binding

class WalkthroughsFragment3 : WalkthroughsFragment() {

    private var _binding: FragmentWalkthroughs3Binding? = null

    private val binding: FragmentWalkthroughs3Binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalkthroughs3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSkip3.setOnClickListener {
            onActionSkipClick()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}