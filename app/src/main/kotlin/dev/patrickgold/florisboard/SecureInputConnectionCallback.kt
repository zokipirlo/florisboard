package dev.patrickgold.florisboard

import android.view.KeyEvent
import androidx.compose.ui.text.input.EditCommand
import androidx.compose.ui.text.input.ImeAction
import dev.patrickgold.florisboard.app.SecureInputConnection

interface SecureInputConnectionCallback {
    /**
     * Called when IME sends some input events.
     *
     * @param editCommands The list of edit commands.
     */
    fun onEditCommands(editCommands: List<EditCommand>)

    /**
     * Called when IME triggered IME action.
     *
     * @param imeAction An IME action.
     */
    fun onImeAction(imeAction: ImeAction)

    /**
     * Called when IME triggered a KeyEvent
     */
    fun onKeyEvent(event: KeyEvent)

    /**
     * Called when IME closed the input connection.
     *
     * @param ic a closed input connection
     */
    fun onConnectionClosed(ic: SecureInputConnection)
}
