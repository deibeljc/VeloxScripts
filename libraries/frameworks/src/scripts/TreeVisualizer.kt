package scripts.behaviortree

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D

class TreeVisualizer(private val root: IBehaviorNode) {

  private val nodeHeight = 30
  private val nodeWidth = 100
  private val verticalSpacing = 50
  private val horizontalSpacing = 20

  fun render(g: Graphics2D, x: Int, y: Int) {
    g.font = Font("Arial", Font.PLAIN, 12)
    renderNode(g, root, x, y, 0)
  }

  private fun renderNode(g: Graphics2D, node: IBehaviorNode, x: Int, y: Int, depth: Int) {
    val status = node.status
    val color =
        when (status) {
          BehaviorTreeStatus.SUCCESS -> Color.GREEN
          BehaviorTreeStatus.FAILURE -> Color.RED
          BehaviorTreeStatus.RUNNING -> Color.YELLOW
          BehaviorTreeStatus.KILL -> Color.BLACK
        }

    g.color = color
    g.drawRect(x, y, nodeWidth, nodeHeight)
    g.drawString(node.javaClass.simpleName, x + 5, y + 20)

    var childX = x + nodeWidth + horizontalSpacing
    val childY = y + nodeHeight + verticalSpacing
    for (child in node.children) {
      renderNode(g, child, childX, childY, depth + 1)
      childX += nodeWidth + horizontalSpacing
    }
  }
}
