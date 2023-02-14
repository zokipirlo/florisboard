package dev.patrickgold.florisboard.core

import android.view.inputmethod.InputConnection
import dev.patrickgold.florisboard.app.SecureInputConnection
import dev.patrickgold.florisboard.core.ime.input.InputFeedbackController

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
