package com.darkforge.bloodsouls.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.random.Random

class BloodBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Ember(
        var x: Float,
        var y: Float,
        var speed: Float,
        var radius: Float,
        var alpha: Int,
        var color: Int
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val embers = mutableListOf<Ember>()

    private var lastTime = System.currentTimeMillis()
    private var emberTimer = 0f

    private val bloodRed = Color.rgb(
        139, 0, 0
    )

    private val darkRed = Color.rgb(
        74, 0, 0
    )

    init {
        setLayerType(
            View.LAYER_TYPE_SOFTWARE,
            null
        )

        repeat(20) {
            createEmber(
                randomY = true
            )
        }
    }

    private fun createEmber(
        randomY: Boolean = false
    ) {

        val ember = Ember(
            x = Random.nextFloat() * width.coerceAtLeast(1),
            y = if (randomY)
                Random.nextFloat() * height.coerceAtLeast(1)
            else
                height.toFloat() + 10f,
            speed = 20f + Random.nextFloat() * 50f,
            radius = 2f + Random.nextFloat() * 3f,
            alpha = 80 + Random.nextInt(120),
            color = if (Random.nextBoolean())
                Color.rgb(255, 102, 0)
            else
                Color.rgb(204, 34, 0)
        )

        embers.add(ember)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        drawBackground(canvas, width, height)
        drawEmbers(canvas, width, height)
        drawRunicBorder(canvas, width, height)
        drawBloodDrips(canvas, width, height)
        drawVignette(canvas, width, height)

        invalidate()
    }

    private fun drawBackground(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {

        val centerX = 0f
        val centerY = 0f

        val radius = max(
            width,
            height
        ) * 1.2f

        val gradient = RadialGradient(
            centerX,
            centerY,
            radius,
            intArrayOf(
                Color.rgb(26, 0, 10),
                Color.rgb(10, 0, 5),
                Color.BLACK
            ),
            null,
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient

        canvas.drawRect(
            0f,
            0f,
            width,
            height,
            paint
        )

        paint.shader = null
    }

    private fun drawEmbers(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {

        val now = System.currentTimeMillis()
        val delta =
            (now - lastTime) / 1000f

        lastTime = now

        emberTimer += delta

        if (emberTimer > 0.35f) {
            emberTimer = 0f

            if (embers.size < 20) {
                createEmber()
            }
        }

        val iterator = embers.iterator()

        while (iterator.hasNext()) {

            val ember = iterator.next()

            ember.y -= ember.speed * delta

            val fade =
                (ember.y / height)
                    .coerceIn(0f, 1f)

            val alpha =
                (ember.alpha * fade)
                    .toInt()
                    .coerceIn(0, 255)

            paint.color = Color.argb(
                alpha,
                Color.red(ember.color),
                Color.green(ember.color),
                Color.blue(ember.color)
            )

            paint.style = Paint.Style.FILL

            canvas.drawCircle(
                ember.x,
                ember.y,
                ember.radius,
                paint
            )

            if (ember.y < -20f) {
                iterator.remove()
            }
        }
    }

    private fun drawRunicBorder(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {

        paint.style = Paint.Style.STROKE

        paint.strokeWidth = 2f
        paint.color = Color.argb(
            50,
            139,
            0,
            0
        )

        canvas.drawRect(
            20f,
            20f,
            width - 20f,
            height - 20f,
            paint
        )

        paint.strokeWidth = 1f

        paint.color = Color.argb(
            38,
            74,
            0,
            0
        )

        canvas.drawRect(
            30f,
            30f,
            width - 30f,
            height - 30f,
            paint
        )

        paint.strokeWidth = 2.5f

        paint.color = Color.argb(
            128,
            139,
            0,
            0
        )

        val corners = listOf(
            Pair(20f, 20f),
            Pair(width - 20f, 20f),
            Pair(20f, height - 20f),
            Pair(width - 20f, height - 20f)
        )

        corners.forEach { (x, y) ->

            if (x < width / 2) {

                canvas.drawLine(
                    x,
                    y,
                    x + 40f,
                    y,
                    paint
                )

            } else {

                canvas.drawLine(
                    x,
                    y,
                    x - 40f,
                    y,
                    paint
                )
            }

            if (y < height / 2) {

                canvas.drawLine(
                    x,
                    y,
                    x,
                    y + 35f,
                    paint
                )

            } else {

                canvas.drawLine(
                    x,
                    y,
                    x,
                    y - 35f,
                    paint
                )
            }
        }

        paint.style = Paint.Style.FILL
    }

    private fun drawBloodDrips(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {

        val drips = floatArrayOf(
            45f,
            120f,
            240f,
            380f,
            520f,
            640f,
            760f,
            850f
        )

        drips.forEachIndexed { index, x ->

            if (x > width) return@forEachIndexed

            val length =
                20f + (index % 3) * 12f

            paint.color = Color.argb(
                70,
                139,
                0,
                0
            )

            paint.strokeWidth = 2.5f

            canvas.drawLine(
                x,
                0f,
                x + 2f,
                length,
                paint
            )

            canvas.drawCircle(
                x + 2f,
                length + 4f,
                4f,
                paint
            )
        }
    }

    private fun drawVignette(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {

        val gradient = RadialGradient(
            width / 2f,
            height / 2f,
            width * 0.8f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(
                    190,
                    0,
                    0,
                    0
                )
            ),
            null,
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient

        canvas.drawRect(
            0f,
            0f,
            width,
            height,
            paint
        )

        paint.shader = null
    }
}
