package scripts.frameworks

import java.awt.*
import kotlin.math.cos
import kotlin.math.sin

class StateMachineVisualizer(val stateMachine: StateMachine) {

  private val statePositions = mutableMapOf<State, Point>()
  private val stateRadius = 30
  private var centerX = 0
  private var centerY = 0

  fun render(g2d: Graphics2D, width: Int, height: Int) {
    // Anti-aliasing for smoother graphics
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    // Clear the statePositions map before each draw
    statePositions.clear()

    // Center coordinates
    centerX = width / 2
    centerY = height / 2

    // Start drawing from the root state machine
    drawStateMachine(g2d, stateMachine, centerX, centerY, 0)
  }

  private fun drawStateMachine(g2d: Graphics2D, sm: StateMachine, x: Int, y: Int, level: Int) {
    val totalStates = sm.states.size
    val angleIncrement = 2 * Math.PI / totalStates
    val radius = 150 - (level * 20) // Reduce radius for nested states
    var angle = 0.0 + (level * Math.PI / 4)

    // Calculate positions for each state
    val positions = mutableMapOf<State, Point>()
    for (state in sm.states) {
      val dx = (radius * cos(angle)).toInt()
      val dy = (radius * sin(angle)).toInt()
      val statePos = Point(x + dx, y + dy)
      positions[state] = statePos
      statePositions[state] = statePos
      angle += angleIncrement
    }

    // Draw transitions first
    for (state in sm.states) {
      val fromPos = positions[state] ?: continue
      for (transition in state.transitions) {
        val toState = transition.toState
        val toPos = positions[toState] ?: statePositions[toState] ?: continue

        // Draw line from fromPos to toPos
        g2d.color = Color.BLACK
        g2d.drawLine(fromPos.x, fromPos.y, toPos.x, toPos.y)
      }
    }

    // Draw states
    for (state in sm.states) {
      val statePos = positions[state] ?: continue
      drawState(g2d, state, statePos.x, statePos.y, level + 1)
    }
  }

  private fun drawState(g2d: Graphics2D, state: State, x: Int, y: Int, level: Int) {
    val currentState = isCurrentState(state)

    // Highlight the current state
    if (currentState) {
      g2d.color = Color.GREEN
    } else {
      g2d.color = Color.LIGHT_GRAY
    }

    // Draw the state circle
    val adjustedRadius = stateRadius - (level * 5)
    g2d.fillOval(x - adjustedRadius, y - adjustedRadius, adjustedRadius * 2, adjustedRadius * 2)

    // Draw the state name
    g2d.color = Color.BLACK
    val fm = g2d.fontMetrics
    val textWidth = fm.stringWidth(state.name)
    if (currentState) {
      val textX = x - textWidth / 2
      val textY = y + fm.ascent / 2 - 2
      g2d.drawString(state.name, textX, textY)
    }

    // If the state has a nested state machine, draw it
    state.nestedStateMachine?.let { nestedSM ->
      // Draw a circle around the nested state machine
      g2d.color = Color.GRAY
      val nestedRadius = adjustedRadius + 36
      g2d.drawOval(x - nestedRadius, y - nestedRadius, nestedRadius * 2, nestedRadius * 2)

      // Draw the nested state machine
      drawStateMachine(g2d, nestedSM, x, y, level + 1)
    }
  }

  private fun isCurrentState(state: State): Boolean {
    return checkCurrentState(stateMachine, state)
  }

  private fun checkCurrentState(sm: StateMachine?, state: State): Boolean {
    if (sm == null) return false
    if (sm.currentState == state) return true
    // Only check the nested state machine of the current state
    return checkCurrentState(sm.currentState?.nestedStateMachine, state)
  }
}
