package scripts.frameworks

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.KeyEvent
import org.tribot.script.sdk.ScriptListening
import org.tribot.script.sdk.interfaces.EventOverride
import org.tribot.script.sdk.interfaces.KeyEventOverrideListener
import scripts.behaviortree.BehaviorTreeStatus
import scripts.behaviortree.IBehaviorNode

class StateTreeVisualizer(private val stateMachine: StateMachine) {
  private val nodeHeight = 30
  private val nodeWidth = 120
  private val verticalSpacing = 20
  private val horizontalSpacing = 20
  private val folderWidth = 80
  private val folderHeight = 40

  private var scrollOffset = 0
  private val scrollBarWidth = 20
  private val scrollBarMinHeight = 50

  fun render(g: Graphics2D, width: Int, height: Int) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.font = Font("Arial", Font.PLAIN, 12)

    val rootX = 300
    val rootY = 50 - scrollOffset

    // Create a clipping region for the main content
    g.clipRect(0, 0, width - scrollBarWidth, height)

    val totalHeight = renderStateMachine(g, stateMachine, rootX, rootY, 0)

    // Reset the clip
    g.clip = null

    // Render scrollbar
    renderScrollBar(g, width, height, totalHeight)
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
      currentY += stateHeight + verticalSpacing / 4 // Reduced spacing after state

      // Calculate and render behavior tree
      val treeHeight =
          folderHeight + verticalSpacing * 4 + calculateBehaviorTreeHeight(state.tree?.root())
      if (treeHeight > 0) {
        renderBehaviorTree(g, state, x + horizontalSpacing * 2, currentY)
        currentY += treeHeight + verticalSpacing / 4 // Reduced spacing after behavior tree
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
    return verticalSpacing + childrenHeight
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
    val folderHeight = 20
    val indentation = 20
    val verticalSpacing = 2

    val status = node.status
    val color =
        when (status) {
          BehaviorTreeStatus.SUCCESS -> Color.GREEN
          BehaviorTreeStatus.FAILURE -> Color.RED
          BehaviorTreeStatus.RUNNING -> Color.YELLOW
          BehaviorTreeStatus.IDLE -> Color.BLACK
          else -> Color.BLACK
        }

    val nodeName = "[${node.javaClass.simpleName}] ${node.label ?: ""}"
    val metrics = g.fontMetrics
    val folderWidth = metrics.stringWidth(nodeName) + 10 // Add some padding

    // Draw folder
    g.color = Color.LIGHT_GRAY
    g.fillRect(x + depth * indentation, y, folderWidth, folderHeight)
    g.color = color
    g.drawRect(x + depth * indentation, y, folderWidth, folderHeight)
    g.color = Color.BLACK
    g.drawString(nodeName, x + depth * indentation + 5, y + 15)

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

  private fun renderScrollBar(g: Graphics2D, width: Int, height: Int, totalHeight: Int) {
    val viewportRatio = height.toFloat() / totalHeight
    val scrollBarHeight =
        (height * viewportRatio).coerceAtLeast(scrollBarMinHeight.toFloat()).toInt()
    val scrollBarY = (scrollOffset.toFloat() / totalHeight * height).toInt()

    // Draw scrollbar background
    g.color = Color.LIGHT_GRAY
    g.fillRect(width - scrollBarWidth, 0, scrollBarWidth, height)

    // Draw scrollbar thumb
    g.color = Color.GRAY
    g.fillRect(width - scrollBarWidth, scrollBarY, scrollBarWidth, scrollBarHeight)
  }

  fun setupScrollListener() {
    ScriptListening.addKeyEventOverrideListener(
        KeyEventOverrideListener { event ->
          if (event.id == KeyEvent.KEY_PRESSED) {
            when (event.keyCode) {
              KeyEvent.VK_UP -> scrollOffset -= 100
              KeyEvent.VK_DOWN -> scrollOffset += 100
            }
          }
          return@KeyEventOverrideListener EventOverride.DISMISS
        })
  }

  fun handleScroll(scrollAmount: Int, totalHeight: Int, height: Int) {
    scrollOffset = (scrollOffset + scrollAmount).coerceIn(0, totalHeight - height)
  }
}
