package com.example.snowflakes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

data class Snowflake(
    var x: Float,
    var y: Float,
    var radius: Float,
    var baseSpeed: Float,
    var color: Int,
    var alpha: Int,
    var rotation: Float,
    var rotationSpeed: Float,
    var initialX: Float,
    var phase: Float,
    var amplitude: Float
)

class Snowflakes @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val snowflakesCount = 70
    private val snowflakes = mutableListOf<Snowflake>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private var animationTask: MoveTask? = null

    private fun getRandomSnowColor(): Int {
        val r = Random.nextInt(190, 256)
        val g = Random.nextInt(220, 256)
        val b = 255
        return Color.rgb(r, g, b)
    }

    private fun createSnowflake(w: Int, startRandomY: Boolean = false): Snowflake {
        val initialX = Random.nextFloat() * w
        val r = Random.nextFloat() * 14f + 10f
        return Snowflake(
            x = initialX,
            y = if (startRandomY) Random.nextFloat() * height else -Random.nextFloat() * 100f,
            radius = r,
            baseSpeed = Random.nextFloat() * 5f + 3f,
            color = getRandomSnowColor(),
            alpha = Random.nextInt(130, 225),
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = (Random.nextFloat() - 0.5f) * 4f,
            initialX = initialX,
            phase = Random.nextFloat() * 360f,
            amplitude = Random.nextFloat() * 40f + 20f
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        snowflakes.clear()
        for (i in 0 until snowflakesCount) {
            snowflakes.add(createSnowflake(w, startRandomY = true))
        }

        animationTask?.cancel(true)
        animationTask = MoveTask()
        animationTask?.execute()
    }

    fun updateSnowflakes() {
        if (height <= 0 || width <= 0) return

        for (s in snowflakes) {
            val decelerationFactor = 1.0f - (s.y / height).coerceIn(0f, 0.75f)
            s.y += s.baseSpeed * decelerationFactor

            val angle = Math.toRadians((s.y * 1.5 + s.phase).toDouble())
            s.x = s.initialX + s.amplitude * sin(angle).toFloat()

            s.rotation += s.rotationSpeed

            if (s.y > height + s.radius * 2) {
                val newSnowflake = createSnowflake(width, startRandomY = false)
                s.x = newSnowflake.x
                s.y = -s.radius * 2
                s.initialX = newSnowflake.initialX
                s.color = newSnowflake.color
                s.alpha = newSnowflake.alpha
                s.baseSpeed = newSnowflake.baseSpeed
                s.radius = newSnowflake.radius
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(10, 15, 30))

        for (s in snowflakes) {
            paint.color = s.color
            paint.alpha = s.alpha
            paint.strokeWidth = (s.radius * 0.12f).coerceAtLeast(2f)

            canvas.save()
            canvas.translate(s.x, s.y)
            canvas.rotate(s.rotation)

            for (i in 0 until 6) {
                canvas.drawLine(0f, 0f, 0f, s.radius, paint)
                val branchY = s.radius * 0.6f
                val branchSize = s.radius * 0.3f
                canvas.drawLine(0f, branchY, -branchSize, branchY + branchSize * 0.6f, paint)
                canvas.drawLine(0f, branchY, branchSize, branchY + branchSize * 0.6f, paint)
                canvas.rotate(60f)
            }

            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationTask?.cancel(true)
    }

    @Suppress("DEPRECATION")
    private inner class MoveTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            while (!isCancelled) {
                updateSnowflakes()
                publishProgress()
                try {
                    Thread.sleep(16)
                } catch (e: InterruptedException) {
                    break
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
            invalidate()
        }
    }
}