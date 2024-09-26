package scripts

import kotlin.jvm.optionals.getOrNull
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.InventoryItem

/** We need to have one of the items in each top list item to be able to run the script */
val requiredItemsToHave =
    listOf(
        listOf("rune axe", "adamant axe", "mithril axe", "steel axe", "iron axe", "bronze axe"),
        listOf("tinderbox"),
        listOf("small fishing net", "big fishing net"))

class InventoryHelper {
  companion object {
    /** Has food in inventory */
    fun hasFood(): Boolean {
      val hasFood = Query.inventory().actionContains("Eat").count() > 0
      return hasFood
    }

    fun hasRawFood(): Boolean {
      return Query.inventory().nameContains("Raw").count() > 0
    }

    /** Has burnt food in inventory */
    fun hasBurntFood(): Boolean {
      return Query.inventory().nameContains("Burnt").count() > 0
    }

    fun hasLog(): Boolean {
      return Query.inventory().nameContains("Log").findFirst().isPresent
    }

    /** Eat whatever food is eatable */
    fun eatFood(): Boolean {
      val food = Query.inventory().actionContains("Eat").findFirst().getOrNull()
      return food?.click() ?: false
    }

    fun hasNonFood(): Boolean {
      return Query.inventory()
          .filter { item ->
            requiredItemsToHave.any { sublist ->
              sublist.any { item.name.lowercase().contains(it) }
            }
          }
          .count() > 0
    }

    fun getNonFoodItems(): List<InventoryItem> {
      return Query.inventory()
          .filter { item ->
            requiredItemsToHave.any { sublist ->
              sublist.any { item.name.lowercase().contains(it) }
            }
          }
          .actionNotContains("Eat")
          .toList()
    }

    fun hasRequiredItems(): Boolean {
      val hasRequiredItems =
          requiredItemsToHave.all { category ->
            val categoryResult =
                category.any { requiredItem ->
                  Query.inventory().nameContains(requiredItem).toList().isNotEmpty()
                }
            categoryResult
          }
      return hasRequiredItems
    }
  }
}
