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
          "Unattended players could still possibly die before aggression ends.")),
  HILL_GIANT(
      "Hill Giant",
      28,
      35,
      "Edgeville Dungeon, Giants' Plateau",
      listOf(
          "Decently high HP for level (35).",
          "They have decent profitable drops for lower-level players such as limpwurt roots and rare uncut gems.",
          "Chance of dropping a giant key, which allows the player to fight Obor.",
          "They sometimes drop law runes, cosmic runes, nature runes, low-level equipment, and rarely chaos runes and death runes.",
          "Always drop big bones which can be sold for guaranteed profit or used for Prayer experience while training.",
          "Faster aggression reset than Stronghold of Security, due to no doors."),
      listOf(
          "Highly crowded in most worlds.",
          "They can be a bit rough and tough against lower levels.")),
  MOSS_GIANT(
      "Moss Giant",
      42,
      60,
      "Varrock Sewers, Crandor",
      listOf(
          "Fairly high HP for level.",
          "Decent alchable drops such as black sq shield, mithril sword, and steel kiteshield.",
          "Good rune drops such as law runes, nature runes, chaos runes, cosmic runes, and rarely death runes.",
          "Chance of dropping a mossy key, which allows the player to fight Bryophyta.",
          "Always drops big bones which can be sold for guaranteed profit or used for Prayer experience while training.",
          "They have access to the gem drop table and rarely drop uncut gems."),
      listOf(
          "Varrock Sewers are often crowded.",
          "Hits hard if you have low Defence or weak armour.",
          "All of their (F2P) locations are far from a bank")),
}

data class MonsterArea(
    val name: String,
    val minLevel: Int,
    val maxLevel: Int,
    val monsters: List<Monster>,
    var area: Area? = null,
    var fishingArea: Area,
    val treeLocation: WorldTile,
    var requiredItems: List<List<String>> = listOf(),
    val requirements: String,
    val recommendations: String
)

