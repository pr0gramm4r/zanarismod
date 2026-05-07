package org.rsmod.content.interfaces.settings.configs

import java.util.WeakHashMap
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.game.entity.Player
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.game.type.varp.VarpType

public typealias setting_hooks = SettingHooks

public object SettingHooks {
    public val clickMappings: Map<SettingClick, Int> = buildMap {
        fun setting(category: Int, sub: Int, settingId: Int) {
            put(SettingClick(category, sub), settingId)
        }

        setting(category = 0, sub = 45, settingId = 5)
        setting(category = 0, sub = 46, settingId = 279)
        setting(category = 0, sub = 47, settingId = 280)
        setting(category = 0, sub = 50, settingId = 10)
        setting(category = 0, sub = 52, settingId = 299)
        setting(category = 0, sub = 53, settingId = 301)
        setting(category = 0, sub = 54, settingId = 300)
        setting(category = 0, sub = 70, settingId = 374)

        setting(category = 1, sub = 7, settingId = 477)
        setting(category = 1, sub = 5, settingId = 468)
        setting(category = 1, sub = 9, settingId = 479)
        setting(category = 1, sub = 10, settingId = 110)
        setting(category = 1, sub = 11, settingId = 480)
        setting(category = 1, sub = 12, settingId = 348)
        setting(category = 1, sub = 13, settingId = 481)
        setting(category = 1, sub = 14, settingId = 482)
        setting(category = 1, sub = 15, settingId = 33)
        setting(category = 1, sub = 16, settingId = 490)
        setting(category = 1, sub = 17, settingId = 491)
        setting(category = 1, sub = 18, settingId = 492)

        setting(category = 2, sub = 1, settingId = 433)
        setting(category = 2, sub = 2, settingId = 431)
        setting(category = 2, sub = 3, settingId = 44)
        setting(category = 2, sub = 4, settingId = 34)
        setting(category = 2, sub = 5, settingId = 35)
        setting(category = 2, sub = 6, settingId = 36)
        setting(category = 2, sub = 9, settingId = 82)
        setting(category = 2, sub = 10, settingId = 83)
        setting(category = 2, sub = 13, settingId = 83)
        setting(category = 2, sub = 14, settingId = 38)
        setting(category = 2, sub = 16, settingId = 40)
        setting(category = 2, sub = 18, settingId = 161)
        setting(category = 2, sub = 19, settingId = 162)
        setting(category = 2, sub = 20, settingId = 160)
        setting(category = 2, sub = 21, settingId = 195)
        setting(category = 2, sub = 22, settingId = 313)
        setting(category = 2, sub = 23, settingId = 314)
        setting(category = 2, sub = 26, settingId = 87)
        setting(category = 2, sub = 27, settingId = 89)
        setting(category = 2, sub = 28, settingId = 92)
        setting(category = 2, sub = 29, settingId = 94)
        setting(category = 2, sub = 30, settingId = 97)
        setting(category = 2, sub = 31, settingId = 99)
        setting(category = 2, sub = 32, settingId = 105)
        setting(category = 2, sub = 33, settingId = 196)
        setting(category = 2, sub = 36, settingId = 101)
        setting(category = 2, sub = 37, settingId = 103)
        setting(category = 2, sub = 38, settingId = 434)
        setting(category = 2, sub = 39, settingId = 107)
        setting(category = 2, sub = 59, settingId = 96)
        setting(category = 2, sub = 60, settingId = 109)

        setting(category = 3, sub = 1, settingId = 312)
        setting(category = 3, sub = 2, settingId = 372)
        setting(category = 3, sub = 3, settingId = 55)
        setting(category = 3, sub = 4, settingId = 56)
        setting(category = 3, sub = 5, settingId = 206)
        setting(category = 3, sub = 6, settingId = 48)
        setting(category = 3, sub = 7, settingId = 49)
        setting(category = 3, sub = 8, settingId = 51)
        setting(category = 3, sub = 14, settingId = 50)
        setting(category = 3, sub = 18, settingId = 207)
        setting(category = 3, sub = 41, settingId = 58)
        setting(category = 3, sub = 44, settingId = 57)
        setting(category = 3, sub = 45, settingId = 441)

        setting(category = 4, sub = 4, settingId = 7)
        setting(category = 4, sub = 5, settingId = 13)

        setting(category = 5, sub = 1, settingId = 387)
        setting(category = 5, sub = 2, settingId = 37)
        setting(category = 5, sub = 3, settingId = 59)
        setting(category = 5, sub = 4, settingId = 84)
        setting(category = 5, sub = 5, settingId = 305)
        setting(category = 5, sub = 23, settingId = 389)
        setting(category = 5, sub = 27, settingId = 476)
        setting(category = 5, sub = 35, settingId = 476)
        setting(category = 5, sub = 36, settingId = 436)
        setting(category = 5, sub = 47, settingId = 460)

        setting(category = 6, sub = 7, settingId = 8)
        setting(category = 6, sub = 9, settingId = 365)
        setting(category = 6, sub = 10, settingId = 171)
        setting(category = 6, sub = 11, settingId = 159)
        setting(category = 6, sub = 12, settingId = 205)
        setting(category = 6, sub = 14, settingId = 315)
        setting(category = 6, sub = 15, settingId = 493)
        setting(category = 6, sub = 16, settingId = 494)
        setting(category = 6, sub = 17, settingId = 442)
        setting(category = 6, sub = 18, settingId = 360)
        setting(category = 6, sub = 20, settingId = 472)
        setting(category = 6, sub = 22, settingId = 473)
        setting(category = 6, sub = 30, settingId = 181)
        setting(category = 6, sub = 31, settingId = 9)
        setting(category = 6, sub = 32, settingId = 257)
        setting(category = 6, sub = 39, settingId = 214)
        setting(category = 6, sub = 40, settingId = 215)
        setting(category = 6, sub = 41, settingId = 216)
        setting(category = 6, sub = 42, settingId = 217)
        setting(category = 6, sub = 43, settingId = 218)
        setting(category = 6, sub = 44, settingId = 219)
        setting(category = 6, sub = 45, settingId = 220)
        setting(category = 6, sub = 46, settingId = 221)
        setting(category = 6, sub = 48, settingId = 222)
        setting(category = 6, sub = 49, settingId = 239)
        setting(category = 6, sub = 50, settingId = 223)
        setting(category = 6, sub = 51, settingId = 224)
        setting(category = 6, sub = 52, settingId = 225)
        setting(category = 6, sub = 53, settingId = 226)
        setting(category = 6, sub = 54, settingId = 227)
        setting(category = 6, sub = 55, settingId = 228)

        setting(category = 7, sub = 7, settingId = 253)
        setting(category = 7, sub = 8, settingId = 254)
        setting(category = 7, sub = 25, settingId = 42)
        setting(category = 7, sub = 26, settingId = 43)
        setting(category = 7, sub = 27, settingId = 65)
        setting(category = 7, sub = 28, settingId = 66)
        setting(category = 7, sub = 30, settingId = 278)
        setting(category = 7, sub = 34, settingId = 192)
        setting(category = 7, sub = 37, settingId = 347)
        setting(category = 7, sub = 38, settingId = 424)
        setting(category = 7, sub = 55, settingId = 255)
        setting(category = 7, sub = 56, settingId = 308)
        setting(category = 7, sub = 57, settingId = 256)
        setting(category = 7, sub = 58, settingId = 309)
        setting(category = 7, sub = 59, settingId = 297)
        setting(category = 7, sub = 60, settingId = 298)

        setting(category = 8, sub = 6, settingId = 345)
        setting(category = 8, sub = 7, settingId = 349)
    }

