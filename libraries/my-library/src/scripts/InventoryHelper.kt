package scripts

import kotlin.jvm.optionals.getOrNull
import org.tribot.script.sdk.query.Query

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
  }
}
