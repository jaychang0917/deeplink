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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.jaychang.deeplink.Step
import com.jaychang.deeplink.example.databinding.FragmentHomeBinding
import com.jaychang.deeplink.example.deeplinks.ProductDeepLinkWorkflow

class HomeFragment : Fragment(), ProductDeepLinkWorkflow.HomeHandler {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.goProductPageButton.setOnClickListener { goToProductPageInternal("iMac") }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToProductPageInternal(productName: String) {
        parentFragmentManager.commit {
            replace(R.id.childContainer, ProductFragment.create(productName))
            addToBackStack(null)
        }
    }

    override fun goToProductPage(name: String): Step<Unit, ProductDeepLinkWorkflow.HomeHandler> {
        goToProductPageInternal(name)
        return Step.create(Unit, this)
    }

    companion object {
        fun create(): HomeFragment {
            return HomeFragment()
        }
    }
}