    public val toggleSettings: Map<Int, ToggleSetting> =
        listOf(
                ToggleSetting(
                    5,
                    "hitsplat_tint_disabled",
                    VarBitStorage(varbits.hitsplat_tint_disabled),
                ),
                ToggleSetting(
                    7,
                    "option_hide_rooftops",
                    VarBitStorage(varbits.option_hide_rooftops),
                ),
                ToggleSetting(8, "orbs_disabled", VarBitStorage(varbits.orbs_disabled)),
                ToggleSetting(9, "wiki_icon_disabled", VarBitStorage(varbits.wiki_icon_disabled)),
                ToggleSetting(
                    10,
                    "hpbar_hud_boss_disabled",
                    VarBitStorage(varbits.hpbar_hud_boss_disabled),
                ),
                ToggleSetting(
                    13,
                    "camera_zoom_mouse_disabled",
                    VarBitStorage(varbits.camera_zoom_mouse_disabled),
                ),
                ToggleSetting(
                    33,
                    "music_unlock_text_toggle",
                    VarBitStorage(varbits.music_unlock_text_toggle),
                ),
                ToggleSetting(34, "option_chat", VarpStorage(varps.option_chat)),
                ToggleSetting(35, "option_pm", VarpStorage(varps.option_pm)),
                ToggleSetting(
                    36,
                    "hide_pm_alongside_chatbox",
                    VarBitStorage(varbits.hide_pm_alongside_chatbox),
                ),
                ToggleSetting(
                    37,
                    "option_chatfilter_disabled",
                    VarpStorage(varps.option_chatfilter_disabled),
                ),
                ToggleSetting(
                    38,
                    "option_lootnotification_on",
                    VarBitStorage(varbits.option_lootnotification_on),
                ),
                ToggleSetting(
                    40,
                    "option_lootnotification_untradeables",
                    VarBitStorage(varbits.option_lootnotification_untradeables),
                ),
                ToggleSetting(
                    42,
                    "option_dropwarning_on",
                    VarBitStorage(varbits.option_dropwarning_on),
                ),
                ToggleSetting(
                    44,
                    "loginlogout_setting",
                    VarBitStorage(varbits.loginlogout_setting),
                ),
                ToggleSetting(48, "option_mouse", VarpStorage(varps.option_mouse)),
                ToggleSetting(49, "mousecam_disabled", VarBitStorage(varbits.mousecam_disabled)),
                ToggleSetting(
                    50,
                    "followerops_deprioritised",
                    VarBitStorage(varbits.followerops_deprioritised),
                ),
                ToggleSetting(
                    51,
                    "desktop_shiftclickdrop_enabled",
                    VarBitStorage(varbits.desktop_shiftclickdrop_enabled),
                ),
                ToggleSetting(
                    57,
                    "keybinding_esc_to_close",
                    VarBitStorage(varbits.keybinding_esc_to_close),
                ),
                ToggleSetting(59, "option_acceptaid", VarBitStorage(varbits.option_acceptaid)),
                ToggleSetting(
                    65,
                    "alchemy_warning_untradeables",
                    VarBitStorage(varbits.alchemy_warning_untradeables),
                ),
                ToggleSetting(
                    82,
                    "option_precise_timing",
                    VarBitStorage(varbits.option_precise_timing),
                ),
                ToggleSetting(
                    83,
                    "option_separate_hours",
                    VarBitStorage(varbits.option_separate_hours),
                ),
                ToggleSetting(
                    84,
                    "gravestone_supplypiles_disabled",
                    VarBitStorage(varbits.gravestone_supplypiles_disabled),
                ),
                ToggleSetting(
                    111,
                    "hpbar_hud_standard_disabled",
                    VarBitStorage(varbits.hpbar_hud_standard_disabled),
                ),
                ToggleSetting(159, "ca_task_popup", VarBitStorage(varbits.ca_task_popup)),
                ToggleSetting(
                    160,
                    "ca_task_recompletion_notifications",
                    VarBitStorage(varbits.ca_task_recompletion_notifications),
                ),
                ToggleSetting(
                    161,
                    "ca_failure_notifications_enabled",
                    VarBitStorage(varbits.ca_failure_notifications_enabled),
                ),
                ToggleSetting(
                    162,
                    "ca_refailure_notifications_enabled",
                    VarBitStorage(varbits.ca_refailure_notifications_enabled),
                ),
                ToggleSetting(
                    171,
                    "option_collection_new_item",
                    VarBitStorage(varbits.option_collection_new_item),
                ),
                ToggleSetting(
                    181,
                    "tli_storebutton_toggle_desktop",
                    VarBitStorage(varbits.tli_storebutton_toggle_desktop),
                ),
                ToggleSetting(
                    192,
                    "gravestone_disable_warning",
                    VarBitStorage(varbits.gravestone_disable_warning),
                ),
                ToggleSetting(
                    195,
                    "option_chatbox_mode_autoset",
                    VarBitStorage(varbits.option_chatbox_mode_autoset),
                ),
                ToggleSetting(
                    205,
                    "trade_delay_disabled",
                    VarBitStorage(varbits.trade_delay_disabled),
                ),
                ToggleSetting(206, "skull_prevent_enabled", VarBitStorage(varbits.skull_prevent)),
                ToggleSetting(207, "runinvert_mode", VarBitStorage(varbits.runinvert_mode)),
                ToggleSetting(
                    215,
                    "questlist_hide_lackreqs",
                    VarBitStorage(varbits.questlist_hide_lackreqs),
                ),
                ToggleSetting(
                    216,
                    "questlist_hide_lackrecs",
                    VarBitStorage(varbits.questlist_hide_lackrecs),
                ),
                ToggleSetting(
                    217,
                    "questlist_hide_not_started",
                    VarBitStorage(varbits.questlist_hide_not_started),
                ),
                ToggleSetting(
                    218,
                    "questlist_hide_in_progress",
                    VarBitStorage(varbits.questlist_hide_in_progress),
                ),
                ToggleSetting(
                    219,
                    "questlist_hide_completed",
                    VarBitStorage(varbits.questlist_hide_completed),
                ),
                ToggleSetting(
                    220,
                    "questlist_hide_miniquests",
                    VarBitStorage(varbits.questlist_hide_miniquests),
                ),
                ToggleSetting(
                    221,
                    "questlist_hide_quests",
                    VarBitStorage(varbits.questlist_hide_quests),
                ),
                ToggleSetting(
                    222,
                    "questlist_larger_text",
                    VarBitStorage(varbits.questlist_larger_text),
                ),
                ToggleSetting(
                    223,
                    "questlist_disable_text_shadow",
                    VarBitStorage(varbits.questlist_disable_text_shadow),
                ),
                ToggleSetting(
                    239,
                    "questlist_disable_headers",
                    VarBitStorage(varbits.questlist_disable_headers),
                ),
                ToggleSetting(
                    253,
                    "wilderness_lever_blockwarning_standard",
                    VarBitStorage(varbits.wilderness_lever_blockwarning_standard),
                ),
                ToggleSetting(
                    254,
                    "wilderness_lever_blockwarning_highrisk",
                    VarBitStorage(varbits.wilderness_lever_blockwarning_highrisk),
                ),
                ToggleSetting(255, "wildy_hub_warning", VarBitStorage(varbits.wildy_hub_warning)),
                ToggleSetting(
                    256,
                    "wildy_canoe_warning",
                    VarBitStorage(varbits.wildy_canoe_warning),
                ),
                ToggleSetting(
                    257,
                    "option_content_recommender_hide",
                    VarBitStorage(varbits.option_content_recommender_hide),
                ),
                ToggleSetting(
                    278,
                    "option_item_retrieval_warning_disabled",
                    VarBitStorage(varbits.option_item_retrieval_warning_disabled),
                ),
                ToggleSetting(
                    279,
                    "hitsplat_maxhit_disabled",
                    VarBitStorage(varbits.hitsplat_maxhit_disabled),
                ),
                ToggleSetting(
                    297,
                    "option_ge_price_buy_warning_disabled",
                    VarBitStorage(varbits.option_ge_price_buy_warning_disabled),
                ),
                ToggleSetting(
                    298,
                    "option_ge_price_sell_warning_disabled",
                    VarBitStorage(varbits.option_ge_price_sell_warning_disabled),
                ),
                ToggleSetting(
                    299,
                    "hpbar_hud_boss_name_disabled",
                    VarBitStorage(varbits.hpbar_hud_boss_name_disabled),
                ),
                ToggleSetting(
                    300,
                    "hpbar_hud_boss_compact_enabled",
                    VarBitStorage(varbits.hpbar_hud_boss_compact_enabled),
                ),
                ToggleSetting(305, "take_ammo_toggle", VarBitStorage(varbits.take_ammo_toggle)),
                ToggleSetting(
                    308,
                    "wildy_hub_warning_highrisk",
                    VarBitStorage(varbits.wildy_hub_warning_highrisk),
                ),
                ToggleSetting(
                    309,
                    "wildy_canoe_warning_highrisk",
                    VarBitStorage(varbits.wildy_canoe_warning_highrisk),
                ),
                ToggleSetting(
                    312,
                    "tradeoption_disabled",
                    VarBitStorage(varbits.tradeoption_disabled),
                ),
                ToggleSetting(
                    313,
                    "purgeignored_permbanned_disabled",
                    VarBitStorage(varbits.purgeignored_permbanned_disabled),
                ),
                ToggleSetting(
                    314,
                    "purgeignored_permmuted_enabled",
                    VarBitStorage(varbits.purgeignored_permmuted_enabled),
                ),
                ToggleSetting(
                    315,
                    "option_level_up_message_disabled",
                    VarBitStorage(varbits.option_level_up_message_disabled),
                ),
                ToggleSetting(
                    347,
                    "worldswitcher_disable_confirmation",
                    VarBitStorage(varbits.worldswitcher_disable_confirmation),
                ),
                ToggleSetting(348, "music_enableloop", VarBitStorage(varbits.music_enableloop)),
                ToggleSetting(360, "option_skill_guide", VarBitStorage(varbits.option_skill_guide)),
                ToggleSetting(
                    362,
                    "compass_reorientation_setheight",
                    VarBitStorage(varbits.compass_reorientation_setheight),
                ),
                ToggleSetting(
                    365,
                    "crm_surprisepopup_blocked",
                    VarBitStorage(varbits.crm_surprisepopup_blocked),
                ),
                ToggleSetting(
                    371,
                    "settings_gravestone_autoequip",
                    VarBitStorage(varbits.settings_gravestone_autoequip),
                ),
                ToggleSetting(
                    372,
                    "rightclick_report_abuse_disabled",
                    VarBitStorage(varbits.rightclick_report_abuse_disabled),
                ),
                ToggleSetting(
                    374,
                    "option_trail_reminder_beginner",
                    VarBitStorage(varbits.option_trail_reminder_beginner),
                ),
                ToggleSetting(
                    382,
                    "settings_hit_sounds",
                    VarBitStorage(varbits.settings_hit_sounds),
                ),
                ToggleSetting(
                    387,
                    "settings_transmit_pronouns",
                    VarBitStorage(varbits.settings_transmit_pronouns),
                ),
                ToggleSetting(
                    424,
                    "bank_depositbox_oplocu_askquantity",
                    VarBitStorage(varbits.bank_depositbox_oplocu_askquantity),
                ),
                ToggleSetting(
                    431,
                    "clan_disable_lastseen",
                    VarBitStorage(varbits.clan_disable_lastseen),
                ),
                ToggleSetting(
                    433,
                    "settings_didyouknow_disabled",
                    VarBitStorage(varbits.settings_didyouknow_disabled),
                ),
                ToggleSetting(
                    436,
                    "option_camera_effect_snow_disabled",
                    VarBitStorage(varbits.option_camera_effect_snow_disabled),
                ),
                ToggleSetting(
                    441,
                    "settings_world_map_hotkey_disabled",
                    VarBitStorage(varbits.settings_world_map_hotkey_disabled),
                ),
                ToggleSetting(
                    442,
                    "settings_interface_resizing",
                    VarBitStorage(varbits.settings_interface_resizing),
                ),
                ToggleSetting(
                    460,
                    "settings_colourful_fade_disabled",
                    VarBitStorage(varbits.settings_colourful_fade_disabled),
                ),
                ToggleSetting(
                    472,
                    "settings_new_menu_interface",
                    VarBitStorage(varbits.settings_new_menu_interface),
                ),
                ToggleSetting(
                    473,
                    "settings_new_menu_transparent_interface_disabled",
                    VarBitStorage(varbits.settings_new_menu_transparent_interface_disabled),
                ),
                ToggleSetting(
                    474,
                    "settings_runepouch_loadout_names_disabled",
                    VarBitStorage(varbits.settings_runepouch_loadout_names_disabled),
                ),
                ToggleSetting(
                    476,
                    "settings_quetzalwhistle_default_tp",
                    VarBitStorage(varbits.settings_quetzalwhistle_default_tp),
                ),
                ToggleSetting(
                    479,
                    "use_previous_music_mode_on_login",
                    VarBitStorage(varbits.use_previous_music_mode_on_login),
                ),
                ToggleSetting(
                    480,
                    "settings_music_default_track_on_area_entry",
                    VarBitStorage(varbits.settings_music_default_track_on_area_entry),
                ),
                ToggleSetting(
                    481,
                    "dont_update_music_on_playlist_change",
                    VarBitStorage(varbits.dont_update_music_on_playlist_change),
                ),
                ToggleSetting(
                    482,
                    "use_shuffle_mode_on_manual_music_selection",
                    VarBitStorage(varbits.use_shuffle_mode_on_manual_music_selection),
                ),
                ToggleSetting(
                    493,
                    "option_level_up_chatbox_list",
                    VarBitStorage(varbits.option_level_up_chatbox_list),
                ),
                ToggleSetting(
                    494,
                    "option_level_up_guide_list_disabled",
                    VarBitStorage(varbits.option_level_up_guide_list_disabled),
                ),
            )
            .associateBy(ToggleSetting::settingId)

