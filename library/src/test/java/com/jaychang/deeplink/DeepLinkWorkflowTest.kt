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

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.spyk
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkWorkflowTest {
    @Before
    fun before() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun after() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `onCompleted callback`() {
        var onCompleted = false
        val workflow = spyk(object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) = Unit
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.create(Unit, entryHandler)
            }
            override fun onCompleted() {
                onCompleted = true
            }
        })
        val deepLink = Uri.parse("app://payment")
        val entryHandler = TestStepHandler()

        workflow.execute(deepLink, entryHandler)

        assertThat(onCompleted).isTrue()
    }

    @Test
    fun `onTerminated callback for step termination`() {
        var onTerminated = false
        val workflow = object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) = Unit
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.terminated()
            }
            override fun onTerminated() {
                onTerminated = true
            }
        }
        val deepLink = Uri.parse("app://payment")
        val entryHandler = TestStepHandler()

        workflow.execute(deepLink, entryHandler)

        assertThat(onTerminated).isTrue()
    }

    @Test
    fun `onTerminated callback for workflow cancellation`() {
        var onTerminated = false
        val workflow = object : DeepLinkWorkflow<Unit, TestStepHandler>() {
            override fun canHandle(deepLink: Uri): Boolean = true
            override fun parseDeepLink(deepLink: Uri) = Unit
            override fun steps(deepLink: Unit, entryHandler: TestStepHandler): Step<out Any, out StepHandler> {
                return Step.createAwaitable(entryHandler)
            }
            override fun onTerminated() {
                onTerminated = true
            }
        }
        val deepLink = Uri.parse("app://payment")
        val entryHandler = TestStepHandler()

        workflow.execute(deepLink, entryHandler).dispose()

        assertThat(onTerminated).isTrue()
    }
}
