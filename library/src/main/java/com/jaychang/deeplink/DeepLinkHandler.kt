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
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * A facade class that handles deep linking by executing appropriate [DeepLinkWorkflow].
 *
 * @param entryHandler the first workflow step handler to start the workflow. Usually
 * the entry point (e.g. MainActivity) receiving the deep link Intent will be the entry handler.
 * */
class DeepLinkHandler(private val entryHandler: StepHandler) {
    private val workflows = mutableSetOf<DeepLinkWorkflow<*, *>>()
    private var current: Disposable? = null

    fun addWorkflows(workflows: Set<DeepLinkWorkflow<*, *>>): DeepLinkHandler {
        this.workflows.addAll(workflows)
        return this
    }

    fun addWorkflow(workflow: DeepLinkWorkflow<*, *>): DeepLinkHandler {
        return addWorkflows(setOf(workflow))
    }

    /**
     * Executes an appropriate [DeepLinkWorkflow] to handle this [deepLink].
     * */
    fun handle(deepLink: Uri) {
        // Reset
        current?.dispose()
        DeepLinkWorkflow.sentinel = BehaviorSubject.create()

        val workflow = workflows.firstOrNull { it.canHandle(deepLink) } ?: return
        current = workflow.execute(deepLink, entryHandler)
    }

    /**
     * A helper method to check if there's a deep link workflow can handle this [intent].
     *
     * @return true if there's a workflow can handle it; false otherwise.
     * */
    fun canHandle(intent: Intent): Boolean {
        val deepLink = intent.data ?: return false
        return workflows.any { it.canHandle(deepLink) }
    }

    /**
     * Notifies the current executing workflow to move forward.
     *
     * @param value the value passed to the awaitable step.
     * */
    fun notify(value: Any) {
        if (current == null) return

        DeepLinkWorkflow.sentinel.onNext(value)
    }
}