    public val numberSettings: Map<Int, NumberSetting> =
        listOf(
                NumberSetting(
                    settingId = 38,
                    internalName = "option_lootnotification_value",
                    storage = VarBitStorage(varbits.option_lootnotification_value),
                    prompt = "Set threshold value:",
                    min = 0,
                    max = 536_870_911,
                    enableToggleSettingId = 38,
                ),
                NumberSetting(
                    settingId = 43,
                    internalName = "option_dropwarning_value",
                    storage = VarBitStorage(varbits.option_dropwarning_value),
                    prompt = "Set threshold value:",
                    min = 0,
                    max = 536_870_911,
                    enableToggleSettingId = 42,
                ),
                NumberSetting(
                    settingId = 66,
                    internalName = "alchemy_warning_valuethreshold",
                    storage = VarBitStorage(varbits.alchemy_warning_valuethreshold),
                    prompt = "Set value threshold for alchemy warnings:",
                    min = 0,
                    max = 536_870_911,
                ),
                NumberSetting(
                    settingId = 280,
                    internalName = "settings_hitsplat_threshold",
                    storage = VarBitStorage(varbits.settings_hitsplat_threshold),
                    prompt = "Set value threshold for max hits (2-500):",
                    min = 10,
                    max = 511,
                ),
                NumberSetting(
                    settingId = 389,
                    internalName = "runenergy_autoenable",
                    storage = VarBitStorage(varbits.runenergy_autoenable),
                    prompt = "Set energy threshold for auto-enabling run mode:",
                    min = 0,
                    max = 100,
                ),
            )
            .associateBy(NumberSetting::settingId)

