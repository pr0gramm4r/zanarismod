package org.rsmod.game.type.obj

public enum class WeaponCategory(public val id: Int, public val text: String) {
    Unarmed(0, "Unarmed"),
    Axe(1, "Axe"),
    Blunt(2, "Blunt"),
    Bow(3, "Bow"),
    Claw(4, "Claw"),
    Crossbow(5, "Crossbow"),
    Salamander(6, "Salamander"),
    Chinchompas(7, "Chinchompas"),
    Gun(8, "Gun"),
    SlashSword(9, "Slash Sword"),
    TwoHandedSword(10, "2h sword"),
    Pickaxe(11, "Pickaxe"),
    Polearm(12, "Polearm"),
    Polestaff(13, "Polestaff"),
    Scythe(14, "Scythe"),
    Spear(15, "Spear"),
    Spiked(16, "Spiked"),
    StabSword(17, "Stab Sword"),
    Staff(18, "Staff"),
    Thrown(19, "Thrown"),
    Whip(20, "Whip"),
    BladedStaff(21, "Bladed Staff"),
    StaffSpellblade(22, "Spellblade"),
    // When swapping to a godsword, the associated op content script seems to explicitly re-set varp
    // 357 (current weapon category for the tab interface) from `10` to `23`.
    GodSword(23, "2h sword"),
    PoweredStaff(24, "Powered Staff"),
    Banner(25, "Banner"),
    ChargeSpear(26, "Charge Spear"),
    Bludgeon(27, "Bludgeon"),
    Bulwark(28, "Bulwark"),
    PoweredWand(29, "Powered Wand"),
    Partisan(30, "Partisan"),
    Tribrid(31, "Tribrid"),
    Egg(32, "Egg"),
    SailingCannon(33, "Sailing Cannon"),
    MultiMelee(34, "Melee");

    public companion object {
        public fun getOrUnarmed(id: Int?): WeaponCategory =
            if (id == null) {
                Unarmed
            } else {
                this[id] ?: Unarmed
            }

        public operator fun get(id: Int): WeaponCategory? =
            when (id) {
                Unarmed.id -> Unarmed
                Axe.id -> Axe
                Blunt.id -> Blunt
                Bow.id -> Bow
                Claw.id -> Claw
                Crossbow.id -> Crossbow
                Salamander.id -> Salamander
                Chinchompas.id -> Chinchompas
                Gun.id -> Gun
                SlashSword.id -> SlashSword
                TwoHandedSword.id -> TwoHandedSword
                Pickaxe.id -> Pickaxe
                Polearm.id -> Polearm
                Polestaff.id -> Polestaff
                Scythe.id -> Scythe
                Spear.id -> Spear
                Spiked.id -> Spiked
                StabSword.id -> StabSword
                Staff.id -> Staff
                Thrown.id -> Thrown
                Whip.id -> Whip
                BladedStaff.id -> BladedStaff
                StaffSpellblade.id -> StaffSpellblade
                Banner.id -> Banner
                GodSword.id -> GodSword
                PoweredStaff.id -> PoweredStaff
                ChargeSpear.id -> ChargeSpear
                Bludgeon.id -> Bludgeon
                Bulwark.id -> Bulwark
                PoweredWand.id -> PoweredWand
                Partisan.id -> Partisan
                Tribrid.id -> Tribrid
                Egg.id -> Egg
                SailingCannon.id -> SailingCannon
                MultiMelee.id -> MultiMelee
                else -> null
            }
    }
}
