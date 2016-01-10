package com.alexzh.circleimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Custom ImageView for circular images in Android.
 */
public class CircleImageView extends ImageView {
    private final static String TAG = "CIRCLE_IMAGE_VIEW";

    private final static float CIRCLE_DEGREE = 360;
    private final static float BORDER_WIDTH = 0.0f;
    private final static float SHADOW_RADIUS = 0.0f;
    private final static float SHADOW_DX = 2.0f;
    private final static float SHADOW_DY = 2.0f;

    private boolean mHasBorder;
    private float mBorderWidth;
    private int mBorderColor;
    private int mBorderSelectedColor;
    private boolean mIsSelected;

    private boolean mHasShadow;
    private float mShadowDx;
    private float mShadowDy;
    private float mShadowRadius;
    private int mShadowColor;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private float mMeasuredWidth;
    private float mMeasuredHeight;
    private float mMinCanvasSide;

    private Paint mImagePaint;
    private Paint mBorderPaint;
    private Paint mBackgroundPaint;
    private Bitmap mImage;

    private ItemSelectedListener mListener;

    public CircleImageView(Context context) {
        this(context, null, R.styleable.CircleImageViewStyle_circleImageViewDefault);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CircleImageViewStyle_circleImageViewDefault);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mImagePaint = new Paint();
        mBorderPaint = new Paint();
        mBackgroundPaint = new Paint();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);
        int backgroundColor = attributes.getColor(R.styleable.CircleImageView_view_backgroundColor,
                context.getResources().getColor(android.R.color.transparent));

        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();

        mBorderWidth = attributes.getDimension(R.styleable.CircleImageView_view_borderWidth, BORDER_WIDTH);
        mShadowRadius = attributes.getDimension(R.styleable.CircleImageView_view_shadowRadius, SHADOW_RADIUS);

        setBackgroundColor(backgroundColor);

        mImagePaint.setAntiAlias(true);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        if (mBorderWidth > 0) {
            mBorderColor = attributes.getColor(R.styleable.CircleImageView_view_borderColor,
                    context.getResources().getColor(android.R.color.white));
            mBorderSelectedColor = attributes.getColor(R.styleable.CircleImageView_view_selectedColor,
                    context.getResources().getColor(android.R.color.white));
            setBorderWidth(mBorderWidth);
            setBorderColor(mBorderColor);
            mHasBorder = true;
        }

        if (mShadowRadius > 0) {
            mShadowDx = attributes.getDimension(R.styleable.CircleImageView_view_shadowDx, SHADOW_DX);
            mShadowDy = attributes.getDimension(R.styleable.CircleImageView_view_shadowDy, SHADOW_DY);
            mShadowColor = attributes.getColor(R.styleable.CircleImageView_view_shadowColor,
                    context.getResources().getColor(android.R.color.black));
            mHasShadow = true;
            drawShadow();
        }

        attributes.recycle();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        RectF rect;
        float centerX, centerY, radius;
        float maxShadowValue;

        if (mShadowDx > mShadowDy)
            maxShadowValue = mShadowDx;
        else if (mShadowDx < mShadowDy)
            maxShadowValue = mShadowDy;
        else
            maxShadowValue = mShadowDx;

        mMeasuredWidth = (getMeasuredWidth() - mPaddingLeft - mPaddingRight);
        mMeasuredHeight = (getMeasuredHeight() - mPaddingTop - mPaddingBottom);

        float oldCanvasSize = mMinCanvasSide;
        mMinCanvasSide = mMeasuredWidth < mMeasuredHeight ? mMeasuredWidth : mMeasuredHeight;

        if (oldCanvasSize != mMinCanvasSide)
            updateBitmapShader();

        centerX = ((mMeasuredWidth - maxShadowValue) / 2.0f) + mPaddingLeft;
        centerY = ((mMeasuredHeight - maxShadowValue) / 2.0f) + mPaddingTop;

        if (mMeasuredHeight > mMeasuredWidth) {
            centerY -= (mMeasuredHeight - mMeasuredWidth) / 2.0f;
        } else if (mMeasuredWidth > mMeasuredHeight) {
            centerX -= (mMeasuredWidth - mMeasuredHeight) / 2.0f;
        }

        radius = (mMinCanvasSide / 2.0f) - mBorderWidth - maxShadowValue - mShadowRadius;

        /* draw border */
        if (mHasBorder) {
            if (mHasShadow)
                mMinCanvasSide = (int) (mMinCanvasSide - mShadowRadius - maxShadowValue);

            rect = getBorderRectF(maxShadowValue);

            if (mIsSelected) {
                setBorderColor(mBorderSelectedColor);
                canvas.drawArc(rect, CIRCLE_DEGREE, CIRCLE_DEGREE, false, mBorderPaint);
            } else {
                setBorderColor(mBorderColor);
                canvas.drawArc(rect, CIRCLE_DEGREE, CIRCLE_DEGREE, false, mBorderPaint);
            }
        }

        /* draw background */
        canvas.drawCircle(centerX, centerY, radius, mBackgroundPaint);

        /* draw image */
        canvas.drawCircle(centerX, centerY, radius, mImagePaint);
    }

    private RectF getBorderRectF(float maxShadowValue) {
        float left = ((0 + mBorderWidth + mShadowRadius) / 2) + maxShadowValue;
        float top = ((0 + mBorderWidth + mShadowRadius) / 2) + maxShadowValue;
        float right = ((mMinCanvasSide - (mBorderWidth - mShadowRadius) / 2)) - maxShadowValue;
        float bottom = ((mMinCanvasSide - (mBorderWidth - mShadowRadius) / 2)) - maxShadowValue;

        left += mPaddingLeft;
        top += mPaddingTop;
        right += mPaddingLeft;
        bottom += mPaddingTop;

        return new RectF(left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (!isClickable()) {
            mIsSelected = false;
            return super.onTouchEvent(event);
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.mIsSelected = !mIsSelected;
                if (mIsSelected && mListener != null) {
                    mListener.onSelected(this);
                } else if (!mIsSelected && mListener != null) {
                    mListener.onUnselected(this);
                }
                break;
        }
        // Redraw image and return super type
        invalidate();
        return super.dispatchTouchEvent(event);
    }

    /**
     * Draw shadow for image or border
     */
    private void drawShadow() {
        if (mHasBorder)
            mBorderPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
        else
            mBackgroundPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
    }


    /**
     * Sets the CircleImageView's selected listener.
     * @param listener The new listener to set for image.
     */
    public void setOnItemSelectedClickListener(ItemSelectedListener listener) {
        this.mListener = listener;
    }

    /**
     * Sets the CircleImageView's background color.
     * @param backgroundColor The new color to set the background.
     */
    public void setBackgroundColor(int backgroundColor) {
        if (mBackgroundPaint != null)
            mBackgroundPaint.setColor(backgroundColor);
        invalidate();
    }

    /**
     * Sets the CircleImageView's basic border color.
     * @param borderColor The new color to set the border.
     */
    public void setBorderColor(int borderColor) {
        if (mBorderPaint != null)
            mBorderPaint.setColor(borderColor);
        invalidate();
    }

    /**
     * Sets the CircleImageView's border width in pixels.
     * @param borderWidth Width in pixels for the border.
     */
    public void setBorderWidth(float borderWidth) {
        this.mBorderWidth = borderWidth;
        if(mBorderPaint != null)
            mBorderPaint.setStrokeWidth(borderWidth);
        requestLayout();
        invalidate();
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)
            return null;
        else if (drawable instanceof BitmapDrawable) {
            Log.i(TAG, "Bitmap drawable!");
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (!(intrinsicWidth > 0 && intrinsicHeight > 0))
            return null;

        try {
            Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Encountered OutOfMemoryError while generating bitmap!");
            return null;
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);

        // Extract a Bitmap out of the drawable & set it as the main shader
        mImage = drawableToBitmap(getDrawable());
        if(mMinCanvasSide > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);

        // Extract a Bitmap out of the drawable & set it as the main shader
        mImage = drawableToBitmap(getDrawable());
        if(mMinCanvasSide > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        // Extract a Bitmap out of the drawable & set it as the main shader
        mImage = drawableToBitmap(getDrawable());
        if(mMinCanvasSide > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        mImage = bm;
        if(mMinCanvasSide > 0)
            updateBitmapShader();
    }

    /**
     * Re-initializes the shader texture used to fill in
     * the Circle upon drawing.
     */
    public void updateBitmapShader() {
        float scale;

        if (mImage == null)
            return;

        BitmapShader bitmapShader = new BitmapShader(mImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        if(mMinCanvasSide != mImage.getWidth() || mMinCanvasSide != mImage.getHeight()) {
            Matrix matrix = new Matrix();

            if (mMeasuredHeight > mMeasuredWidth)
                scale = (mMeasuredWidth - mBorderWidth) / mImage.getHeight();
            else
                scale = (mMeasuredHeight - mBorderWidth) / mImage.getWidth();

            matrix.setScale(scale, scale);
            matrix.postTranslate(mPaddingLeft + mBorderWidth, mPaddingTop + mBorderWidth);
            bitmapShader.setLocalMatrix(matrix);

            mImagePaint.setShader(bitmapShader);
        }
    }
}