    public val dropdownSettings: Map<Int, IntSetting> =
        listOf(
                IntSetting(55, "option_attackpriority", VarpStorage(varps.option_attackpriority)),
                IntSetting(
                    56,
                    "option_attackpriority_npc",
                    VarpStorage(varps.option_attackpriority_npc),
                ),
                IntSetting(110, "music_area_mode", VarBitStorage(varbits.music_area_mode)),
                IntSetting(214, "questlist_sort_type", VarBitStorage(varbits.questlist_sort_type)),
                IntSetting(
                    301,
                    "hpbar_hud_boss_percentage_enabled",
                    VarBitStorage(varbits.hpbar_hud_boss_percentage_enabled),
                ),
                IntSetting(477, "musicplay", VarpStorage(varps.musicplay)),
                IntSetting(
                    478,
                    "music_current_playlist",
                    VarBitStorage(varbits.music_current_playlist),
                ),
            )
            .associateBy(IntSetting::settingId)

    public val colourSettings: Map<Int, ColourSetting> =
        listOf(
                ColourSetting(
                    87,
                    "option_chat_colour_public_opaque",
                    VarpStorage(varps.option_chat_colour_public_opaque),
                    defaultColour = 0x0000ff,
                ),
                ColourSetting(
                    89,
                    "option_chat_colour_private_opaque",
                    VarpStorage(varps.option_chat_colour_private_opaque),
                    defaultColour = 0x7f0000,
                ),
                ColourSetting(
                    92,
                    "option_chat_colour_autochat_opaque",
                    VarpStorage(varps.option_chat_colour_autochat_opaque),
                    defaultColour = 0x20202f,
                ),
                ColourSetting(
                    94,
                    "option_chat_colour_broadcast_opaque",
                    VarpStorage(varps.option_chat_colour_broadcast_opaque),
                    defaultColour = 0x000000,
                ),
                ColourSetting(
                    97,
                    "option_chat_colour_friendschat_opaque",
                    VarpStorage(varps.option_chat_colour_friendschat_opaque),
                    defaultColour = 0x7f007f,
                ),
                ColourSetting(
                    99,
                    "option_chat_colour_clanchat_opaque",
                    VarpStorage(varps.option_chat_colour_clanchat_opaque),
                    defaultColour = 0x000000,
                ),
                ColourSetting(
                    101,
                    "option_chat_colour_tradereq_opaque",
                    VarpStorage(varps.option_chat_colour_tradereq_opaque),
                    defaultColour = 0x7f007f,
                ),
                ColourSetting(
                    103,
                    "option_chat_colour_challengereq_opaque",
                    VarpStorage(varps.option_chat_colour_challengereq_opaque),
                    defaultColour = 0x7e3300,
                ),
                ColourSetting(
                    105,
                    "option_chat_colour_guestclan_opaque",
                    VarpStorage(varps.option_chat_colour_guestclan_opaque),
                    defaultColour = 0x7f00ff,
                ),
                ColourSetting(
                    196,
                    "option_chat_colour_clanbroadcast_opaque",
                    VarpStorage(varps.option_chat_colour_clanbroadcast_opaque),
                    defaultColour = 0x006600,
                ),
                ColourSetting(
                    434,
                    "option_chat_colour_didyouknow_opaque",
                    VarpStorage(varps.option_chat_colour_didyouknow_opaque),
                    defaultColour = 0xfff000,
                ),
                ColourSetting(
                    96,
                    "option_chat_colour_broadcast_split",
                    VarpStorage(varps.option_chat_colour_broadcast_split),
                    defaultColour = 0xfff000,
                ),
                ColourSetting(
                    224,
                    "questlist_colour_not_started",
                    VarpStorage(varps.questlist_colour_not_started),
                    defaultColour = 0xff0000,
                ),
                ColourSetting(
                    225,
                    "questlist_colour_in_progress",
                    VarpStorage(varps.questlist_colour_in_progress),
                    defaultColour = 0xfff000,
                ),
                ColourSetting(
                    226,
                    "questlist_colour_completed",
                    VarpStorage(varps.questlist_colour_completed),
                    defaultColour = 0x0dc10d,
                ),
                ColourSetting(
                    227,
                    "questlist_colour_unavailable",
                    VarpStorage(varps.questlist_colour_unavailable),
                    defaultColour = 0x9f9f9f,
                ),
            )
            .associateBy(ColourSetting::settingId)

