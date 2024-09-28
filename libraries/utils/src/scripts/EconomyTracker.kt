package scripts

import java.time.Duration
import java.time.Instant
import org.tribot.script.sdk.pricing.Pricing

class EconomyTracker {
  private val lootedItems = mutableMapOf<Int, Int>()
  private var startTime: Instant = Instant.now()
  private var lastUpdateTime: Instant = Instant.now()

  fun addLootedItem(itemId: Int, quantity: Int) {
    lootedItems[itemId] = lootedItems.getOrDefault(itemId, 0) + quantity
    updateLastUpdateTime()
  }

  fun getTotalEarnings(): Int {
    return (lootedItems.entries).sumOf { (itemId, quantity) ->
      Pricing.lookupPrice(itemId).orElse(0) * quantity
    }
  }

  fun getGpPerHour(): Int {
    val durationInHours = Duration.between(startTime, Instant.now()).seconds / 3600.0
    return if (durationInHours > 0) (getTotalEarnings() / durationInHours).toInt() else 0
  }

  fun getLootedItemsValue(): Int {
    return lootedItems.entries.sumOf { (itemId, quantity) ->
      Pricing.lookupPrice(itemId).orElse(0) * quantity
    }
  }

  fun reset() {
    lootedItems.clear()
    startTime = Instant.now()
    lastUpdateTime = Instant.now()
  }

  private fun updateLastUpdateTime() {
    lastUpdateTime = Instant.now()
  }

  fun getTimeSinceLastUpdate(): Duration {
    return Duration.between(lastUpdateTime, Instant.now())
  }

  companion object {
    private var instance: EconomyTracker? = null

    fun getInstance(): EconomyTracker {
      if (instance == null) {
        instance = EconomyTracker()
      }
      return instance!!
    }
  }
}
