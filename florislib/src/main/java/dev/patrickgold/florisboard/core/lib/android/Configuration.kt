/*
 * Copyright (C) 2021 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.core.lib.android

import android.content.res.Configuration
import dev.patrickgold.florisboard.core.lib.FlorisLocale

fun Configuration.isOrientationPortrait(): Boolean {
    return this.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Configuration.isOrientationLandscape(): Boolean {
    return this.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Configuration.setLocale(locale: FlorisLocale) {
    return this.setLocale(locale.base)
}
