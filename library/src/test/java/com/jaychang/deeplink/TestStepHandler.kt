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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleRegistry

class TestStepHandler(defaultState: State = State.INITIALIZED) : StepHandler {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        setState(defaultState)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    fun setState(state: State) {
        lifecycleRegistry.currentState = state
    }

    fun nextStep(data: Any, handler: StepHandler) = Step.create(data, handler)

    fun nextAwaitableStep(handler: StepHandler) = Step.createAwaitable<Any, StepHandler>(handler)
}
