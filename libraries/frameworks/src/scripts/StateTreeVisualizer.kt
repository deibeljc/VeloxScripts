package scripts.frameworks

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import org.tribot.script.sdk.Log
import scripts.behaviortree.BehaviorTreeStatus
import scripts.behaviortree.IBehaviorNode

class StateTreeVisualizer(private val stateMachine: StateMachine) {
  private val nodeHeight = 30
  private val nodeWidth = 120
  private val verticalSpacing = 20 // Reduced from 50
  private val horizontalSpacing = 20
  private val folderWidth = 80
  private val folderHeight = 40

  fun render(g: Graphics2D, width: Int, height: Int) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.font = Font("Arial", Font.PLAIN, 12)

    val rootX = 50
    val rootY = 50

    renderStateMachine(g, stateMachine, rootX, rootY, 0)
  }

  private fun renderStateMachine(g: Graphics2D, sm: StateMachine, x: Int, y: Int, level: Int): Int {
    // Draw folder for state machine
    g.color = Color.LIGHT_GRAY
    g.fillRect(x, y, folderWidth, folderHeight)
    g.color = Color.BLACK
    g.drawRect(x, y, folderWidth, folderHeight)
    g.drawString(sm.javaClass.simpleName, x + 5, y + 25)

    var currentY = y + folderHeight + verticalSpacing
    var maxWidth = folderWidth

    // Draw states and their behavior trees
    for (state in sm.states) {
      val stateHeight =
          renderState(g, state, x + horizontalSpacing, currentY, level, sm.currentState == state)
      currentY += stateHeight + verticalSpacing / 2 // Reduced spacing after state

      // Calculate and render behavior tree
      val treeHeight = calculateBehaviorTreeHeight(state.tree?.root())
      Log.info("State ${state.name} Tree Height: $treeHeight State Height: $stateHeight")
      if (treeHeight > 0) {
        renderBehaviorTree(g, state, x + horizontalSpacing * 2, currentY)
        currentY += treeHeight + verticalSpacing / 2 // Reduced spacing after behavior tree
      }

      maxWidth = maxOf(maxWidth, nodeWidth + horizontalSpacing)
    }

    return currentY - y // Return total height of the state machine visualization
  }

  private fun renderState(
      g: Graphics2D,
      state: State,
      x: Int,
      y: Int,
      level: Int,
      isCurrent: Boolean
  ): Int {
    g.color = if (isCurrent) Color.GREEN else Color.WHITE
    g.fillRect(x, y, nodeWidth, nodeHeight)
    g.color = Color.BLACK
    g.drawRect(x, y, nodeWidth, nodeHeight)
    g.drawString(state.name, x + 5, y + 20)

    var totalHeight = nodeHeight

    // Draw nested state machine if present
    state.nestedStateMachine?.let { nestedSM ->
      val nestedHeight =
          renderStateMachine(g, nestedSM, x + nodeWidth + horizontalSpacing, y, level + 1)
      totalHeight = maxOf(totalHeight, nestedHeight)
    }

    return totalHeight
  }

  private fun calculateBehaviorTreeHeight(node: IBehaviorNode?): Int {
    if (node == null) return 0
    val childrenHeight = node.children.sumOf { calculateBehaviorTreeHeight(it) }
    val nodeHeight = folderHeight + verticalSpacing
    return nodeHeight + (if (node.children.isNotEmpty()) childrenHeight else 0)
  }

  private fun renderBehaviorTree(g: Graphics2D, state: State, x: Int, y: Int): Int {
    val rootNode = state.tree?.root()
    return if (rootNode != null) {
      renderBehaviorNode(g, rootNode, x, y, 0)
    } else {
      0
    }
  }

  private fun renderBehaviorNode(
      g: Graphics2D,
      node: IBehaviorNode,
      x: Int,
      y: Int,
      depth: Int
  ): Int {
    val folderWidth = 120
    val folderHeight = 20
    val indentation = 20
    val verticalSpacing = 5

    val status = node.status
    val color =
        when (status) {
          BehaviorTreeStatus.SUCCESS -> Color.GREEN
          BehaviorTreeStatus.FAILURE -> Color.RED
          BehaviorTreeStatus.RUNNING -> Color.YELLOW
          BehaviorTreeStatus.KILL -> Color.BLACK
          else -> Color.GRAY
        }

    // Draw folder
    g.color = Color.LIGHT_GRAY
    g.fillRect(x + depth * indentation, y, folderWidth, folderHeight)
    g.color = color
    g.drawRect(x + depth * indentation, y, folderWidth, folderHeight)
    g.color = Color.BLACK
    g.drawString(node.label ?: node.javaClass.simpleName, x + depth * indentation + 5, y + 15)

    // Draw children
    var childY = y + folderHeight + verticalSpacing
    var totalHeight = folderHeight + verticalSpacing
    for (child in node.children) {
      val childHeight = renderBehaviorNode(g, child, x, childY, depth + 1)
      childY += childHeight
      totalHeight += childHeight
    }

    return totalHeight
  }
}
