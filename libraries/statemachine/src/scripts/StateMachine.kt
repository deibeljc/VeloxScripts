package scripts.statemachine

import org.tribot.script.sdk.Log

// State and Transition Classes
class State(
    val name: String,
    val action: () -> Unit = {},
    val nestedStateMachine: StateMachine? = null
) {
  val transitions = mutableListOf<Transition>()
}

data class Transition(val condition: () -> Boolean, val toState: State)

// StateMachine Class with stateChangeListener
class StateMachine(val states: List<State>, initialState: State? = null) {
  var currentState: State? = initialState
    private set

  var stateChangeListener: ((State?) -> Unit)? = null

  fun setInitialState(state: State) {
    currentState = state
    enterState(currentState)
  }

  fun enterInitialState() {
    if (currentState == null) {
      currentState = states.firstOrNull()
      enterState(currentState)
    }
  }

  fun step() {
    if (currentState == null) {
      // If currentState is not set, do nothing
      return
    }

    var transitioned: Boolean
    do {
      transitioned = false
      for (transition in currentState?.transitions ?: emptyList()) {
        if (transition.condition()) {
          Log.info("Transitioning to ${transition.toState.name}")
          currentState = transition.toState
          enterState(currentState)
          transitioned = true
          break
        }
      }
    } while (transitioned)

    // After all transitions, process the nested state machine and invoke the action
    currentState?.nestedStateMachine?.step()
    currentState?.action?.invoke()
  }

  private fun enterState(state: State?) {
    if (state == null) return
    // Initialize the nested state machine's current state
    state.nestedStateMachine?.let { nestedSM ->
      // Set the nested state machine's stateChangeListener
      nestedSM.stateChangeListener = { nestedState ->
        // Notify the parent listener (this state machine's listener)
        stateChangeListener?.invoke(nestedState)
      }
      nestedSM.enterInitialState()
    }

    // Notify the listener about the current state change
    stateChangeListener?.invoke(state)
  }
}

// StateBuilder Class
class StateBuilder {
  var action: () -> Unit = {}
  var nestedStateMachine: StateMachine? = null

  fun action(action: () -> Unit) {
    this.action = action
  }

  fun nestedStateMachine(block: StateMachineBuilder.() -> Unit) {
    val builder = StateMachineBuilder()
    builder.block()
    this.nestedStateMachine = builder.build()
  }
}

// Top-level State Function
fun createState(name: String, block: StateBuilder.() -> Unit): State {
  val builder = StateBuilder()
  builder.block()
  return State(name, builder.action, builder.nestedStateMachine)
}

// StateMachineBuilder Class with corrections
class StateMachineBuilder {
  private val states = mutableSetOf<State>()
  private val globalTransitions = mutableListOf<Pair<() -> Boolean, State>>()

  fun state(name: String, block: StateBuilder.() -> Unit): State {
    val state = createState(name, block)
    states.add(state)
    return state
  }

  infix fun State.on(condition: () -> Boolean): TransitionBuilder {
    states.add(this) // Ensure 'from' state is added
    return TransitionBuilder(this, condition)
  }

  fun any(block: AnyTransitionBuilder.() -> Unit) {
    AnyTransitionBuilder().apply(block)
  }

  inner class TransitionBuilder(
      private val fromState: State,
      private val condition: () -> Boolean
  ) {
    infix fun to(toState: State) {
      fromState.transitions.add(Transition(condition, toState))
      states.add(toState) // Ensure 'to' state is added
    }
  }

  inner class AnyTransitionBuilder {
    infix fun on(condition: () -> Boolean): AnyTransitionBuilder {
      return this.also { it.condition = condition }
    }

    infix fun to(toState: State) {
      globalTransitions.add(condition to toState)
      states.add(toState) // Ensure 'to' state is added
    }

    private lateinit var condition: () -> Boolean
  }

  fun build(initialState: State? = null): StateMachine {
    // Apply global transitions to all states, except self-transitions
    states.forEach { state ->
      globalTransitions.forEach { (condition, toState) ->
        if (state != toState) {
          state.transitions.add(Transition(condition, toState))
        }
      }
    }
    return StateMachine(states.toList(), initialState)
  }
}

// Top-level StateMachine Function
fun createStateMachine(
    initialState: State? = null,
    block: StateMachineBuilder.() -> Unit
): StateMachine {
  val builder = StateMachineBuilder()
  builder.block()
  return builder.build(initialState)
}
