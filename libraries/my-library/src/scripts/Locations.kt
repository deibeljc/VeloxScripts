package scripts

import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.WorldTile

enum class Monster(
    val monsterName: String,
    val combatLevel: Int,
    val hitpoints: Int,
    val location: String,
    val advantages: List<String>,
    val disadvantages: List<String>
) {
  CHICKEN(
      "Chicken",
      1,
      3,
      "North-east of Lumbridge just east of River Lum, south of River Lum in Farmer Fred's chicken coop",
      listOf(
          "No food required.",
          "Feathers are dropped in groups of 5-15 and are stackable, and can be sold for some profit.",
          "Bones will drop frequently, allowing the player to train their Prayer as well."),
      listOf(
          "Only 3 HP (12xp) per kill. Once you start hitting 4's with your Strength and weapon, chickens are no longer a good melee training monster but they are still good for training Prayer and collecting feathers.",
          "Non-aggressive.")),
  MONK(
      "Monk",
      5,
      15,
      "Edgeville Monastery",
      listOf(
          "No food required as you can ask monks to heal you for free.",
          "Good training for 1 Defence pures.",
          "15 HP (60xp) per kill plus more experience per kill if they heal themselves for 2 HP. Players can continue killing monks towards Level 20 melee skills."),
      listOf("No drops other than bones", "Non-aggressive.")),
  SEAGULL(
      "Seagull",
      3,
      10,
      "Port Sarim ports, Corsair Cove",
      listOf(
          "10 HP (40xp) per kill from the level 3 seagulls.",
          "Seagulls cannot hit you, making it viable for no-damage accounts."),
      listOf(
          "No drops other than bones.",
          "A long death animation before dropping bones.",
          "Non-aggressive.")),
  GIANT_RAT(
      "Giant rat",
      6,
      10,
      "Lumbridge Swamp, Edgeville Dungeon, Varrock Sewers",
      listOf(
          "They always drop raw rat meat, which can be cooked for Cooking XP and heal for 3 hitpoints."),
      listOf(
          "No valuable drops.",
          "Lumbridge Swamp may be crowded and giant rats are slightly distanced from each other.",
          "They have a tendency to hit 1's on players with little defence and no armour.",
          "Non-aggressive.")),
  GIANT_FROG(
      "Giant frog",
      13,
      23,
      "Lumbridge Swamp",
      listOf(
          "High HP (23 HP for 92 melee experience per kill), low defence, and they deal little damage.",
          "Always drops big bones which can be sold for guaranteed profit or used for Prayer experience while training.",
          "They rarely drop beginner clue scrolls."),
      listOf(
          "Sometimes crowded.", "No valuable drops other than a beginner clue scroll to solve.")),
  FLESH_CRAWLER(
      "Flesh Crawler",
      41,
      25,
      "Catacomb of Famine (level 2) of Stronghold of Security",
      listOf(
          "They only deal 1 HP of damage but higher level Flesh Crawlers can hit quite accurately.",
          "Always aggressive towards players of all levels, making this a great afk training method at any level.",
          "They have access to the gem drop table and rarely drop uncut gems."),
      listOf(
          "No bone drops.",
          "Higher level Flesh Crawlers will quickly damage players, even if it is just 1 HP per hit as they have a very fast attack speed of 1.8 seconds per attack.",
          "Not as profitable in free-to-play as other monsters around these levels. They are only profitable in member worlds.")),
  ZOMBIE(
      "Zombie",
      53,
      50,
      "Catacomb of Famine (level 2) of Stronghold of Security",
      listOf(
          "The zombies in the Stronghold of Security are a decent alternative to Flesh Crawlers.",
          "They are aggressive towards players up to twice their combat level + 1.",
          "They have access to the gem drop table and very rarely drop uncut gems."),
      listOf(
          "By Combat Level 107, zombies in the Stronghold of Security are no longer aggressive.",
          "Not as profitable in free-to-play as other monsters around these levels. They are only profitable in member worlds.")),
  GIANT_SPIDER(
      "Giant spider",
      50,
      50,
      "Pit of Pestilence (level 3) of Stronghold of Security",
      listOf(
          "Always aggressive towards players of all levels, making this a great afk training method at any level to 99 and beyond.",
          "After aggression ends in 10 minutes, players can re-aggro them by running to specific corners of the room."),
      listOf(
          "Giant spiders can sometimes hit up to a maximum of 7 points of damage.",
          "No drops other than beginner clue scrolls.",
          "Unattended players could still possibly die before aggression ends."))
}

data class MonsterArea(
    val name: String,
    val minLevel: Int,
    val maxLevel: Int,
    val monsters: List<Monster>,
    var area: Area? = null,
    val treeLocation: WorldTile,
    val requirements: String,
    val recommendations: String
)

