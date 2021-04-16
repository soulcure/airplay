package com.coocaa.tvpi.module.remote.floatui

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.util.Log
import com.coocaa.publib.utils.DimensUtils
import com.coocaa.tvpi.util.dp
import com.github.mmin18.widget.RealtimeBlurView

class TopRoundRectBlurView(context: Context?, attrs: AttributeSet?) : RealtimeBlurView(context, attrs) {
    private val mPaint = Paint()
    private val mRectF = RectF()
    private val mPath = Path()

    override fun drawBlurredBitmap(canvas: Canvas?, blurredBitmap: Bitmap?, overlayColor: Int) {
        super.drawBlurredBitmap(canvas, blurredBitmap, overlayColor)
        if (blurredBitmap != null) {
            mRectF.right = width.toFloat()
            mRectF.bottom = height.toFloat()

            //画高斯模糊bitmap
            val shader = BitmapShader(
                    blurredBitmap,
                    Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP
            )
            val matrix = Matrix()
            matrix.postScale(
                    mRectF.width() / blurredBitmap.width,
                    mRectF.height() / blurredBitmap.height
            )
            shader.setLocalMatrix(matrix)
            mPaint.shader = shader
            mPath.addRoundRect(
                    mRectF,
                    floatArrayOf(16f.dp,16f.dp,16f.dp,16f.dp,0f,0f,0f,0f),
                    Path.Direction.CW
            )
            canvas?.clipPath(mPath)
            canvas?.drawPath(mPath, mPaint)

            //画默认色
            mPaint.apply {
                reset()
                isAntiAlias = true
                color = overlayColor
            }
            canvas?.drawPath(mPath, mPaint)

            //画描边线
            mPaint.apply {
                reset()
                isAntiAlias = true
                color = Color.parseColor("#14000000")
                strokeWidth = 0.5f.dp
                style = Paint.Style.STROKE
            }
            canvas?.drawPath(mPath,mPaint)
        }
    }
}