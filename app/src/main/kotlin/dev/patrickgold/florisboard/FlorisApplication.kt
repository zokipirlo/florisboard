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

package dev.patrickgold.florisboard

import android.app.Application
import dev.patrickgold.florisboard.core.FlorisboardLibrary

@Suppress("unused")
class FlorisApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FlorisboardLibrary.init(
            applicationContext,
            BuildConfig.APPLICATION_ID,
            getString(R.string.floris_app_name),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            R.mipmap.floris_app_icon
        )
    }
}
