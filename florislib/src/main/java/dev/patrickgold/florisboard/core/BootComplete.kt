package dev.patrickgold.florisboard.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.patrickgold.florisboard.core.lib.devtools.flogError

class BootComplete(private val applicationContext: Context) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_USER_UNLOCKED) {
            try {
                applicationContext.unregisterReceiver(this)
            } catch (e: Exception) {
                flogError { e.toString() }
            }

            FlorisboardLibrary.mainHandler.post { FlorisboardLibrary.initDeps() }
        }
    }
}
