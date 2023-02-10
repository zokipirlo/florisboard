package dev.patrickgold.florisboard.app.settings.theme

import androidx.compose.runtime.Composable
import dev.patrickgold.florisboard.core.R
import dev.patrickgold.florisboard.core.lib.compose.stringRes
import dev.patrickgold.florisboard.core.lib.snygg.SnyggLevel
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries

object SnyggLevelEntries {
    @Composable
    fun listEntries() = listPrefEntries {
        entry(
            key = SnyggLevel.BASIC,
            label = stringRes(R.string.enum__snygg_level__basic),
            description = stringRes(R.string.enum__snygg_level__basic__description),
            showDescriptionOnlyIfSelected = true,
        )
        entry(
            key = SnyggLevel.ADVANCED,
            label = stringRes(R.string.enum__snygg_level__advanced),
            description = stringRes(R.string.enum__snygg_level__advanced__description),
            showDescriptionOnlyIfSelected = true,
        )
        entry(
            key = SnyggLevel.DEVELOPER,
            label = stringRes(R.string.enum__snygg_level__developer),
            description = stringRes(R.string.enum__snygg_level__developer__description),
            showDescriptionOnlyIfSelected = true,
        )
    }
}
