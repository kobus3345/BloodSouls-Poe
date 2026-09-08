package com.bloodsouls.game.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.sin

class BloodSoulsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Drip(
        val xFraction: Float,
        val delayFraction: Float,
        val durationMs: Int
    )

    private data class Ember(
        val xFraction: Float,
        val delayFraction: Float,
        val durationMs: Int
    )

    private val drips = List(14) { i ->
        Drip(
            xFraction = (3f + i * 7f) / 100f,
            delayFraction = (i * 0.4f) / 5f,
            durationMs = ((3f + (i % 4) * 0.7f) * 1000f).toInt()
        )
    }

    private val embers = List(22) { i ->
        Ember(
            xFraction = (1f + i * 4.5f) / 100f,
            delayFraction = (i * 0.22f) / 6f,
            durationMs = ((4f + (i % 5)) * 1000f).toInt()
        )
    }

    private val loadMessages = listOf(
        "Summoning lost souls...",
        "Binding dark covenant...",
        "Awakening the cursed...",
        "Forging blood pacts...",
        "Opening the abyss..."
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val skullGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.5f
        color = Color.rgb(221, 17, 17)
        maskFilter = BlurMaskFilter(
            6f,
            BlurMaskFilter.Blur.SOLID
        )
    }

    private val skullPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.rgb(221, 17, 17)
    }

    private val toothPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(220, 221, 17, 17)
    }

    private val nosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        strokeJoin = Paint.Join.ROUND
        color = Color.argb(220, 221, 17, 17)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(
            Typeface.SERIF,
            Typeface.BOLD
        )
        textAlign = Paint.Align.CENTER
        color = Color.rgb(255, 26, 26)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(
            Typeface.SERIF,
            Typeface.BOLD
        )
        textAlign = Paint.Align.CENTER
        color = Color.rgb(85, 0, 0)
    }

    private val loadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(
            Typeface.SERIF,
            Typeface.BOLD
        )
        textAlign = Paint.Align.CENTER
        color = Color.rgb(74, 53, 37)
    }

    private val percentagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(
            Typeface.SERIF,
            Typeface.BOLD
        )
        textAlign = Paint.Align.CENTER
        color = Color.rgb(58, 32, 21)
    }

    private var titleOffsetY = -40f
    private var titleAlpha = 0f

    private var skullScale = 1f
    private var flickerAlpha = 1f
    private var glowRadius = 14f

    private var particleTime = 0f
    private var shimmerX = -0.35f
    private var pressPulse = 1f

    private var loadProgress = 0f
    private var loadDone = false
    private var loadMessage = loadMessages[0]

    private val animationStartTime =
        System.currentTimeMillis()

    private var titleAnimator: ValueAnimator? = null
    private var loadingAnimator: ValueAnimator? = null

    init {
        setLayerType(
            View.LAYER_TYPE_SOFTWARE,
            null
        )

        startTitleAnimation()
        startLoadingAnimation()

        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        drawBackground(
            canvas,
            width,
            height
        )

        drawAtmosphere(
            canvas,
            width,
            height
        )

        drawTitle(
            canvas,
            width,
            height
        )

        drawSkull(
            canvas,
            width,
            height
        )

        drawLoadingSection(
            canvas,
            width,
            height
        )

        updateAnimations()

        postInvalidateOnAnimation()
    }

    private fun drawBackground(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        val gradient = LinearGradient(
            0f,
            0f,
            width,
            height,
            intArrayOf(
                Color.rgb(9, 0, 0),
                Color.rgb(4, 0, 10),
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

    private fun drawAtmosphere(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        val centerX = width / 2f
        val centerY = height / 2f

        val redGlow = RadialGradient(
            centerX,
            centerY,
            dp(320f),
            intArrayOf(
                Color.argb(
                    24,
                    139,
                    0,
                    0
                ),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )

        paint.shader = redGlow

        canvas.drawCircle(
            centerX,
            centerY,
            dp(320f),
            paint
        )

        paint.shader = null

        val vignette = RadialGradient(
            centerX,
            centerY,
            width * 0.72f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(
                    204,
                    0,
                    0,
                    0
                )
            ),
            null,
            Shader.TileMode.CLAMP
        )

        paint.shader = vignette

        canvas.drawRect(
            0f,
            0f,
            width,
            height,
            paint
        )

        paint.shader = null

        drawDrips(
            canvas,
            width,
            height
        )

        drawEmbers(
            canvas,
            width,
            height
        )

        drawScanLines(
            canvas,
            width,
            height
        )
    }

    private fun drawDrips(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        drips.forEach { drip ->

            val phase =
                (particleTime + drip.delayFraction) % 1f

            val maxHeight =
                height * 0.14f

            val dripHeight = when {
                phase < 0.10f ->
                    maxHeight *
                            (phase / 0.10f)

                phase < 0.70f ->
                    maxHeight

                phase < 0.88f ->
                    maxHeight *
                            (
                                    1f -
                                            (phase - 0.70f) /
                                            0.18f
                                    )

                else ->
                    0f
            }

            if (dripHeight <= 0f) {
                return@forEach
            }

            val alpha = when {
                phase < 0.10f ->
                    phase / 0.10f

                phase > 0.70f ->
                    1f -
                            (phase - 0.70f) /
                            0.18f

                else ->
                    0.8f
            }.coerceIn(
                0f,
                1f
            )

            paint.shader = LinearGradient(
                0f,
                0f,
                0f,
                dripHeight,
                Color.rgb(
                    139,
                    0,
                    0
                ),
                Color.argb(
                    51,
                    192,
                    0,
                    0
                ),
                Shader.TileMode.CLAMP
            )

            paint.alpha =
                (alpha * 255).toInt()

            canvas.drawRect(
                width *
                        drip.xFraction -
                        dp(1f),
                0f,
                width *
                        drip.xFraction +
                        dp(1.5f),
                dripHeight,
                paint
            )

            paint.alpha = 255
            paint.shader = null
        }
    }

    private fun drawEmbers(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        embers.forEach { ember ->

            val speed =
                6000f /
                        ember.durationMs

            val phase =
                (
                        particleTime *
                                speed +
                                ember.delayFraction
                        ) % 1f

            val y =
                height *
                        (1f - phase)

            val x =
                width *
                        ember.xFraction +
                        sin(
                            phase *
                                    PI.toFloat() *
                                    4f
                        ) *
                        dp(12f)

            val alpha = when {
                phase < 0.08f ->
                    phase /
                            0.08f *
                            0.7f

                phase > 0.88f ->
                    (1f - phase) /
                            0.12f *
                            0.4f

                else ->
                    0.55f
            }.coerceIn(
                0f,
                1f
            )

            paint.color =
                Color.rgb(
                    255,
                    68,
                    0
                )

            paint.alpha =
                (alpha * 255).toInt()

            canvas.drawCircle(
                x,
                y,
                dp(2.5f),
                paint
            )

            paint.alpha = 255
        }
    }

    private fun drawScanLines(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        paint.color = Color.BLACK

        var y = 0f

        while (y < height) {

            paint.alpha = 10

            canvas.drawRect(
                0f,
                y,
                width,
                y + dp(2f),
                paint
            )

            y += dp(4f)
        }

        paint.alpha = 255
    }

    private fun drawSkull(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        val skullWidth = dp(88f)
        val skullHeight = dp(78f)

        val centerX = width / 2f

        val centerY =
            height / 2f -
                    dp(105f) +
                    titleOffsetY


        canvas.save()

        canvas.translate(
            centerX,
            centerY
        )

        canvas.scale(
            skullScale,
            skullScale
        )

        val path = Path()

        path.moveTo(
            skullWidth * 0.50f,
            skullHeight * 0.07f
        )

        path.cubicTo(
            skullWidth * 0.22f,
            skullHeight * 0.07f,
            skullWidth * 0.08f,
            skullHeight * 0.24f,
            skullWidth * 0.08f,
            skullHeight * 0.46f
        )

        path.cubicTo(
            skullWidth * 0.08f,
            skullHeight * 0.62f,
            skullWidth * 0.16f,
            skullHeight * 0.74f,
            skullWidth * 0.29f,
            skullHeight * 0.80f
        )

        path.lineTo(
            skullWidth * 0.29f,
            skullHeight * 0.92f
        )

        path.lineTo(
            skullWidth * 0.71f,
            skullHeight * 0.92f
        )

        path.lineTo(
            skullWidth * 0.71f,
            skullHeight * 0.80f
        )

        path.cubicTo(
            skullWidth * 0.84f,
            skullHeight * 0.74f,
            skullWidth * 0.92f,
            skullHeight * 0.62f,
            skullWidth * 0.92f,
            skullHeight * 0.46f
        )

        path.cubicTo(
            skullWidth * 0.92f,
            skullHeight * 0.24f,
            skullWidth * 0.78f,
            skullHeight * 0.07f,
            skullWidth * 0.50f,
            skullHeight * 0.07f
        )

        path.close()

        canvas.drawPath(
            path,
            skullGlowPaint
        )

        canvas.drawPath(
            path,
            skullPaint
        )

        listOf(
            0.385f,
            0.50f,
            0.615f
        ).forEach { x ->

            canvas.drawLine(
                skullWidth * x,
                skullHeight * 0.92f,
                skullWidth * x,
                skullHeight * 0.81f,
                toothPaint
            )
        }

        canvas.drawCircle(
            skullWidth * 0.33f,
            skullHeight * 0.45f,
            skullWidth * 0.115f,
            skullGlowPaint
        )

        canvas.drawCircle(
            skullWidth * 0.33f,
            skullHeight * 0.45f,
            skullWidth * 0.048f,
            skullPaint
        )

        canvas.drawCircle(
            skullWidth * 0.67f,
            skullHeight * 0.45f,
            skullWidth * 0.115f,
            skullGlowPaint
        )

        canvas.drawCircle(
            skullWidth * 0.67f,
            skullHeight * 0.45f,
            skullWidth * 0.048f,
            skullPaint
        )

        val nose = Path()

        nose.moveTo(
            skullWidth * 0.455f,
            skullHeight * 0.645f
        )

        nose.lineTo(
            skullWidth * 0.500f,
            skullHeight * 0.710f
        )

        nose.lineTo(
            skullWidth * 0.545f,
            skullHeight * 0.645f
        )

        canvas.drawPath(
            nose,
            nosePaint
        )

        canvas.restore()
    }

    private fun drawTitle(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        val centerX =
            width / 2f

        titlePaint.textSize =
            dp(52f)

        titlePaint.color =
            Color.argb(
                (flickerAlpha * 255).toInt(),
                255,
                26,
                26
            )

        titlePaint.setShadowLayer(
            glowRadius,
            0f,
            0f,
            Color.argb(
                240,
                204,
                0,
                0
            )
        )

        val titleY =
            height / 2f +
                    dp(55f) +
                    titleOffsetY

        canvas.drawText(
            "BLOOD",
            centerX,
            titleY,
            titlePaint
        )

        canvas.drawText(
            "SOULS",
            centerX,
            titleY + dp(58f),
            titlePaint
        )

        titlePaint.clearShadowLayer()

        subtitlePaint.textSize =
            dp(9f)

        canvas.drawText(
            "D E A T H   A W A I T S",
            centerX,
            titleY + dp(88f),
            subtitlePaint
        )
    }

    private fun drawLoadingSection(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        val centerX =
            width / 2f

        val horizontalPadding =
            dp(48f)

        val left =
            horizontalPadding

        val right =
            width -
                    horizontalPadding

        val barY =
            height -
                    dp(48f)

        loadingPaint.textSize =
            dp(8f)

        if (!loadDone) {

            canvas.drawText(
                loadMessage.uppercase(),
                centerX,
                barY - dp(12f),
                loadingPaint
            )

        } else {

            loadingPaint.color =
                Color.argb(
                    (pressPulse * 255).toInt(),
                    139,
                    0,
                    0
                )

            loadingPaint.textSize =
                dp(9f)

            canvas.drawText(
                "✦   PRESS TO BEGIN   ✦",
                centerX,
                barY + dp(25f),
                loadingPaint
            )

            loadingPaint.color =
                Color.rgb(
                    74,
                    53,
                    37
                )
        }

        paint.color =
            Color.rgb(
                17,
                0,
                0
            )

        canvas.drawRoundRect(
            left,
            barY,
            right,
            barY + dp(4f),
            dp(3f),
            dp(3f),
            paint
        )

        val progressWidth =
            (right - left) *
                    (loadProgress / 100f)

        if (progressWidth > 0f) {

            paint.shader = LinearGradient(
                left,
                0f,
                right,
                0f,
                intArrayOf(
                    Color.rgb(
                        90,
                        0,
                        0
                    ),
                    Color.rgb(
                        204,
                        0,
                        0
                    ),
                    Color.rgb(
                        255,
                        34,
                        0
                    )
                ),
                null,
                Shader.TileMode.CLAMP
            )

            canvas.drawRoundRect(
                left,
                barY,
                left + progressWidth,
                barY + dp(4f),
                dp(3f),
                dp(3f),
                paint
            )

            paint.shader = null

            val shimmerWidth =
                progressWidth * 0.25f

            val shimmerCenter =
                left +
                        progressWidth *
                        shimmerX

            paint.shader = LinearGradient(
                shimmerCenter - shimmerWidth,
                0f,
                shimmerCenter + shimmerWidth,
                0f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(
                        70,
                        255,
                        255,
                        255
                    ),
                    Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )

            canvas.drawRoundRect(
                left,
                barY,
                left + progressWidth,
                barY + dp(4f),
                dp(3f),
                dp(3f),
                paint
            )

            paint.shader = null
        }

        if (!loadDone) {

            percentagePaint.textSize =
                dp(7f)

            canvas.drawText(
                "${loadProgress.toInt()}%",
                centerX,
                barY + dp(18f),
                percentagePaint
            )
        }
    }

    private fun startTitleAnimation() {

        titleAnimator =
            ValueAnimator.ofFloat(
                0f,
                1f
            ).apply {

                duration = 1100L

                startDelay = 400L

                interpolator =
                    DecelerateInterpolator()

                addUpdateListener { animator ->

                    val value =
                        animator.animatedValue
                                as Float

                    titleAlpha =
                        value

                    titleOffsetY =
                        -40f +
                                40f * value

                    invalidate()
                }

                start()
            }
    }

    private fun startLoadingAnimation() {

        loadingAnimator =
            ValueAnimator.ofFloat(
                0f,
                100f
            ).apply {

                duration = 9000L

                startDelay = 400L

                interpolator =
                    LinearInterpolator()

                addUpdateListener { animator ->

                    loadProgress =
                        animator.animatedValue
                                as Float

                    val index =
                        (
                                loadProgress /
                                        100f *
                                        (loadMessages.size - 1)
                                ).toInt()

                    loadMessage =
                        loadMessages[
                            index.coerceIn(
                                0,
                                loadMessages.lastIndex
                            )
                        ]

                    invalidate()
                }

                addListener(
                    object :
                        AnimatorListenerAdapter() {

                        override fun onAnimationEnd(
                            animation: Animator
                        ) {

                            postDelayed({

                                loadDone = true

                                invalidate()

                            }, 400L)
                        }
                    }
                )

                start()
            }
    }

    private fun updateAnimations() {

        val elapsed =
            System.currentTimeMillis() -
                    animationStartTime

        val time =
            elapsed / 1000f

        skullScale =
            1f +
                    0.06f *
                    (
                            (
                                    sin(
                                        time *
                                                2.0 *
                                                PI /
                                                3.0
                                    ) + 1.0
                                    ) / 2.0
                            ).toFloat()

        flickerAlpha =
            (0.92f +
                    0.08f *
                    (
                            sin(
                                time * 1.7
                            ) * 0.5f +
                                    0.5f
                            )).toFloat()

        glowRadius =
            14f +
                    16f *
                    (
                            (
                                    sin(
                                        time *
                                                2.0 *
                                                PI /
                                                2.2
                                    ) + 1.0
                                    ) / 2.0
                            ).toFloat()

        particleTime =
            (
                    elapsed % 6000L
                    ) / 6000f

        shimmerX =
            -0.35f +
                    (
                            elapsed % 1600L
                            ).toFloat() /
                    1600f *
                    1.7f

        pressPulse =
            0.35f +
                    0.65f *
                    (
                            sin(
                                time *
                                        2.0 *
                                        PI /
                                        1.3
                            ) * 0.5f +
                                    0.5f
                            ).toFloat()
    }

    private fun dp(value: Float): Float {
        return value *
                resources.displayMetrics.density
    }

    override fun onDetachedFromWindow() {

        titleAnimator?.cancel()

        loadingAnimator?.cancel()

        super.onDetachedFromWindow()
    }
}
