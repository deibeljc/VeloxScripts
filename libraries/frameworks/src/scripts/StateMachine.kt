package scripts.frameworks

import org.tribot.script.sdk.Log

// State and Transition Classes
class State(
  val name: String,
  val action: () -> Unit = {},
  val tree: BehaviorTree? = null,
  val nestedStateMachine: StateMachine? = null
) {
  val transitions = mutableListOf<Transition>()
}

data class Transition(val condition: () -> Boolean, val toState: State, val priority: Int = 0)

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
      val sortedTransitions = (currentState?.transitions ?: emptyList()).sortedBy { it.priority }
      for (transition in sortedTransitions) {
        if (transition.condition()) {
          Log.info("Transitioning to ${transition.toState.name} (priority: ${transition.priority})")
          // Reset the existing tree before transitioning to the new state
          currentState?.tree?.root()?.reset()
          // Transition to the new state
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
    // Step the behavior tree
    currentState?.tree?.tick()
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
  var tree: () -> BehaviorTree? = { null }
  var nestedStateMachine: StateMachine? = null

  fun action(action: () -> Unit) {
    this.action = action
  }

  fun nestedStateMachine(block: StateMachineBuilder.() -> Unit) {
    val builder = StateMachineBuilder()
    builder.block()
    this.nestedStateMachine = builder.build()
  }

  fun tree(block: () -> BehaviorTree) {
    this.tree = block
  }
}

// Top-level State Function
fun createState(name: String, block: StateBuilder.() -> Unit): State {
  val builder = StateBuilder()
  builder.block()
  return State(name, builder.action, builder.tree(), builder.nestedStateMachine)
}

class StateMachineBuilder {
  private val states = mutableSetOf<State>()
  private val globalTransitions = mutableListOf<Transition>()

  fun state(name: String, block: StateBuilder.() -> Unit): State {
    val state = createState(name, block)
    states.add(state)
    return state
  }

  infix fun State.on(condition: () -> Boolean): TransitionBuilder {
    states.add(this)
    return TransitionBuilder(this, condition, 0)
  }

  fun State.withPriority(priority: Int): StateTransitionInitializer {
    states.add(this)
    return StateTransitionInitializer(this, priority)
  }

  fun any(block: AnyTransitionBuilder.() -> Unit) {
    AnyTransitionBuilder().apply(block)
  }

  inner class StateTransitionInitializer(private val state: State, private val priority: Int) {
    infix fun on(condition: () -> Boolean): TransitionBuilder {
      return TransitionBuilder(state, condition, priority)
    }
  }

  inner class TransitionBuilder(
    private val fromState: State,
    private val condition: () -> Boolean,
    private val priority: Int
  ) {
    infix fun to(toState: State) {
      fromState.transitions.add(Transition(condition, toState, priority))
      states.add(toState)
    }
  }

  inner class AnyTransitionBuilder {
    infix fun on(condition: () -> Boolean): AnyTransitionBuilder {
      this.condition = condition
      this.priority = 0
      return this
    }

    fun withPriority(priority: Int): AnyTransitionBuilder {
      this.priority = priority
      return this
    }

    infix fun to(toState: State) {
      globalTransitions.add(Transition(condition, toState, priority))
      states.add(toState)
    }

    private lateinit var condition: () -> Boolean
    private var priority: Int = 0
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
