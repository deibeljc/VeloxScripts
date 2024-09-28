package scripts.frameworks

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import scripts.behaviortree.BehaviorTreeStatus
import scripts.behaviortree.IBehaviorNode

class StateTreeVisualizer(private val stateMachine: StateMachine) {
  private val nodeHeight = 30
  private val nodeWidth = 120
  private val verticalSpacing = 50
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

  private fun renderStateMachine(g: Graphics2D, sm: StateMachine, x: Int, y: Int, level: Int) {
    // Draw folder for state machine
    g.color = Color.LIGHT_GRAY
    g.fillRect(x, y, folderWidth, folderHeight)
    g.color = Color.BLACK
    g.drawRect(x, y, folderWidth, folderHeight)
    g.drawString(sm.javaClass.simpleName, x + 5, y + 25)

    var currentY = y + folderHeight + verticalSpacing

    // Draw states
    for (state in sm.states) {
      renderState(g, state, x + horizontalSpacing, currentY, level, sm.currentState == state)
      currentY += nodeHeight + verticalSpacing

      // Render behavior tree for the state
      renderBehaviorTree(g, state, x + horizontalSpacing * 2, currentY)
      currentY += nodeHeight + verticalSpacing
    }
  }

  private fun renderState(
      g: Graphics2D,
      state: State,
      x: Int,
      y: Int,
      level: Int,
      isCurrent: Boolean
  ) {
    g.color = if (isCurrent) Color.GREEN else Color.WHITE
    g.fillRect(x, y, nodeWidth, nodeHeight)
    g.color = Color.BLACK
    g.drawRect(x, y, nodeWidth, nodeHeight)
    g.drawString(state.name, x + 5, y + 20)

    // Draw nested state machine if present
    state.nestedStateMachine?.let { nestedSM ->
      renderStateMachine(g, nestedSM, x + nodeWidth + horizontalSpacing, y, level + 1)
    }
  }

  private fun renderBehaviorTree(g: Graphics2D, state: State, x: Int, y: Int) {
    // Assuming the state's action is a BehaviorNode
    val rootNode = (state.action as? () -> IBehaviorNode)?.invoke()
    if (rootNode != null) {
      renderBehaviorNode(g, rootNode, x, y, 0)
    }
  }

  private fun renderBehaviorNode(g: Graphics2D, node: IBehaviorNode, x: Int, y: Int, depth: Int) {
    val status = node.tick()
    val color =
        when (status) {
          BehaviorTreeStatus.SUCCESS -> Color.GREEN
          BehaviorTreeStatus.FAILURE -> Color.RED
          BehaviorTreeStatus.RUNNING -> Color.YELLOW
          BehaviorTreeStatus.KILL -> Color.BLACK
        }

    g.color = color
    g.drawRect(x - nodeWidth / 2, y, nodeWidth, nodeHeight)
    g.drawString(node.javaClass.simpleName, x - nodeWidth / 2 + 5, y + 20)

    if (node.children.isNotEmpty()) {
      val childrenWidth = node.children.size * (nodeWidth + horizontalSpacing)
      var childX = x - childrenWidth / 2 + nodeWidth / 2
      val childY = y + nodeHeight + verticalSpacing

      for (child in node.children) {
        renderBehaviorNode(g, child, childX, childY, depth + 1)
        g.drawLine(x, y + nodeHeight, childX, childY)
        childX += nodeWidth + horizontalSpacing
      }
    }
  }
}
