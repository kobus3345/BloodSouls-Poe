package com.darkforge.bloodsouls.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class SkullView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {

        val w = width.toFloat()
        val h = height.toFloat()

        val skull = Path()

        skull.moveTo(
            w * .5f,
            h * .09f
        )

        skull.cubicTo(
            w * .28f,
            h * .09f,
            w * .16f,
            h * .25f,
            w * .16f,
            h * .44f
        )

        skull.cubicTo(
            w * .16f,
            h * .56f,
            w * .22f,
            h * .66f,
            w * .31f,
            h * .72f
        )

        skull.lineTo(
            w * .31f,
            h * .84f
        )

        skull.lineTo(
            w * .69f,
            h * .84f
        )

        skull.lineTo(
            w * .69f,
            h * .72f
        )

        skull.cubicTo(
            w * .78f,
            h * .66f,
            w * .84f,
            h * .56f,
            w * .84f,
            h * .44f
        )

        skull.cubicTo(
            w * .84f,
            h * .25f,
            w * .72f,
            h * .09f,
            w * .5f,
            h * .09f
        )

        skull.close()

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(
            26,
            0,
            8
        )

        canvas.drawPath(
            skull,
            paint
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.rgb(
            139,
            0,
            0
        )

        canvas.drawPath(
            skull,
            paint
        )

        drawEye(
            canvas,
            w * .375f,
            h * .44f,
            w,
            h
        )

        drawEye(
            canvas,
            w * .625f,
            h * .44f,
            w,
            h
        )

        val jaw = Path()

        jaw.moveTo(
            w * .31f,
            h * .68f
        )

        jaw.quadTo(
            w * .5f,
            h * .63f,
            w * .69f,
            h * .68f
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.argb(
            128,
            139,
            0,
            0
        )

        canvas.drawPath(
            jaw,
            paint
        )
    }

    private fun drawEye(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK

        canvas.drawCircle(
            x,
            y,
            w * .09f,
            paint
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.argb(
            230,
            139,
            0,
            0
        )

        canvas.drawCircle(
            x,
            y,
            w * .09f,
            paint
        )

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(
            204,
            204,
            0,
            0
        )

        canvas.drawCircle(
            x,
            y + h * .03f,
            w * .031f,
            paint
        )
    }
}