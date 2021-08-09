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

package com.jaychang.deeplink

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkHandlerTest {
    private val entryHandler = TestStepHandler()
    private val handler = DeepLinkHandler(entryHandler)

    @Test
    fun `cancel current running workflow before starting a new one`() {
        var terminated = false
        val workflow = object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) = Unit
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.createAwaitable(entryHandler)
            }
            override fun onTerminated() {
                terminated = true
            }
        }
        handler.addWorkflow(workflow)
        val deepLink = Uri.parse("app://payment")
        handler.handle(deepLink)

        handler.handle(deepLink)

        assertThat(terminated).isEqualTo(true)
    }

    @Test
    fun `execute the first workflow that can handle the deep link`() {
        var check = 0
        val workflow1 = object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) {
                check = 1
            }
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.create(Unit, entryHandler)
            }
        }
        val workflow2 = object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) {
                check = 2
            }
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.create(Unit, entryHandler)
            }
        }
        handler.addWorkflows(setOf(workflow1, workflow2))
        val deepLink = Uri.parse("app://payment")

        handler.handle(deepLink)

        assertThat(check).isEqualTo(1)
    }

    @Test
    fun `can handle valid deep link`() {
        val intent = Intent().apply { data = Uri.parse("app://payment") }
        val workflow = object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) = Unit
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.createAwaitable(entryHandler)
            }
        }
        handler.addWorkflow(workflow)
        assertThat(handler.canHandle(intent)).isTrue()
    }

    @Test
    fun `cannot handle invalid deep link`() {
        val intentHaveNoData = Intent()
        assertThat(handler.canHandle(intentHaveNoData)).isFalse()
    }

    @Test
    fun `no workflow can handle the deep link`() {
        val validDeepLinkIntent = Intent().apply { data = Uri.parse("app://payment") }
        assertThat(handler.canHandle(validDeepLinkIntent)).isFalse()
    }
}
