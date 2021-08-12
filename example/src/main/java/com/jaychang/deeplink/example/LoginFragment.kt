/*
 * Copyright (C) 2021. Jay Chang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaychang.deeplink.example

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jaychang.deeplink.DeepLinkHandler
import com.jaychang.deeplink.example.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var deepLinkHandler: DeepLinkHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // For production app, use DI to inject a DeepLinkHandler instead.
        deepLinkHandler = (context as MainActivity).deepLinkHandler
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.loginButton.setOnClickListener { login() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (!FakeAppManager.isLoggedIn) {
            // User decided to give up
            deepLinkHandler.notify(false)
        }
    }

    private fun login() {
        FakeAppManager.isLoggedIn = true
        parentFragmentManager.popBackStack()
        deepLinkHandler.notify(true)
    }

    companion object {
        fun create(): LoginFragment {
            return LoginFragment()
        }
    }
}
