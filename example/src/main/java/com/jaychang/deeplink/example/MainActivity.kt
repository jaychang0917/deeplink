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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jaychang.deeplink.DeepLinkHandler
import com.jaychang.deeplink.Step
import com.jaychang.deeplink.example.databinding.ActivityMainBinding
import com.jaychang.deeplink.example.deeplinks.PaymentDeepLinkWorkflow
import com.jaychang.deeplink.example.deeplinks.ProductDeepLinkWorkflow

class MainActivity :
    AppCompatActivity(),
    ProductDeepLinkWorkflow.EntryHandler,
    PaymentDeepLinkWorkflow.EntryHandler {

    val deepLinkHandler = DeepLinkHandler(this).apply {
        addWorkflow(ProductDeepLinkWorkflow)
        addWorkflow(PaymentDeepLinkWorkflow)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var activeFragment: Fragment
    private val homeFragment = HomeFragment.create()
    private val profileFragment = ProfileFragment.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigationView()
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    // Product workflow handler
    override fun goToHomePage(): Step<Unit, ProductDeepLinkWorkflow.HomeHandler> {
        binding.bottomNavigation.selectedItemId = R.id.home
        return Step.create(Unit, homeFragment)
    }

    // Payment workflow handler
    override fun waitUntilLoggedIn(): Step<Boolean, PaymentDeepLinkWorkflow.EntryHandler> {
        return if (FakeAppManager.isLoggedIn) {
            Step.create(true, this)
        } else {
            goToLoginPage()
            // Wait for user to login
            Step.createAwaitable(this)
        }
    }

    override fun goToProfilePage(): Step<Unit, PaymentDeepLinkWorkflow.ProfileHandler> {
        binding.bottomNavigation.selectedItemId = R.id.profile
        return Step.create(Unit, profileFragment)
    }

    // simple: main -> home -> product
    //    deep link: https://jaychang.page.link/product?name=iPhone
    //    command: adb shell am start -a "android.intent.action.VIEW" -d "https://jaychang.page.link/P8GD"
    //
    // complex: main -> until logged -> profile -> payment
    //    deep link: https://jaychang.page.link/payment
    //    command: adb shell am start -a "android.intent.action.VIEW" -d "https://jaychang.page.link/bZqj"
    private fun handleDeepLink(intent: Intent) {
        Firebase.dynamicLinks.getDynamicLink(intent)
            .addOnSuccessListener(this) { data ->
                data?.link?.let {
                    deepLinkHandler.handle(it)
                }
            }
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.commit {
                        hide(activeFragment).show(homeFragment)
                    }
                    activeFragment = homeFragment
                    true
                }
                R.id.profile -> {
                    if (FakeAppManager.isLoggedIn) {
                        supportFragmentManager.commit {
                            hide(activeFragment).show(profileFragment)
                        }
                        activeFragment = profileFragment
                        true
                    } else {
                        goToLoginPage()
                        false
                    }
                }
                else -> false
            }
        }
        supportFragmentManager.commit {
            add(R.id.parentContainer, homeFragment).hide(homeFragment)
            add(R.id.parentContainer, profileFragment).hide(profileFragment)
        }
        activeFragment = when (binding.bottomNavigation.selectedItemId) {
            R.id.home -> homeFragment
            R.id.profile -> profileFragment
            else -> error("two fragments only")
        }
        binding.bottomNavigation.selectedItemId = binding.bottomNavigation.selectedItemId
    }

    private fun goToLoginPage() {
        supportFragmentManager.commit {
            replace(R.id.childContainer, LoginFragment.create())
            addToBackStack(null)
        }
    }
}