    public val confirmationSettings: Map<Int, ConfirmationSetting> =
        listOf(
                ConfirmationSetting(
                    settingId = 107,
                    title = "Are you sure you want to reset your opaque chatbox colours?",
                    action = ConfirmationAction.ResetOpaqueChatColours,
                ),
                ConfirmationSetting(
                    settingId = 109,
                    title = "Are you sure you want to reset your split chat colours?",
                    action = ConfirmationAction.ResetSplitChatColours,
                ),
                ConfirmationSetting(
                    settingId = 228,
                    title = "Are you sure you want to reset your quest list text colours?",
                    action = ConfirmationAction.ResetQuestListColours,
                ),
                ConfirmationSetting(
                    settingId = 468,
                    title = "Are you sure you want to reset your volume sliders?",
                    action = ConfirmationAction.ResetVolumeSliders,
                ),
                ConfirmationSetting(
                    settingId = 490,
                    title = "Are you sure you want to wipe playlist 1?",
                    action = ConfirmationAction.NoOp,
                ),
                ConfirmationSetting(
                    settingId = 491,
                    title = "Are you sure you want to wipe playlist 2?",
                    action = ConfirmationAction.NoOp,
                ),
                ConfirmationSetting(
                    settingId = 492,
                    title = "Are you sure you want to wipe playlist 3?",
                    action = ConfirmationAction.NoOp,
                ),
                ConfirmationSetting(
                    settingId = 58,
                    title = "Are you sure you want to reset your keybinds?",
                    action = ConfirmationAction.NoOp,
                ),
            )
            .associateBy(ConfirmationSetting::settingId)

