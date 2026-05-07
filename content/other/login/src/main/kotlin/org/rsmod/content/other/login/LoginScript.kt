package org.rsmod.content.other.login

import jakarta.inject.Inject
import net.rsprot.protocol.game.outgoing.misc.client.HideLocOps
import net.rsprot.protocol.game.outgoing.misc.client.HideNpcOps
import net.rsprot.protocol.game.outgoing.misc.client.HideObjOps
import net.rsprot.protocol.game.outgoing.misc.client.MinimapToggle
import net.rsprot.protocol.game.outgoing.misc.client.ResetAnims
import net.rsprot.protocol.game.outgoing.misc.player.ChatFilterSettings
import net.rsprot.protocol.game.outgoing.varp.VarpReset
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.inv.weight.InvWeight
import org.rsmod.api.player.output.Camera
import org.rsmod.api.player.output.ChatType
import org.rsmod.api.player.output.MiscOutput
import org.rsmod.api.player.output.UpdateRun
import org.rsmod.api.player.output.UpdateStat
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.output.runClientScript
import org.rsmod.api.player.startInvTransmit
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.player.vars.resyncVar
import org.rsmod.api.realm.Realm
import org.rsmod.api.script.onEvent
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.player.SessionStateEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.game.type.varp.UnpackedVarpType
import org.rsmod.game.type.varp.VarpType
import org.rsmod.game.type.varp.VarpTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class LoginScript
@Inject
constructor(
    private val realm: Realm,
    private val mapClock: MapClock,
    private val objTypes: ObjTypeList,
    private val varpTypes: VarpTypeList,
    private val statTypes: StatTypeList,
    private val invisibleLevels: InvisibleLevels,
) : PluginScript() {
    private val transmitVars by lazy { transmitVars() }

    private var Player.chatboxUnlocked: Boolean by boolVarBit(varbits.has_displayname_transmitter)

    override fun ScriptContext.startup() {
        onEvent<SessionStateEvent.EngineLogin>(0L) { player.engineLogin() }
    }

    private fun Player.engineLogin() {
        sendHighPriority()
        sendLowPriority()
    }

    private fun Player.sendHighPriority() {
        sendChatFilters()
        sendOpVisibility()
        sendWelcomeMessage()
        sendVars()
    }

    private fun Player.sendChatFilters() {
        client.write(ChatFilterSettings(0, 0))
    }

    private fun Player.sendOpVisibility() {
        client.write(HideNpcOps(false))
        client.write(HideLocOps(false))
        client.write(HideObjOps(false))
    }

    private fun Player.sendWelcomeMessage() {
        val message = realm.config.loginMessage
        message?.let { mes(it, ChatType.Welcome) }

        val broadcast = realm.config.loginBroadcast
        broadcast?.let { mes(it, ChatType.Broadcast) }
    }

    private fun Player.sendVars() {
        client.write(VarpReset)
        chatboxUnlocked = displayName.isNotBlank()
        setDefaultVarBit(varbits.option_hide_rooftops, DEFAULT_HIDE_ROOFS)
        setMinimumVarBit(varbits.settings_hitsplat_threshold, DEFAULT_HITSPLAT_THRESHOLD)
        setDefaultAudioOptions()
        for (varp in transmitVars) {
            if (varp in vars) {
                resyncVar(varp)
            }
        }
    }

    private fun Player.setDefaultAudioOptions() {
        setDefaultVarp(varps.option_master_volume, DEFAULT_AUDIO_VOLUME)
        setDefaultVarp(varps.option_music, DEFAULT_AUDIO_VOLUME)
        setDefaultVarp(varps.option_sounds, DEFAULT_AUDIO_VOLUME)
        setDefaultVarp(varps.option_areasounds, DEFAULT_AUDIO_VOLUME)
        setDefaultDesktopAudioOptions()
        setUnmuteAudioSavedOptions()
    }

    private fun Player.setDefaultDesktopAudioOptions() {
        val desktopAudio = varbits.option_master_volume_desktop.baseVar
        if (desktopAudio !in vars) {
            setVarBit(varbits.option_master_volume_desktop, DEFAULT_AUDIO_VOLUME)
            setVarBit(varbits.option_music_desktop, DEFAULT_AUDIO_VOLUME)
            setVarBit(varbits.option_master_volume_saved_desktop, DEFAULT_UNMUTE_VOLUME)
            setVarBit(varbits.option_music_saved_desktop, DEFAULT_UNMUTE_VOLUME)
        }

        val desktopEffects = varbits.option_sounds_desktop.baseVar
        if (desktopEffects !in vars) {
            setVarBit(varbits.option_sounds_desktop, DEFAULT_AUDIO_VOLUME)
            setVarBit(varbits.option_areasounds_desktop, DEFAULT_AUDIO_VOLUME)
            setVarBit(varbits.option_sounds_saved_desktop, DEFAULT_UNMUTE_VOLUME)
            setVarBit(varbits.option_areasounds_saved_desktop, DEFAULT_UNMUTE_VOLUME)
        }
    }

    private fun Player.setUnmuteAudioSavedOptions() {
        setVarBit(varbits.option_master_volume_saved_desktop, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_music_saved_desktop, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_sounds_saved_desktop, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_areasounds_saved_desktop, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_master_volume_saved, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_music_saved, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_sounds_saved, DEFAULT_UNMUTE_VOLUME)
        setVarBit(varbits.option_areasounds_saved, DEFAULT_UNMUTE_VOLUME)
    }

    private fun Player.setDefaultVarp(varp: VarpType, value: Int) {
        if (varp !in vars) {
            VarPlayerIntMapSetter.set(this, varp, value)
        }
    }

    private fun Player.setDefaultVarBit(varbit: VarBitType, value: Int) {
        if (varbit.baseVar !in vars) {
            setVarBit(varbit, value)
        }
    }

    private fun Player.setMinimumVarBit(varbit: VarBitType, value: Int) {
        if (vars[varbit] < value) {
            setVarBit(varbit, value)
        }
    }

    private fun Player.setVarBit(varbit: VarBitType, value: Int) {
        VarPlayerIntMapSetter.set(this, varbit, value)
    }

    private fun Player.sendLowPriority() {
        sendInvs()
        runClientScript(LOGIN_CLIENTSCRIPT)
        runClientScript(2498, 1, 0, 0)
        resetCam()
        runClientScript(828, 1)
        runClientScript(5141)
        sendPlayerOps()
        runClientScript(876, mapClock.cycle, 0, displayName, "REGULAR")
        sendStats()
        sendRun()
        client.write(ResetAnims)
        client.write(MinimapToggle(0))
    }

    private fun Player.sendInvs() {
        startInvTransmit(inv)
        startInvTransmit(worn)
    }

    private fun Player.resetCam() {
        Camera.camReset(this)
    }

    private fun Player.sendStats() {
        for (stat in statTypes.values) {
            val currXp = statMap.getXP(stat)
            val currLvl = stat(stat)
            val hiddenLvl = currLvl + invisibleLevels.get(this, stat)
            UpdateStat.update(this, stat, currXp, currLvl, hiddenLvl)
        }
    }

    private fun Player.sendRun() {
        val weightInGrams = InvWeight.calculateWeightInGrams(this, objTypes)
        runWeight = weightInGrams
        UpdateRun.weight(this, kg = weightInGrams / 1000)
        UpdateRun.energy(this, runEnergy)
    }

    private fun Player.sendPlayerOps() {
        MiscOutput.setPlayerOp(this, slot = 2, op = null)
        MiscOutput.setPlayerOp(this, slot = 3, op = "Follow")
        MiscOutput.setPlayerOp(this, slot = 4, op = "Trade with")
        MiscOutput.setPlayerOp(this, slot = 5, op = null)
        MiscOutput.setPlayerOp(this, slot = 8, op = "Report")
    }

    private fun transmitVars(): List<UnpackedVarpType> {
        return varpTypes.filterTransmitKeys().sorted().map(varpTypes::getValue)
    }

    private companion object {
        /**
         * Initializes rev237 camera zoom bounds. Without this, mouse-wheel camera zoom clamps
         * against zeroed client varcs and appears disabled.
         */
        private const val LOGIN_CLIENTSCRIPT = 626

        private const val DEFAULT_AUDIO_VOLUME = 100
        private const val DEFAULT_UNMUTE_VOLUME = 5
        private const val DEFAULT_HIDE_ROOFS = 1
        private const val DEFAULT_HITSPLAT_THRESHOLD = 10
    }
}
