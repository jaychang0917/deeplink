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
import com.jaychang.deeplink.example.deeplinks.ProductDeepLinkWorkflow.EntryHandler
import com.jaychang.deeplink.example.deeplinks.ProductDeepLinkWorkflow.ProductDeepLink

object ProductDeepLinkWorkflow : DeepLinkWorkflow<ProductDeepLink, EntryHandler>() {

    override fun canHandle(deepLink: Uri): Boolean = deepLink.path == "/product"

    override fun parseDeepLink(deepLink: Uri): ProductDeepLink {
        val name = deepLink.getQueryParameter("name") ?: error("Missing parameter `name`")
        return ProductDeepLink(name = name)
    }

    override fun steps(deepLink: ProductDeepLink, entryHandler: EntryHandler): Step<out Any, out StepHandler> {
        return entryHandler.goToHomePage()
            .then { _, handler -> handler.goToProductPage(deepLink.name) }
    }

    data class ProductDeepLink(val name: String)

    interface EntryHandler : StepHandler {
        fun goToHomePage(): Step<Unit, HomeHandler>
    }

    interface HomeHandler : StepHandler {
        fun goToProductPage(name: String): Step<Unit, HomeHandler>
    }
}
