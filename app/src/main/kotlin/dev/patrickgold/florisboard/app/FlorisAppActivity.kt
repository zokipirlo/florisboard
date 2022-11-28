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

package dev.patrickgold.florisboard.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.apptheme.FlorisAppTheme
import dev.patrickgold.florisboard.app.devtools.DevtoolsOverlay
import dev.patrickgold.florisboard.ime.ImeUiMode
import dev.patrickgold.florisboard.ime.clipboard.ClipboardInputLayout
import dev.patrickgold.florisboard.ime.keyboard.ProvideKeyboardRowBaseHeight
import dev.patrickgold.florisboard.ime.media.MediaInputLayout
import dev.patrickgold.florisboard.ime.onehanded.OneHandedMode
import dev.patrickgold.florisboard.ime.onehanded.OneHandedPanel
import dev.patrickgold.florisboard.ime.text.TextInputLayout
import dev.patrickgold.florisboard.ime.theme.FlorisImeTheme
import dev.patrickgold.florisboard.ime.theme.FlorisImeUi
import dev.patrickgold.florisboard.keyboardManager
import dev.patrickgold.florisboard.lib.FlorisLocale
import dev.patrickgold.florisboard.lib.android.AndroidVersion
import dev.patrickgold.florisboard.lib.android.hideAppIcon
import dev.patrickgold.florisboard.lib.android.isOrientationLandscape
import dev.patrickgold.florisboard.lib.android.isOrientationPortrait
import dev.patrickgold.florisboard.lib.android.setLocale
import dev.patrickgold.florisboard.lib.android.showAppIcon
import dev.patrickgold.florisboard.lib.compose.LocalPreviewFieldController
import dev.patrickgold.florisboard.lib.compose.PreviewKeyboardField
import dev.patrickgold.florisboard.lib.compose.ProvideLocalizedResources
import dev.patrickgold.florisboard.lib.compose.SystemUiApp
import dev.patrickgold.florisboard.lib.compose.SystemUiIme
import dev.patrickgold.florisboard.lib.compose.rememberPreviewFieldController
import dev.patrickgold.florisboard.lib.compose.stringRes
import dev.patrickgold.florisboard.lib.observeAsTransformingState
import dev.patrickgold.florisboard.lib.snygg.ui.SnyggSurface
import dev.patrickgold.florisboard.lib.util.AppVersionUtils
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.ProvideDefaultDialogPrefStrings

enum class AppTheme(val id: String) {
    AUTO("auto"),
    AUTO_AMOLED("auto_amoled"),
    LIGHT("light"),
    DARK("dark"),
    AMOLED_DARK("amoled_dark");
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("LocalNavController not initialized")
}

class FlorisAppActivity : ComponentActivity() {
    private val prefs by florisPreferenceModel()
    private var appTheme by mutableStateOf(AppTheme.AUTO)
    private var showAppIcon = true
    private var resourcesContext by mutableStateOf(this as Context)

    private val keyboardManager by keyboardManager()
    private var inputViewSize by mutableStateOf(IntSize.Zero)
    private val inputFeedbackController by lazy { InputActivityFeedbackController.new(this@FlorisAppActivity) }
    private var isFullscreenUiMode by mutableStateOf(false)
    private var isExtractUiShown by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
//        window?.setFlags(
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//        )

        // Splash screen should be installed before calling super.onCreate()
        installSplashScreen().apply {
            setKeepOnScreenCondition { !prefs.datastoreReadyStatus.get() }
        }
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        prefs.advanced.settingsTheme.observe(this) {
            appTheme = it
        }
        prefs.advanced.settingsLanguage.observe(this) {
            val config = Configuration(resources.configuration)
            config.setLocale(if (it == "auto") FlorisLocale.default() else FlorisLocale.fromTag(it))
            resourcesContext = createConfigurationContext(config)
        }
        if (AndroidVersion.ATMOST_API28_P) {
            prefs.advanced.showAppIcon.observe(this) {
                showAppIcon = it
            }
        }

