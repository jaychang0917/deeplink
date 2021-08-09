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

package com.jaychang.deeplink.internal

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer

internal fun Lifecycle.states(): Observable<Lifecycle.State> {
    return LifecycleObservable(this)
}

internal class LifecycleObservable(private val lifecycle: Lifecycle) : Observable<Lifecycle.State>() {
    override fun subscribeActual(observer: Observer<in Lifecycle.State>) {
        val lifecycleObserver = LifecycleObserver(lifecycle, observer)
        observer.onSubscribe(lifecycleObserver)
        lifecycle.addObserver(lifecycleObserver)
    }

    private class LifecycleObserver(
        private val lifecycle: Lifecycle,
        private val observer: Observer<in Lifecycle.State>
    ) : MainThreadDisposable(), DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) = onStateChanged(Lifecycle.State.CREATED)

        override fun onStart(owner: LifecycleOwner) = onStateChanged(Lifecycle.State.STARTED)

        override fun onResume(owner: LifecycleOwner) = onStateChanged(Lifecycle.State.RESUMED)

        override fun onPause(owner: LifecycleOwner) = Unit

        override fun onStop(owner: LifecycleOwner) = Unit

        override fun onDestroy(owner: LifecycleOwner) = onStateChanged(Lifecycle.State.DESTROYED)

        private fun onStateChanged(state: Lifecycle.State) {
            if (!isDisposed) {
                observer.onNext(state)
            }
        }

        override fun onDispose() {
            lifecycle.removeObserver(this)
        }
    }
}
