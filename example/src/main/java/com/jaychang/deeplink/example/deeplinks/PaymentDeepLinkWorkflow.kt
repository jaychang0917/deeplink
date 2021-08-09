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

package com.jaychang.deeplink.example.deeplinks

import android.net.Uri
import com.jaychang.deeplink.DeepLinkWorkflow
import com.jaychang.deeplink.Step
import com.jaychang.deeplink.StepHandler
import com.jaychang.deeplink.example.deeplinks.PaymentDeepLinkWorkflow.EntryHandler

object PaymentDeepLinkWorkflow : DeepLinkWorkflow<Unit, EntryHandler>() {

    override fun canHandle(deepLink: Uri): Boolean = deepLink.path == "/payment"

    override fun parseDeepLink(deepLink: Uri) = Unit

    override fun steps(deepLink: Unit, entryHandler: EntryHandler): Step<out Any, out StepHandler> {
        return entryHandler.waitUntilLoggedIn()
            .then { loggedIn, handler ->
                if (loggedIn) {
                    handler.goToProfilePage()
                } else {
                    // User gives up login, terminate the flow.
                    Step.terminated()
                }
            }
            .then { _, handler -> (handler as ProfileHandler).goToPaymentPage() }
    }

    override fun onCompleted() {
        println("PaymentDeepLinkWorkflow onCompleted")
    }

    override fun onTerminated() {
        println("PaymentDeepLinkWorkflow onTerminated")
    }

    interface EntryHandler : StepHandler {
        fun waitUntilLoggedIn(): Step<Boolean, EntryHandler>
        fun goToProfilePage(): Step<Unit, ProfileHandler>
    }

    interface ProfileHandler : StepHandler {
        fun goToPaymentPage(): Step<Unit, ProfileHandler>
    }
}