object Locations {
  private val areas =
      listOf(
          MonsterArea(
              "Farmer Fred's chicken coop",
              1,
              20,
              listOf(Monster.CHICKEN),
              Area.fromPolygon(
                  WorldTile(3184, 3277, 0),
                  WorldTile(3199, 3277, 0),
                  WorldTile(3201, 3265, 0),
                  WorldTile(3197, 3256, 0),
                  WorldTile(3187, 3256, 0),
                  WorldTile(3184, 3269, 0)),
              WorldTile(3205, 3263),
              "None",
              "Your best weapon and armour."),
          MonsterArea(
              "Lumbridge Swamp",
              1,
              20,
              listOf(Monster.GIANT_RAT),
              Area.fromPolygon(
                  WorldTile(3212, 3196, 0),
                  WorldTile(3224, 3196, 0),
                  WorldTile(3233, 3188, 0),
                  WorldTile(3234, 3172, 0),
                  WorldTile(3217, 3159, 0),
                  WorldTile(3188, 3159, 0),
                  WorldTile(3165, 3159, 0),
                  WorldTile(3161, 3203, 0),
                  WorldTile(3183, 3204, 0),
                  WorldTile(3195, 3196, 0)),
              WorldTile(3215, 3183),
              "None",
              "Your best weapon and armour. Food may be required but they drop raw rat meat. Small fishing net, tinderbox, and an axe may be optional to fish shrimps south-east of Lumbridge Swamp."),
          MonsterArea(
              "Edgeville Monastery",
              1,
              20,
              listOf(Monster.MONK),
              Area.fromPolygon(
                  WorldTile(3050, 3472, 0),
                  WorldTile(3040, 3477, 0),
                  WorldTile(3039, 3497, 0),
                  WorldTile(3042, 3509, 0),
                  WorldTile(3053, 3511, 0),
                  WorldTile(3061, 3508, 0),
                  WorldTile(3064, 3491, 0),
                  WorldTile(3062, 3480, 0),
                  WorldTile(3059, 3474, 0)),
              WorldTile(3055, 3485),
              "None",
              ""),
          MonsterArea(
              "Lumbridge Swamp",
              20,
              40,
              listOf(Monster.GIANT_FROG),
              Area.fromPolygon(
                  WorldTile(3212, 3196, 0),
                  WorldTile(3224, 3196, 0),
                  WorldTile(3233, 3188, 0),
                  WorldTile(3234, 3172, 0),
                  WorldTile(3217, 3159, 0),
                  WorldTile(3188, 3159, 0),
                  WorldTile(3165, 3159, 0),
                  WorldTile(3161, 3203, 0),
                  WorldTile(3183, 3204, 0),
                  WorldTile(3195, 3196, 0)),
              WorldTile(3215, 3183),
              "None",
              "Bring your best armour (chainbody recommended) and weapons."),
          MonsterArea(
              "Stronghold of Security - Level 2",
              40,
              70,
              listOf(Monster.FLESH_CRAWLER, Monster.ZOMBIE),
              null,
              WorldTile(3215, 3183),
              "Travelling through Vault of War (level 1) once. Decent combat.",
              "Bring your best armour and weapons, and food. 40+ Defence is recommended."),
          MonsterArea(
              "Stronghold of Security - Level 3",
              60,
              99,
              listOf(Monster.GIANT_SPIDER),
              null,
              WorldTile(3215, 3183),
              "Travelling through Vault of War (level 1) and Catacomb of Famine (level 2) once. High-level combat.",
              "If fishing for food by the riverbank of Barbarian Village, bring a fly fishing rod and feather. Prioritize salmon over trout. If retrieving food from the Edgeville bank for longer trips, bring 1 or 2 strength potions and the rest should be anchovy pizzas or swordfish."))

  fun getBestTrainingArea(): MonsterArea {
    val playerLevel = MyPlayer.getCombatLevel()
    val suitableAreas = areas.filter { playerLevel >= it.minLevel && playerLevel <= it.maxLevel }

    return if (suitableAreas.isNotEmpty()) {
      suitableAreas.minByOrNull { area ->
        area.area?.center?.distanceTo(MyPlayer.getTile())?.toDouble() ?: Double.MAX_VALUE
      } ?: suitableAreas.first()
    } else {
      // If no suitable areas, return the closest area regardless of level
      areas.minByOrNull { area ->
        area.area?.center?.distanceTo(MyPlayer.getTile())?.toDouble() ?: Double.MAX_VALUE
      } ?: areas.last() // Default to the last area if all locations are null
    }
  }

  fun getMonsterInfo(monster: Monster): String {
    return """
            Monster: ${monster.monsterName}
            Combat Level: ${monster.combatLevel}
            Hitpoints: ${monster.hitpoints}
            Location: ${monster.location}
            
            Advantages:
            ${monster.advantages.joinToString("\n") { "- $it" }}
            
            Disadvantages:
            ${monster.disadvantages.joinToString("\n") { "- $it" }}
        """
        .trimIndent()
  }
}
