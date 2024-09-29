package scripts

import org.tribot.script.sdk.Log
import scripts.frameworks.IParentNode
import scripts.frameworks.perform

data class StateHistory(val state: String, val timestamp: Long, var duration: Long = 0L)

class VeloxState {
  companion object {
    private var scriptState: String = "Idle"
    private var lastStateChangeTime: Long = System.currentTimeMillis()
    private val stateHistories: MutableList<StateHistory> =
      mutableListOf(StateHistory(scriptState, lastStateChangeTime))

    /**
     * Sets the current state and records the transition.
     *
     * @param state The new state to set.
     * @return Boolean indicating if the state was successfully set.
     */
    fun setState(state: String): Boolean {
      // If our new state string is the same as our current state string, do nothing
      if (state == scriptState) {
        return false
      }

      val currentTime = System.currentTimeMillis()
      val duration = currentTime - lastStateChangeTime

      // Update the duration of the last state
      if (stateHistories.isNotEmpty()) {
        stateHistories.last().duration = duration
      }

      // Log the state change
      Log.info("Set state from $scriptState to $state")

      // Update the current state and timestamp
      scriptState = state
      lastStateChangeTime = currentTime

      // Add the new state to history
      stateHistories.add(StateHistory(state, currentTime))

      // Ensure history does not exceed 5 entries
      if (stateHistories.size > 5) {
        stateHistories.removeAt(0)
      }

      return true
    }

    /**
     * Retrieves the current state.
     *
     * @return The current state as a String.
     */
    fun getState(): String {
      return scriptState
    }

    /**
     * Retrieves the state history.
     *
     * @return A list of StateHistory objects.
     */
    fun getStateHistories(): String {
      val currentTime = System.currentTimeMillis()
      val currentStateDuration = currentTime - lastStateChangeTime

      return stateHistories
        .mapIndexed { index, stateHistory ->
          val duration =
            if (index == stateHistories.lastIndex) currentStateDuration
            else stateHistory.duration
          val minutes = (duration / 1000) / 60
          val seconds = (duration / 1000) % 60
          "${stateHistory.state} [${minutes}m ${seconds}s]"
        }
        .joinToString(" -> ")
    }
  }
}

fun IParentNode.updateState(state: String) = perform { VeloxState.setState(state) }
