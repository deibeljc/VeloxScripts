package scripts

import kotlin.jvm.optionals.getOrNull
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.InventoryItem

class InventoryHelper {
  companion object {
    fun getRequiredItems(): List<List<String>> {
      return Locations.getBestTrainingArea().requiredItems
    }

    /** Has food in inventory */
    fun hasFood(): Boolean {
      val hasFood = Query.inventory().actionContains("Eat").count() > 0
      return hasFood
    }

    fun hasRawFood(): Boolean {
      return Query.inventory().nameContains("Raw").count() > 0
    }

    fun rawFoodCount(): Int {
      return Query.inventory().nameContains("Raw").count()
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
      return Query.inventory().filter { !isRequiredItem(it) }.count() > 0
    }

    /** Get all non food items that are also not raw food that is to be cooked */
    fun getNonFoodItems(): List<InventoryItem> {
      return Query.inventory()
          .filter { !isRequiredItem(it) }
          .filter { !it.name.contains("Raw") }
          .actionNotContains("Eat")
          .toList()
    }

    fun isRequiredItem(item: InventoryItem): Boolean {
      return getRequiredItems().any { sublist ->
        sublist.any { item.name.lowercase().contains(it) }
      }
    }

    fun hasRequiredItems(): Boolean {
      val hasRequiredItems =
          getRequiredItems().all { category ->
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
