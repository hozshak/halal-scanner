package com.halal.scanner.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * Horizontale Score-Bar mit 5-Farb-Gradient (rot→orange→gelb→hellgrün→grün)
 * + weißem Indikator-Kreis an der `position` (0f..1f).
 *
 * Verwendet für Eco-Score (0..100) und Nova-Score (1..4, gemappt auf 0.1..0.9).
 */
class ScoreBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    /** Position des Indikators von 0f (links/schlecht) bis 1f (rechts/gut). */
    var position: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dotFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    private val dotRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0B0B0F.toInt()
        style = Paint.Style.STROKE
        strokeWidth = density(2f)
    }

    private val barHeight   get() = density(10f)
    private val dotRadius   get() = density(11f)
    private val dotPadding  get() = density(2f)

    private val colors = intArrayOf(
        0xFFE54B4B.toInt(),
        0xFFE6A23C.toInt(),
        0xFFE6C133.toInt(),
        0xFF8FCB6F.toInt(),
        0xFF2EB872.toInt(),
    )
    private val stops = floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f)

    private var shader: Shader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shader = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            colors, stops, Shader.TileMode.CLAMP,
        )
        barPaint.shader = shader
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Mindesthöhe = Indikator-Durchmesser, Breite = match_parent
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (dotRadius * 2 + dotPadding * 2).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cy = h / 2f

        // Bar
        val barTop = cy - barHeight / 2f
        val barBottom = cy + barHeight / 2f
        val rect = RectF(0f, barTop, w, barBottom)
        val corner = barHeight / 2f
        canvas.drawRoundRect(rect, corner, corner, barPaint)

        // Dot
        val cx = (max(dotRadius, min(w - dotRadius, position * w))).toFloat()
        canvas.drawCircle(cx, cy, dotRadius, dotRingPaint)
        canvas.drawCircle(cx, cy, dotRadius - dotPadding, dotFillPaint)
    }

    private fun density(dp: Float): Float = dp * resources.displayMetrics.density
}
