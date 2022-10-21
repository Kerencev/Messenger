package com.kerencev.messenger.ui.login.walkthroughs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentWalkthroughs2Binding

class WalkthroughsFragment2 : WalkthroughsFragment() {

    private var _binding: FragmentWalkthroughs2Binding? = null

    private val binding: FragmentWalkthroughs2Binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalkthroughs2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSkip2.setOnClickListener {
            onActionSkipClick()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}