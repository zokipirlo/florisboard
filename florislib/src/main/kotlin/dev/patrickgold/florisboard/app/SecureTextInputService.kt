package dev.patrickgold.florisboard.app

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.EditCommand
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.PlatformTextInputService
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextInputService
import dev.patrickgold.florisboard.FlorisImeService
import dev.patrickgold.florisboard.SecureInputConnectionCallback

class SecurePlatformTextInputService(
    val view: View,
    private val openKeyboard: (Boolean) -> Unit
) : PlatformTextInputService {
    private var editorHasFocus = false
    private var onEditCommand: (List<EditCommand>) -> Unit = {}
    private var onImeActionPerformed: (ImeAction) -> Unit = {}

    var state = TextFieldValue(text = "", selection = TextRange.Zero)
        private set
    private var imeOptions = ImeOptions.Default

    // used for sendKeyEvent delegation
    private val baseInputConnection by lazy(LazyThreadSafetyMode.NONE) {
        BaseInputConnection(view, false)
    }

    fun isEditorFocused(): Boolean = editorHasFocus

    private val secureInputConnection by lazy {
        SecureInputConnection(
            state,
            object : SecureInputConnectionCallback {
                override fun onEditCommands(editCommands: List<EditCommand>) {
                    onEditCommand(editCommands)
                }

                override fun onImeAction(imeAction: ImeAction) {
                    onImeActionPerformed(imeAction)
                }

                override fun onKeyEvent(event: KeyEvent) {
                    baseInputConnection.sendKeyEvent(event)
                }

                override fun onConnectionClosed(ic: SecureInputConnection) {
                    FlorisImeService.setSecureInputConnection(null)
                }

            },
            false
        ).also {
            FlorisImeService.setSecureInputConnection(it)
        }
    }

    override fun hideSoftwareKeyboard() {
        openKeyboard(false)
    }

    override fun showSoftwareKeyboard() {
        openKeyboard(true)
    }

    override fun startInput(
        value: TextFieldValue,
        imeOptions: ImeOptions,
        onEditCommand: (List<EditCommand>) -> Unit,
        onImeActionPerformed: (ImeAction) -> Unit
    ) {
        editorHasFocus = true
        state = value
        this.imeOptions = imeOptions
        this.onEditCommand = onEditCommand
        this.onImeActionPerformed = onImeActionPerformed

        openKeyboard(true)
    }

    override fun stopInput() {
        editorHasFocus = false
        onEditCommand = {}
        onImeActionPerformed = {}

        openKeyboard(false)
    }

    override fun updateState(oldValue: TextFieldValue?, newValue: TextFieldValue) {
        this.state = newValue
        // update the latest TextFieldValue in InputConnection
        secureInputConnection.mTextFieldValue = newValue
    }
}

class SecureTextInputService(
    view: View,
    openKeyboard: (Boolean) -> Unit,
    textInputService: SecurePlatformTextInputService = SecurePlatformTextInputService(
        view,
        openKeyboard
    )
) : TextInputService(textInputService) {

}
