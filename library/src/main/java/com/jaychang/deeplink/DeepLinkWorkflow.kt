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
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * A deep link workflow that composed by a chain of [Step]s.
 * */
abstract class DeepLinkWorkflow<DeepLink, Handler : StepHandler> {

    /**
     * True if this workflow can handle the [deepLink]; false otherwise.
     * */
    abstract fun canHandle(deepLink: Uri): Boolean

    /**
     * If the input [deepLink] can be handled by this workflow,
     * this method will be invoked to parse the input to a [DeepLink] model.
     * */
    abstract fun parseDeepLink(deepLink: Uri): DeepLink

    /**
     * A sequential steps of the deep link workflow, starts from the entry handler.
     * */
    abstract fun steps(deepLink: DeepLink, entryHandler: Handler): Step<out Any, out StepHandler>

    /**
     * This callback will be invoked when the workflow is finished.
     * */
    open fun onCompleted() {}

    /**
     * This callback will be invoked if the step is [Step.terminated].
     * */
    open fun onTerminated() {}

    internal fun execute(deepLink: Uri, entryHandler: StepHandler): Disposable {
        val data = parseDeepLink(deepLink)
        @Suppress("UNCHECKED_CAST")
        val steps = steps(data, entryHandler as Handler)

        return steps.asObservable()
            .subscribe(
                { onCompleted() },
                { onTerminated() }
            )
    }

    companion object {
        // Guard the awaitable step movement.
        internal var sentinel = BehaviorSubject.create<Any>()
    }
}
