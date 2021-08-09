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

import androidx.lifecycle.Lifecycle.State
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class StepTest {
    @Before
    fun before() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun after() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `call next step when previous step finished and handler is at least CREATED state`() {
        callNextStepForState(State.CREATED, times = 1)
        callNextStepForState(State.STARTED, times = 1)
        callNextStepForState(State.RESUMED, times = 1)
    }

    @Test
    fun `don't call next step when previous step finished and handler is in INITIALIZED or DESTROYED state`() {
        callNextStepForState(State.INITIALIZED, times = 0)
        callNextStepForState(State.DESTROYED, times = 0)
    }

    private fun callNextStepForState(state: State, times: Int) {
        val observable = PublishSubject.create<Step.DataHandler<Unit, TestStepHandler>>()
        val steps = Step(observable)
            .then { _, handler ->
                handler.nextStep(Unit, handler)
            }

        val entryHandler = spyk(TestStepHandler(state))
        steps.asObservable().subscribe()
        observable.onNext(Step.DataHandler(Unit, entryHandler))

        verify(exactly = times) {
            entryHandler.nextStep(any(), any())
        }
    }

    @Test
    fun `call next step when previous step finished and the state of handler changes from INITIALIZED to CREATED`() {
        val observable = PublishSubject.create<Step.DataHandler<Unit, TestStepHandler>>()
        val steps = Step(observable)
            .then { _, handler ->
                handler.nextStep(Unit, handler)
            }

        val entryHandler = spyk(TestStepHandler(State.INITIALIZED))
        steps.asObservable().subscribe()
        observable.onNext(Step.DataHandler(Unit, entryHandler))
        entryHandler.setState(State.CREATED)

        verify(exactly = 1) {
            entryHandler.nextStep(any(), any())
        }
    }

    @Test
    fun `previous step is awaiting`() {
        val entryHandler = spyk(TestStepHandler(State.CREATED))
        val observable = Observable.just(Step.DataHandler(Unit, entryHandler))
        val steps = Step(observable)
            .then { _, handler ->
                handler.nextAwaitableStep(handler)
            }
            .then { data, handler ->
                (handler as TestStepHandler).nextStep(data, handler)
            }

        steps.asObservable().subscribe()

        verify(exactly = 0) {
            entryHandler.nextStep(false, entryHandler)
        }

        DeepLinkWorkflow.sentinel.onNext(true)

        verify(exactly = 1) {
            entryHandler.nextStep(true, entryHandler)
        }
    }
}
