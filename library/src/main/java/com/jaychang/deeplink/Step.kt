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
import com.jaychang.deeplink.internal.states
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

/**
 * A unit of work that [StepHandler] performs.
 *
 * Each step is represented as an item of the workflow stream, once handler finished the
 * work (e.g. a specific page is opened), and emitted the next step item, the workflow moves to the next step.
 *
 * If the step is awaitable (e.g. wait for user to log in), that means this step requires a signal to move on,
 * use [DeepLinkHandler.notify] to notify it to continue. To create an awaitable step, use [createAwaitable].
 * */
open class Step<Data, Handler : StepHandler>(
    private val observable: Observable<DataHandler<Data, Handler>>,
    private val isAwaitable: Boolean = false
) {
    private var notStarted = true

    fun <NextData, NextHandler : StepHandler> then(
        block: (Data, Handler) -> Step<NextData, NextHandler>
    ): Step<NextData, NextHandler> {
        // Chain next step with current step's return data and handler.
        val chained = asObservable().flatMap { dataHandler ->
            // Run next step only when next step handler is created and the step is not started yet.
            dataHandler.handler.lifecycle.states()
                .filter { it.isAtLeast(Lifecycle.State.CREATED) && notStarted }
                .doOnNext { notStarted = false }
                .flatMap {
                    block(dataHandler.data, dataHandler.handler).asObservable()
                }
        }
        return Step(chained, isAwaitable)
    }

    internal fun asObservable(): Observable<DataHandler<Data, Handler>> {
        // If the step is awaitable, wait for the signal from sentinel before starting.
        val start = if (isAwaitable) {
            DeepLinkWorkflow.sentinel.flatMap { value ->
                observable.map {
                    @Suppress("UNCHECKED_CAST")
                    DataHandler(value as Data, it.handler)
                }
            }
        } else {
            observable
        }
        return start.observeOn(AndroidSchedulers.mainThread())
    }

    data class DataHandler<Data, Handler : StepHandler>(val data: Data, val handler: Handler)

    private object PoisonStep : Step<Any, StepHandler>(Observable.error(RuntimeException("Workflow is terminated.")))

    companion object {
        /**
         * Creates a poison step to terminate the workflow.
         * */
        fun terminated(): Step<Any, StepHandler> = PoisonStep

        /**
         * Creates an awaitable step. This will pause the workflow and continue once received signal
         * from [DeepLinkHandler.notify].
         * */
        fun <Data, Handler : StepHandler> createAwaitable(nextHandler: Handler): Step<Data, Handler> {
            val action = Observable.defer {
                Observable.fromCallable {
                    @Suppress("UNCHECKED_CAST")
                    DataHandler(DeepLinkWorkflow.sentinel.value as Data, nextHandler)
                }
            }
            return Step(action, true)
        }

        /**
         * Creates a normal step.
         * */
        fun <Data, Handler : StepHandler> create(data: Data, nextHandler: Handler): Step<Data, Handler> {
            val action = Observable.defer {
                Observable.fromCallable { DataHandler(data, nextHandler) }
            }
            return Step(action, false)
        }
    }
}