val axe = listOf("rune axe", "adamant axe", "mithril axe", "steel axe", "iron axe", "bronze axe")
val tinderbox = listOf("tinderbox")
val fishingnet = listOf("small fishing net", "big fishing net")
val flyfishingrod = listOf("fly fishing rod")
val feathers = listOf("feather")
val fishingrod = listOf("fishing rod")
val bait = listOf("bait")

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
              Area.fromPolygon(),
              WorldTile(3205, 3263),
              listOf(),
              "None",
              "Your best weapon and armour."),
          MonsterArea(
              "Lumbridge Swamp",
              1,
              20,
              listOf(Monster.GIANT_RAT),
              Area.fromPolygon(
                  WorldTile(3155, 3207),
                  WorldTile(3146, 3190),
                  WorldTile(3154, 3176),
                  WorldTile(3157, 3163),
                  WorldTile(3169, 3152),
                  WorldTile(3204, 3147),
                  WorldTile(3221, 3152),
                  WorldTile(3231, 3152),
                  WorldTile(3234, 3159),
                  WorldTile(3240, 3171),
                  WorldTile(3243, 3183),
                  WorldTile(3243, 3188),
                  WorldTile(3235, 3190),
                  WorldTile(3228, 3194),
                  WorldTile(3223, 3198),
                  WorldTile(3206, 3197),
                  WorldTile(3193, 3196),
                  WorldTile(3185, 3205),
                  WorldTile(3178, 3206)),
              Area.fromPolygon(
                  listOf(
                      WorldTile(3246, 3159, 0),
                      WorldTile(3246, 3156, 0),
                      WorldTile(3247, 3153, 0),
                      WorldTile(3243, 3150, 0),
                      WorldTile(3241, 3148, 0),
                      WorldTile(3240, 3146, 0),
                      WorldTile(3235, 3147, 0),
                      WorldTile(3233, 3151, 0),
                      WorldTile(3240, 3157, 0),
                      WorldTile(3243, 3160, 0))),
              WorldTile(3215, 3183),
              listOf(axe, tinderbox, fishingnet),
              "None",
              "Your best weapon and armour. Food may be required but they drop raw rat meat. Small fishing net, tinderbox, and an axe may be optional to fish shrimps south-east of Lumbridge Swamp."),
          MonsterArea(
              "Lumbridge Swamp",
              20,
              30,
              listOf(Monster.GIANT_FROG),
              Area.fromPolygon(
                  WorldTile(3155, 3207),
                  WorldTile(3146, 3190),
                  WorldTile(3154, 3176),
                  WorldTile(3157, 3163),
                  WorldTile(3169, 3152),
                  WorldTile(3204, 3147),
                  WorldTile(3221, 3152),
                  WorldTile(3231, 3152),
                  WorldTile(3234, 3159),
                  WorldTile(3240, 3171),
                  WorldTile(3243, 3183),
                  WorldTile(3243, 3188),
                  WorldTile(3235, 3190),
                  WorldTile(3228, 3194),
                  WorldTile(3223, 3198),
                  WorldTile(3206, 3197),
                  WorldTile(3193, 3196),
                  WorldTile(3185, 3205),
                  WorldTile(3178, 3206)),
              Area.fromPolygon(
                  listOf(
                      WorldTile(3246, 3159, 0),
                      WorldTile(3246, 3156, 0),
                      WorldTile(3247, 3153, 0),
                      WorldTile(3243, 3150, 0),
                      WorldTile(3241, 3148, 0),
                      WorldTile(3240, 3146, 0),
                      WorldTile(3235, 3147, 0),
                      WorldTile(3233, 3151, 0),
                      WorldTile(3240, 3157, 0),
                      WorldTile(3243, 3160, 0))),
              WorldTile(3215, 3183),
              listOf(axe, tinderbox, fishingnet),
              "None",
              "Bring your best armour (chainbody recommended) and weapons."),
          MonsterArea(
              "Hill Giant",
              30,
              45,
              listOf(Monster.HILL_GIANT),
              Area.fromPolygon(
                  WorldTile(3116, 9854, 0),
                  WorldTile(3119, 9853, 0),
                  WorldTile(3124, 9849, 0),
                  WorldTile(3126, 9844, 0),
                  WorldTile(3125, 9836, 0),
                  WorldTile(3121, 9830, 0),
                  WorldTile(3117, 9827, 0),
                  WorldTile(3113, 9827, 0),
                  WorldTile(3111, 9823, 0),
                  WorldTile(3107, 9822, 0),
                  WorldTile(3099, 9825, 0),
                  WorldTile(3094, 9830, 0),
                  WorldTile(3094, 9836, 0),
                  WorldTile(3100, 9840, 0),
                  WorldTile(3105, 9840, 0),
                  WorldTile(3106, 9844, 0),
                  WorldTile(3108, 9848, 0),
                  WorldTile(3109, 9850, 0),
                  WorldTile(3112, 9851, 0)),
              Area.fromPolygon(
                  WorldTile(3107, 3436, 0),
                  WorldTile(3109, 3436, 0),
                  WorldTile(3112, 3434, 0),
                  WorldTile(3109, 3430, 0),
                  WorldTile(3106, 3426, 0),
                  WorldTile(3105, 3423, 0),
                  WorldTile(3100, 3430, 0),
                  WorldTile(3101, 3433, 0),
                  WorldTile(3105, 3435, 0)),
              WorldTile(3103, 3434),
              listOf(axe, tinderbox, flyfishingrod, feathers),
              "None",
              "Your best weapon and armour."),
          MonsterArea(
              "Moss Giant",
              42,
              60,
              listOf(Monster.MOSS_GIANT),
              Area.fromPolygon(
                  WorldTile(3150, 9860, 0),
                  WorldTile(3160, 9860, 0),
                  WorldTile(3160, 9850, 0),
                  WorldTile(3150, 9850, 0)),
              Area.fromPolygon(
                  WorldTile(3150, 3400, 0),
                  WorldTile(3160, 3400, 0),
                  WorldTile(3160, 3390, 0),
                  WorldTile(3150, 3390, 0)),
              WorldTile(3155, 3395),
              listOf(axe, tinderbox, flyfishingrod, feathers),
              "Varrock Sewers, Crandor",
              "Food, armour (chainbody recommended), staff of fire and nature runes for High Level Alchemy, runes for Varrock Teleport"))

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