    public val textSettings: Map<Int, TextSetting> =
        listOf(
                TextSetting(
                    settingId = 345,
                    internalName = "option_loottracker_ignorelist",
                    storage = MemoryStringStorage(settingId = 345),
                    hasTextStorage = VarBitStorage(varbits.option_loottracker_ignorelist_has_text),
                    prompt = "Enter loot tracker ignored item names:",
                ),
                TextSetting(
                    settingId = 349,
                    internalName = "option_loottracker_ignorelist_sources",
                    storage = MemoryStringStorage(settingId = 349),
                    hasTextStorage = null,
                    prompt = "Enter loot tracker ignored source names:",
                ),
            )
            .associateBy(TextSetting::settingId)

    public fun findSettingId(category: Int, sub: Int): Int? =
        clickMappings[SettingClick(category, sub)]

    public fun toggle(player: Player, settingId: Int): Boolean {
        val setting = toggleSettings[settingId] ?: return false
        setting.set(player, if (setting.get(player) == 0) 1 else 0)
        return true
    }

    public fun setToggle(player: Player, settingId: Int, enabled: Boolean): Boolean {
        val setting = toggleSettings[settingId] ?: return false
        setting.set(player, if (enabled) 1 else 0)
        return true
    }

    public fun setNumber(player: Player, settingId: Int, value: Int): Boolean {
        val setting = numberSettings[settingId] ?: return false
        val normalized = value.coerceIn(setting.min, setting.max)
        setting.set(player, normalized)
        setting.enableToggleSettingId?.let { setToggle(player, it, enabled = normalized > 0) }
        return true
    }