        // We defer the setContent call until the datastore model is loaded, until then the splash screen stays drawn
        prefs.datastoreReadyStatus.observe(this) { isModelLoaded ->
            if (!isModelLoaded) return@observe
            AppVersionUtils.updateVersionOnInstallAndLastUse(this, prefs)
            setContent {
                ProvideLocalizedResources(resourcesContext) {
                    FlorisAppTheme(theme = appTheme) {
                        Surface(color = MaterialTheme.colors.background) {
                            SystemUiApp()
                            AppContent()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // App icon visibility control was restricted in Android 10.
        // See https://developer.android.com/reference/android/content/pm/LauncherApps#getActivityList(java.lang.String,%20android.os.UserHandle)
        if (AndroidVersion.ATMOST_API28_P) {
            if (showAppIcon) {
                this.showAppIcon()
            } else {
                this.hideAppIcon()
            }
        }
    }

    @Composable
    private fun AppContent() {
        val navController = rememberNavController()
        val previewFieldController = rememberPreviewFieldController()

        val isImeSetUp by prefs.internal.isImeSetUp.observeAsState()

        CompositionLocalProvider(
            LocalNavController provides navController,
            LocalPreviewFieldController provides previewFieldController,
        ) {
            ProvideDefaultDialogPrefStrings(
                confirmLabel = stringRes(R.string.action__ok),
                dismissLabel = stringRes(R.string.action__cancel),
                neutralLabel = stringRes(R.string.action__default),
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
//                        .imePadding(),
                ) {
                    var showKeyboard by remember {
                        mutableStateOf(false)
                    }

                    BackHandler(enabled = showKeyboard) {
                        showKeyboard = false
                    }

                    Routes.AppNavHost(
                        modifier = Modifier.weight(1.0f),
                        navController = navController,
                        startDestination = if (isImeSetUp) Routes.Settings.Home else Routes.Setup.Screen,
                    )
                    PreviewKeyboardField(previewFieldController, openKeyboard = {
                        showKeyboard = it
                    })
//                    AndroidView(factory = { CustomEditText(it) }, modifier = Modifier.fillMaxWidth())
                    ImeUiWrapper(showKeyboard)
                }
            }
        }

        SideEffect {
            navController.setOnBackPressedDispatcher(this.onBackPressedDispatcher)
        }
    }

    @Composable
    private fun ImeUiWrapper(showKeyboard: Boolean) {
        AnimatedVisibility(
            visible = showKeyboard,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ProvideLocalizedResources(resourcesContext) {
                ProvideKeyboardRowBaseHeight {
                    CompositionLocalProvider(LocalInputActivityFeedbackController provides inputFeedbackController) {
                        FlorisImeTheme {
                            Column(modifier = Modifier.fillMaxWidth()) {
//                            if (!(isFullscreenUiMode && isExtractUiShown)) {
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .weight(1f),
//                                ) {
//                                    DevtoolsUi()
//                                }
//                            }
                                ImeUi()
                            }
                            SystemUiIme()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun ImeUi() {
        val state by keyboardManager.activeState.collectAsState()
        val keyboardStyle = FlorisImeTheme.style.get(
            element = FlorisImeUi.Keyboard,
            mode = state.inputShiftState.value,
        )
        val layoutDirection = LocalLayoutDirection.current
        SideEffect {
            if (keyboardManager.activeState.layoutDirection != layoutDirection) {
                keyboardManager.activeState.layoutDirection = layoutDirection
            }
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            SnyggSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onGloballyPositioned { coords -> inputViewSize = coords.size }
                    // Do not remove below line or touch input may get stuck
                    .pointerInteropFilter { false },
                style = keyboardStyle,
            ) {
                val configuration = LocalConfiguration.current
                val bottomOffset by if (configuration.isOrientationPortrait()) {
                    prefs.keyboard.bottomOffsetPortrait
                } else {
                    prefs.keyboard.bottomOffsetLandscape
                }.observeAsTransformingState { it.dp }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        // FIXME: removing this fixes the Smartbar sizing but breaks one-handed-mode
                        //.height(IntrinsicSize.Min)
                        .padding(bottom = bottomOffset),
                ) {
                    val oneHandedMode by prefs.keyboard.oneHandedMode.observeAsState()
                    val oneHandedModeScaleFactor by prefs.keyboard.oneHandedModeScaleFactor.observeAsState()
                    val keyboardWeight = when {
                        oneHandedMode == OneHandedMode.OFF || configuration.isOrientationLandscape() -> 1f
                        else -> oneHandedModeScaleFactor / 100f
                    }
                    if (oneHandedMode == OneHandedMode.END && configuration.isOrientationPortrait()) {
                        OneHandedPanel(
                            panelSide = OneHandedMode.START,
                            weight = 1f - keyboardWeight,
                        )
                    }
//                    CompositionLocalProvider(LocalTextInputService provides layoutDirection) {
                    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                        Box(
                            modifier = Modifier
                                .weight(keyboardWeight)
                                .wrapContentHeight(),
                        ) {
                            when (state.imeUiMode) {
                                ImeUiMode.TEXT -> TextInputLayout()
                                ImeUiMode.MEDIA -> MediaInputLayout()
                                ImeUiMode.CLIPBOARD -> ClipboardInputLayout()
                            }
                        }
                    }
//                    }
                    if (oneHandedMode == OneHandedMode.START && configuration.isOrientationPortrait()) {
                        OneHandedPanel(
                            panelSide = OneHandedMode.END,
                            weight = 1f - keyboardWeight,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DevtoolsUi() {
        val devtoolsEnabled by prefs.devtools.enabled.observeAsState()
        if (devtoolsEnabled) {
            DevtoolsOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}
