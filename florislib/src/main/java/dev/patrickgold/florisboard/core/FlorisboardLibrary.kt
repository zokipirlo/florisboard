package dev.patrickgold.florisboard.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import androidx.core.os.UserManagerCompat
import dev.patrickgold.florisboard.app.florisPreferenceModel
import dev.patrickgold.florisboard.core.ime.clipboard.ClipboardManager
import dev.patrickgold.florisboard.core.ime.core.SubtypeManager
import dev.patrickgold.florisboard.core.ime.dictionary.DictionaryManager
import dev.patrickgold.florisboard.core.ime.editor.EditorInstance
import dev.patrickgold.florisboard.core.ime.keyboard.KeyboardManager
import dev.patrickgold.florisboard.core.ime.media.emoji.FlorisEmojiCompat
import dev.patrickgold.florisboard.core.ime.nlp.NlpManager
import dev.patrickgold.florisboard.core.ime.text.gestures.GlideTypingManager
import dev.patrickgold.florisboard.core.ime.theme.ThemeManager
import dev.patrickgold.florisboard.core.lib.NativeStr
import dev.patrickgold.florisboard.core.lib.cache.CacheManager
import dev.patrickgold.florisboard.core.lib.devtools.Flog
import dev.patrickgold.florisboard.core.lib.devtools.LogTopic
import dev.patrickgold.florisboard.core.lib.devtools.flogError
import dev.patrickgold.florisboard.core.lib.devtools.flogInfo
import dev.patrickgold.florisboard.core.lib.ext.ExtensionManager
import dev.patrickgold.florisboard.core.lib.io.AssetManager
import dev.patrickgold.florisboard.core.lib.io.deleteContentsRecursively
import dev.patrickgold.florisboard.core.lib.io.subFile
import dev.patrickgold.florisboard.core.lib.toNativeStr
import dev.patrickgold.jetpref.datastore.JetPref

object FlorisboardLibrary {
    lateinit var applicationContext: Context
    lateinit var applicationId: String
    lateinit var applicationName: String
    lateinit var versionName: String
    var versionCode: Int = 0
    var appIconId: Int = 0

    private const val ICU_DATA_ASSET_PATH = "icu/icudt69l.dat"

    private external fun nativeInitICUData(path: NativeStr): Int

    init {
        try {
            System.loadLibrary("florisboard-native")
        } catch (_: Exception) {
        }
    }

    private val prefs by florisPreferenceModel()
    val mainHandler by lazy { Handler(applicationContext.mainLooper) }

    val assetManager by lazy { AssetManager(applicationContext) }
    val cacheManager by lazy { CacheManager(applicationContext) }
    val clipboardManager by lazy { ClipboardManager(applicationContext) }
    val editorInstance by lazy { EditorInstance(applicationContext) }
    val extensionManager by lazy { ExtensionManager(applicationContext) }
    val glideTypingManager by lazy { GlideTypingManager(applicationContext) }
    val keyboardManager by lazy { KeyboardManager(applicationContext) }
    val nlpManager by lazy { NlpManager(applicationContext) }
    val subtypeManager by lazy { SubtypeManager(applicationContext) }
    val themeManager by lazy { ThemeManager(applicationContext) }

    fun init(
        applicationContext: Context,
        applicationId: String,
        applicationName: String,
        versionName: String,
        versionCode: Int,
        appIconId: Int,
    ) {
        this.applicationContext = applicationContext
        this.applicationId = applicationId
        this.applicationName = applicationName
        this.versionName = versionName
        this.versionCode = versionCode
        this.appIconId = appIconId

        try {
            JetPref.configure(saveIntervalMs = 500)
            Flog.install(
                context = applicationContext,
                isFloggingEnabled = BuildConfig.DEBUG,
                flogTopics = LogTopic.ALL,
                flogLevels = Flog.LEVEL_ALL,
                flogOutputs = Flog.OUTPUT_CONSOLE,
            )
            FlorisEmojiCompat.init(applicationContext)

            if (!UserManagerCompat.isUserUnlocked(applicationContext)) {
                applicationContext.cacheDir?.deleteContentsRecursively()
                extensionManager.init()
                applicationContext.registerReceiver(
                    BootComplete(applicationContext),
                    IntentFilter(Intent.ACTION_USER_UNLOCKED)
                )
                return
            }

            initDeps()
        } catch (e: Exception) {
            return
        }
    }

    fun initDeps() {
        initICU(applicationContext)
        applicationContext.cacheDir?.deleteContentsRecursively()
        prefs.initializeBlocking(applicationContext)
        extensionManager.init()
        clipboardManager.initializeForContext(applicationContext)
        DictionaryManager.init(applicationContext)
    }

    private fun initICU(context: Context): Boolean {
        try {
            val androidAssetManager = context.assets ?: return false
            val icuTmpDataFile = context.cacheDir.subFile("icudt.dat")
            icuTmpDataFile.outputStream().use { os ->
                androidAssetManager.open(ICU_DATA_ASSET_PATH).use { it.copyTo(os) }
            }
            val status = nativeInitICUData(icuTmpDataFile.absolutePath.toNativeStr())
            icuTmpDataFile.delete()
            return if (status != 0) {
                flogError { "Native ICU data initializing failed with error code $status!" }
                false
            } else {
                flogInfo { "Successfully loaded ICU data!" }
                true
            }
        } catch (e: Exception) {
            flogError { e.toString() }
            return false
        }
    }
}