    public fun setDropdown(player: Player, settingId: Int, value: Int): Boolean {
        val setting = dropdownSettings[settingId] ?: return false
        setting.set(player, value)
        return true
    }

    public fun setColour(player: Player, settingId: Int, colour: Int): Boolean {
        val setting = colourSettings[settingId] ?: return false
        setting.set(player, colour.coerceIn(0, MAX_RGB_COLOUR) + 1)
        return true
    }

    public fun resetColours(player: Player, settings: Iterable<Int>) {
        for (settingId in settings) {
            colourSettings[settingId]?.set(player, 0)
        }
    }

    public fun setText(player: Player, settingId: Int, text: String): Boolean {
        val setting = textSettings[settingId] ?: return false
        val normalized = normalizeCsvText(text)
        setting.set(player, normalized)
        return true
    }

    private fun normalizeCsvText(text: String): String {
        return text
            .replace(HtmlTagRegex, "")
            .lowercase()
            .split(',')
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { it.take(MAX_CSV_TEXT_ENTRY_LENGTH) }
            .distinct()
            .take(MAX_CSV_TEXT_ENTRIES)
            .joinToString(", ")
    }

    public const val MAX_RGB_COLOUR: Int = 0x00ffffff

    public val opaqueChatColourSettingIds: List<Int> =
        listOf(87, 89, 92, 94, 97, 99, 101, 103, 105, 196, 434)
    public val splitChatColourSettingIds: List<Int> = listOf(96)
    public val questListColourSettingIds: List<Int> = listOf(224, 225, 226, 227)

