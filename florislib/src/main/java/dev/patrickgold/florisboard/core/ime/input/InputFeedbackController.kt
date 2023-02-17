package dev.patrickgold.florisboard.core.ime.input

import dev.patrickgold.florisboard.core.ime.keyboard.KeyData
import dev.patrickgold.florisboard.core.ime.text.keyboard.TextKeyData

interface InputFeedbackController {
    fun updateSystemPrefsState()
    fun keyPress(data: KeyData = TextKeyData.UNSPECIFIED)
    fun keyLongPress(data: KeyData = TextKeyData.UNSPECIFIED)
    fun keyRepeatedAction(data: KeyData = TextKeyData.UNSPECIFIED)
    fun gestureSwipe(data: KeyData = TextKeyData.UNSPECIFIED)
    fun gestureMovingSwipe(data: KeyData = TextKeyData.UNSPECIFIED)
    fun systemPref(id: String): Boolean
    fun performAudioFeedback(data: KeyData, factor: Double)
    fun performHapticFeedback(data: KeyData, factor: Double)
}
