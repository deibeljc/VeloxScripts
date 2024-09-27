package scripts.utils.viz

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

class ProgressBar(
    private val width: Int,
    private val height: Int,
    private var progress: Double = 0.0,
    private var label: String = "",
    private var backgroundColor: Color = Color.LIGHT_GRAY,
    private var foregroundColor: Color = Color.GREEN
) {
  fun setProgress(value: Double) {
    progress = value.coerceIn(0.0, 1.0)
  }

  fun setLabel(text: String) {
    label = text
  }

  fun setColors(background: Color, foreground: Color) {
    backgroundColor = background
    foregroundColor = foreground
  }

  fun draw(g: Graphics, x: Float, y: Float, alpha: Float = 1.0f) {
    val g2d = g as Graphics2D
    val originalComposite = g2d.composite
    g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

    // Draw background
    g.color = backgroundColor
    g.fillRect(x.toInt(), y.toInt(), width, height)

    // Draw progress
    g.color = foregroundColor
    val progressWidth = (width * progress).toInt()
    g.fillRect(x.toInt(), y.toInt(), progressWidth, height)

    // Draw border
    g.color = Color.BLACK
    g.drawRect(x.toInt(), y.toInt(), width, height)

    // Set up for text drawing
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    val fontMetrics: FontMetrics = g.fontMetrics
    val labelBounds: Rectangle2D = fontMetrics.getStringBounds(label, g)
    val labelX = x + (width - labelBounds.width.toInt()) / 2
    val labelY = y + (height - fontMetrics.descent + fontMetrics.ascent) / 2

    // Create TextLayout and get the outline
    val textLayout = TextLayout(label, g2d.font, g2d.fontRenderContext)
    val outline =
        textLayout.getOutline(
            AffineTransform.getTranslateInstance(labelX.toDouble(), labelY.toDouble()))

    // Draw the outline (stroke)
    g2d.color = Color.BLACK
    g2d.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    g2d.draw(outline)

    // Fill the text
    g2d.color = Color.WHITE
    g2d.fill(outline)

    g2d.composite = originalComposite
  }
}
