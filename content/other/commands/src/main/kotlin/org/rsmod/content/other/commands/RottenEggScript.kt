package org.rsmod.content.other.commands

import org.rsmod.api.config.refs.modlevels
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeld1
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class RottenEggScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeld1(objs.rotten_egg) { scramble() }
    }

    private suspend fun ProtectedAccess.scramble() {
        if (!player.modLevel.hasAccessTo(modlevels.admin)) {
            player.mes("Nothing interesting happens.")
            return
        }

        teleportMenu()
    }

    private suspend fun ProtectedAccess.teleportMenu() {
        val categoryIndex =
            menu(
                "Teleports",
                hotkeys = false,
                choices = RottenEggTeleports.categories.map { it.label },
            )
        val category = RottenEggTeleports.categories.getOrNull(categoryIndex) ?: return
        if (category.closeOnly) {
            ifClose()
            return
        }

        val section =
            when (category.sections.size) {
                0 -> {
                    ifClose()
                    return
                }
                1 -> category.sections.single()
                else -> {
                    val sectionIndex =
                        menu(
                            category.label,
                            hotkeys = false,
                            choices = category.sections.map { it.label },
                        )
                    category.sections.getOrNull(sectionIndex) ?: return
                }
            }
        teleportFromSection(section)
    }

    private suspend fun ProtectedAccess.teleportFromSection(section: RottenEggTeleportSection) {
        val locationIndex =
            menu(section.label, hotkeys = false, choices = section.locations.map { it.label })
        val location = section.locations.getOrNull(locationIndex) ?: return
        ifClose()
        telejump(location.coords)
    }
}
