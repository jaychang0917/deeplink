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

const val KOTLIN_VERSION = "1.5.10"
const val MOCKK_VERSION = "1.11.0"
const val DETEKT_VERSION = "1.17.1"
const val LIFECYCLE_VERSION = "2.3.1"

object Libraries {
    // Kotlin
    const val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION"

    // Android
    const val APPCOMPAT = "androidx.appcompat:appcompat:1.3.0"
    const val FRAGMENT = "androidx.fragment:fragment-ktx:1.3.4"
    const val ANNOTATION = "androidx.annotation:annotation:1.2.0"
    const val MATERIAL = "com.google.android.material:material:1.3.0"
    const val LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:$LIFECYCLE_VERSION"
    const val LIFECYCLE_COMMON = "androidx.lifecycle:lifecycle-common-java8:$LIFECYCLE_VERSION"

    // Firebase
    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:28.1.0"
    const val FIREBASE_CRASHLYTICS = "com.google.firebase:firebase-crashlytics-ktx"
    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics-ktx"
    const val FIREBASE_DYNAMIC_LINKS = "com.google.firebase:firebase-dynamic-links-ktx"

    // Rx
    const val RXJAVA3 = "io.reactivex.rxjava3:rxjava:3.0.13"
    const val RXANDROID = "io.reactivex.rxjava3:rxandroid:3.0.0"

    // JVM Test
    const val JUNIT4 = "junit:junit:4.13"
    const val JUNIT5 = "org.junit.jupiter:junit-jupiter:5.7.2"
    const val MOCKK = "io.mockk:mockk:$MOCKK_VERSION"
    const val TRUTH_ASSERT = "androidx.test.ext:truth:1.0.0"
    const val ANDROID_TEST_RUNNER = "androidx.test.ext:junit-ktx:1.1.2"

    // Misc
    const val DETEKT_FORMATTING = "io.gitlab.arturbosch.detekt:detekt-formatting:$DETEKT_VERSION"
}
