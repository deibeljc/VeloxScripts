package scripts

import java.awt.Graphics

class RenderStack(
    private val rootX: Int,
    private val rootY: Int,
    private val direction: Direction,
    private val maxStackSize: Int = 15,
    private val animationDuration: Long = 100L, // milliseconds
    private val spacing: Int = 5 // Space between items in pixels
) {
  enum class Direction {
    HORIZONTAL,
    VERTICAL
  }

  data class RenderItem(
      var renderFunction: (Graphics, Float, Float, Float) -> Unit,
      var width: Int,
      var height: Int,
      var position: Float,
      var alpha: Float = 0f,
      var targetPosition: Float = 0f
  )

  private val renderStack = mutableMapOf<String, RenderItem>()
  private val renderOrder = mutableListOf<String>()
  private val animationSpeed = 1f / animationDuration

  fun animateRenderStack(frameTime: Long) {
    var currentPosition = 0f

    renderOrder.forEach { key ->
      val item = renderStack[key] ?: return@forEach
      item.targetPosition = currentPosition
      val targetAlpha = 1f

      item.position += (item.targetPosition - item.position) * animationSpeed * frameTime
      item.alpha += (targetAlpha - item.alpha) * animationSpeed * frameTime

      currentPosition += if (direction == Direction.VERTICAL) item.height else item.width + spacing
    }
  }

  fun renderStack(g: Graphics) {
    renderOrder.forEach { key ->
      val item = renderStack[key] ?: return@forEach
      val x = if (direction == Direction.HORIZONTAL) rootX + item.position else rootX.toFloat()
      val y = if (direction == Direction.VERTICAL) rootY + item.position else rootY.toFloat()
      item.renderFunction(g, x, y, item.alpha)
    }
  }

  fun pushToRenderStack(
      key: String,
      renderFunction: (Graphics, Float, Float, Float) -> Unit,
      width: Int,
      height: Int
  ) {
    if (key in renderStack) {
      // Update existing item
      renderStack[key]?.apply {
        this.renderFunction = renderFunction
        this.width = width
        this.height = height
      }
    } else {
      // Add new item
      renderStack[key] = RenderItem(renderFunction, width, height, 0f, targetPosition = 0f)
      renderOrder.add(0, key)
      if (renderOrder.size > maxStackSize) {
        val removedKey = renderOrder.removeAt(renderOrder.size - 1)
        renderStack.remove(removedKey)
      }
    }
  }

  fun removeFromRenderStack(key: String) {
    renderStack.remove(key)
    renderOrder.remove(key)
  }
}
