/*
 * Copyright (C) 2022 Patrick Goldinger
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

package dev.patrickgold.florisboard.core.ime.input

import androidx.compose.runtime.Composable
import dev.patrickgold.florisboard.core.R
import dev.patrickgold.florisboard.core.lib.compose.stringRes
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries

enum class HapticVibrationMode  {
    USE_VIBRATOR_DIRECTLY,
    USE_HAPTIC_FEEDBACK_INTERFACE;

    companion object {
        @Composable
        fun listEntries() = listPrefEntries {
            entry(
                key = USE_VIBRATOR_DIRECTLY,
                label = stringRes(R.string.enum__haptic_vibration_mode__use_vibrator_directly),
                description = stringRes(R.string.enum__haptic_vibration_mode__use_vibrator_directly__description),
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = USE_HAPTIC_FEEDBACK_INTERFACE,
                label = stringRes(R.string.enum__haptic_vibration_mode__use_haptic_feedback_interface),
                description = stringRes(R.string.enum__haptic_vibration_mode__use_haptic_feedback_interface__description),
                showDescriptionOnlyIfSelected = true,
            )
        }
    }
}