    private const val MAX_CSV_TEXT_ENTRIES = 50
    private const val MAX_CSV_TEXT_ENTRY_LENGTH = 64
    private val HtmlTagRegex = Regex("<[^>]*>")
}

public data class SettingClick(public val category: Int, public val sub: Int)

public data class ToggleSetting(
    public val settingId: Int,
    public val internalName: String,
    private val storage: IntSettingStorage,
) {
    public fun get(player: Player): Int = storage.get(player)

    public fun set(player: Player, value: Int) {
        storage.set(player, value)
    }
}

public data class NumberSetting(
    public val settingId: Int,
    public val internalName: String,
    private val storage: IntSettingStorage,
    public val prompt: String,
    public val min: Int,
    public val max: Int,
    public val enableToggleSettingId: Int? = null,
) {
    public fun get(player: Player): Int = storage.get(player)

    public fun set(player: Player, value: Int) {
        storage.set(player, value)
    }
}

public data class IntSetting(
    public val settingId: Int,
    public val internalName: String,
    private val storage: IntSettingStorage,
) {
    public fun get(player: Player): Int = storage.get(player)

    public fun set(player: Player, value: Int) {
        storage.set(player, value)
    }
}

public data class ColourSetting(
    public val settingId: Int,
    public val internalName: String,
    private val storage: IntSettingStorage,
    public val defaultColour: Int,
) {
    public fun get(player: Player): Int = storage.get(player)

    public fun getPickerColour(player: Player): Int {
        val stored = get(player)
        return if (stored > 0) stored - 1 else defaultColour
    }

    public fun set(player: Player, value: Int) {
        storage.set(player, value)
    }
}

public data class ConfirmationSetting(
    public val settingId: Int,
    public val title: String,
    public val action: ConfirmationAction,
)

public enum class ConfirmationAction {
    NoOp,
    ResetOpaqueChatColours,
    ResetSplitChatColours,
    ResetQuestListColours,
    ResetVolumeSliders,
}

public data class TextSetting(
    public val settingId: Int,
    public val internalName: String,
    private val storage: StringSettingStorage,
    private val hasTextStorage: IntSettingStorage?,
    public val prompt: String,
) {
    public fun get(player: Player): String? = storage.get(player)

    public fun set(player: Player, value: String) {
        storage.set(player, value)
        hasTextStorage?.set(player, if (value.isNotEmpty()) 1 else 0)
    }
}

public sealed interface IntSettingStorage {
    public fun get(player: Player): Int

    public fun set(player: Player, value: Int)
}

public sealed interface StringSettingStorage {
    public fun get(player: Player): String?

    public fun set(player: Player, value: String?)
}

private data class VarBitStorage(private val varbit: VarBitType) : IntSettingStorage {
    override fun get(player: Player): Int = player.vars[varbit]

    override fun set(player: Player, value: Int) {
        VarPlayerIntMapSetter.set(player, varbit, value)
    }
}

private data class VarpStorage(private val varp: VarpType) : IntSettingStorage {
    override fun get(player: Player): Int = player.vars[varp]

    override fun set(player: Player, value: Int) {
        VarPlayerIntMapSetter.set(player, varp, value)
    }
}

private data class MemoryStringStorage(private val settingId: Int) : StringSettingStorage {
    override fun get(player: Player): String? = backing[player]?.get(settingId)

    override fun set(player: Player, value: String?) {
        if (value == null) {
            backing[player]?.remove(settingId)
        } else {
            backing.getOrPut(player) { mutableMapOf() }[settingId] = value
        }
    }

    private companion object {
        private val backing = WeakHashMap<Player, MutableMap<Int, String>>()
    }
}
