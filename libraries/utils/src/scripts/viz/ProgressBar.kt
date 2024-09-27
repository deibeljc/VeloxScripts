package scripts.utils.viz

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform

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
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    val frc = g2d.fontRenderContext
    val f = Font("Helvetica", Font.BOLD, 12)
    val textTl = TextLayout(label, f, frc)
    val outline = textTl.getOutline(null)
    val outlineBounds = outline.bounds

    // Save the current transform
    val originalTransform = g2d.transform

    // Create a new transform for the text
    val textTransform = AffineTransform()
    textTransform.translate(
        x + width / 2 - (outlineBounds.width / 2).toDouble(),
        y + height / 2 + (outlineBounds.height / 2).toDouble())
    g2d.transform = textTransform

    // Set the outline stroke width
    val outlineStroke = BasicStroke(2f) // Adjust this value to change the outline thickness
    g2d.stroke = outlineStroke

    // Draw the outline (stroke)
    g2d.color = Color.BLACK
    g2d.draw(outline)

    // Fill the text
    g2d.color = Color.WHITE
    g2d.fill(outline)

    // Restore the original transform and stroke
    g2d.transform = originalTransform
    g2d.stroke = BasicStroke() // Reset to default stroke

    g2d.composite = originalComposite
  }
}
