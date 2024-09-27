package scripts.utils

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.Path2D
import kotlin.math.roundToInt

class LineChart(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int
) {
  private var data: List<Pair<Double, Double>> = emptyList()
  private var xMin: Double = 0.0
  private var xMax: Double = 0.0
  private var yMin: Double = 0.0
  private var yMax: Double = 0.0
  private val padding = 40
  private val axisColor = Color.WHITE
  private val labelFont = Font("Arial", Font.PLAIN, 12)

  fun setData(newData: List<Pair<Double, Double>>) {
    data = newData
    if (data.isNotEmpty()) {
      xMin = data.minOf { it.first }
      xMax = data.maxOf { it.first }
      yMin = 0.0 // Always set yMin to 0
      yMax = maxOf(data.maxOf { it.second }, 1.0) // Ensure yMax is at least 1.0
    }
  }

  fun render(g: Graphics2D) {
    if (data.isEmpty()) return

    // Draw chart border
    g.color = Color.LIGHT_GRAY
    g.drawRect(x, y, width, height)

    // Draw axes
    drawAxes(g)

    // Draw data line
    val path = Path2D.Double()
    data.forEachIndexed { index, (xValue, yValue) ->
      val xPos = getXPosition(xValue)
      val yPos = getYPosition(yValue)

      if (index == 0) {
        path.moveTo(xPos.toDouble(), yPos.toDouble())
      } else {
        path.lineTo(xPos.toDouble(), yPos.toDouble())
      }
    }

    g.color = Color.BLUE
    g.draw(path)
  }

  private fun drawAxes(g: Graphics2D) {
    g.color = axisColor
    g.font = labelFont

    // X-axis
    g.drawLine(x + padding, y + height - padding, x + width - padding, y + height - padding)
    // Y-axis
    g.drawLine(x + padding, y + padding, x + padding, y + height - padding)

    // X-axis labels
    val xStep = (xMax - xMin) / 5
    for (i in 0..5) {
      val xValue = xMin + i * xStep
      val xPos = getXPosition(xValue)
      g.drawString(String.format("%.1f", xValue), xPos - 15, y + height - padding + 20)
      g.drawLine(xPos, y + height - padding, xPos, y + height - padding + 5)
    }

    // Y-axis labels
    val yStep = (yMax - yMin) / 5
    for (i in 0..5) {
      val yValue = yMin + i * yStep
      val yPos = getYPosition(yValue)
      g.drawString(String.format("%.1f", yValue), x + padding - 35, yPos + 5)
      g.drawLine(x + padding - 5, yPos, x + padding, yPos)
    }
  }

  private fun getXPosition(xValue: Double): Int {
    return x + padding + ((xValue - xMin) / (xMax - xMin) * (width - 2 * padding)).roundToInt()
  }

  private fun getYPosition(yValue: Double): Int {
    return y + height -
        padding -
        ((yValue - yMin) / (yMax - yMin) * (height - 2 * padding)).roundToInt()
  }
}
