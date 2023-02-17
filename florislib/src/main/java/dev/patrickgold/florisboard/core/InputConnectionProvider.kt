package dev.patrickgold.florisboard.core

import android.view.inputmethod.InputConnection
import androidx.compose.runtime.staticCompositionLocalOf
import dev.patrickgold.florisboard.core.ime.input.InputFeedbackController
import dev.patrickgold.florisboard.core.ime.input.InputServiceFeedbackController
import dev.patrickgold.florisboard.core.ime.input.SecureInputConnection

val LocalInputFeedbackController = staticCompositionLocalOf<InputFeedbackController> { error("not init") }

object InputConnectionProvider {
    private var secureInputConnection: SecureInputConnection? = null

    fun currentInputConnection(): InputConnection? {
//            return CustomEditText.appInputConnection
        return secureInputConnection
//            return FlorisImeServiceReference.get()?.currentInputConnection
    }

    fun setSecureInputConnection(conn: SecureInputConnection?) {
        secureInputConnection = conn
    }

    fun inputFeedbackController(): InputFeedbackController? {
        return null//FlorisImeServiceReference.get()?.inputFeedbackController
    }
}
