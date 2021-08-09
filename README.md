A deep link handler for Android. Inspired by uber RIBs workflow.

The `DeepLinkWorkflow` encapsulates the deep link handling logic:
- Which deep link to handle.
- How to parse the deep link parameters.
- What steps to perform to lead to a specific destination.
  - The Android components(e.g. Activity) performs the actual steps implementation.
- What to do when the workflow completes or terminates.

Then we pass the deep link to `DeepLinkHandler` and it will trigger an appropriate `DeepLinkWorkflow` for it.

# Download
Deeplink is available on `mavenCentral()`.
```
implementation("io.github.jaychang0917:deeplink:0.0.1")
```    

# Quick Start
1. Defines a deep link workflow.
```kotlin
object PaymentDeepLinkWorkflow : DeepLinkWorkflow<Unit, EntryHandler>() {
    
    // The workflow can handle any deep links starting with path "/payment". 
    override fun canHandle(deepLink: Uri): Boolean = deepLink.path == "/payment"
    
    // No parameters need to be parsed, return Unit.
    override fun parseDeepLink(deepLink: Uri): Unit = Unit
     
    // Steps
    // 1. Wait until user logged in.
    // 2.1 If logged in, go to profile page. 
    // 2.2 Else terminate the workflow, step 3 will not be executed.
    // 3. Then go to payment page.
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
     
    // The workflow is completed.
    override fun onCompleted() {
        // ...
    }

    // The workflow is terminated.
    override fun onTerminated() {
        // ...
    }

    interface EntryHandler : StepHandler {
        fun waitUntilLoggedIn() : Step<Boolean, EntryHandler>
        fun goToProfilePage() : Step<Unit, ProfileHandler>
    }

    interface ProfileHandler : StepHandler {
        fun goToPaymentPage() : Step<Unit, ProfileHandler>
    }
}
```  
2. Implements the `StepHandler`s' logic.
```kotlin
// EntryHandler
class MainActivity : AppCompatActivity(), PaymentDeepLinkWorkflow.EntryHandler {

    override fun waitUntilLoggedIn(): Step<Boolean, PaymentDeepLinkWorkflow.EntryHandler> {
        return if (isLoggedIn) {
            Step.create(true, this)
        } else {
            // Go to login page.
            supportFragmentManager.commit {
                replace(R.id.container, LoginFragment.create())
                addToBackStack(null)
            }
            // Wait for user to login.
            Step.createAwaitable(this)
        }
    }

    override fun goToProfilePage(): Step<Unit, PaymentDeepLinkWorkflow.ProfileHandler> {
        binding.bottomNavigation.selectedItemId = R.id.profile
        return Step.create(Unit, profileFragment)
    }
}  
// Login
class LoginFragment : Fragment() {

    override fun onDestroyView() {
        super.onDestroyView()

        if (!isLoggedIn) {
            // User decides to give up.
            deepLinkHandler.notify(false)
        }
    }

    private fun onLoggedIn() {
        deepLinkHandler.notify(true)
    }
}
 
// ProfileHandler
class ProfileFragment : Fragment(), PaymentDeepLinkWorkflow.ProfileHandler {

    override fun goToPaymentPage(): Step<Unit, PaymentDeepLinkWorkflow.ProfileHandler> {
        // Go to payment page.
        parentFragmentManager.commit {
            replace(R.id.container, PaymentFragment.create())
            addToBackStack(null)
        }
        return Step.create(Unit, this)
    }
}
``` 
3. Delegates the deep link handling to `DeepLinkHandler`.
```kotlin
class MainActivity : AppCompatActivity(), PaymentDeepLinkWorkflow.EntryHandler {
    private val deepLinkHandler = DeepLinkHandler(this).apply {
        addWorkflow(PaymentDeepLinkWorkflow)
    }  
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent) {
        val deepLink = intent.data ?: return
        deepLinkHandler.handle(deepLink)
    }
}
``` 
# License
```
 Copyright (C) 2021. Jay Chang
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```
