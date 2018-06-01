package com.alexzh.circleimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.nineoldandroids.animation.ObjectAnimator;

import static com.alexzh.circleimageview.utils.MathUtils.getBorderMax;
import static com.alexzh.circleimageview.utils.MathUtils.twiceValue;

/**
 * Custom ImageView for circular images in Android.
 */
public class CircleImageView extends ImageView {
    private static final int DEFAULT_SHADOW_COLOR = Color.BLACK;
    private static final int DEFAULT_BORDER_COLOR = Color.WHITE;
    private static final int DEFAULT_BORDER_SELECTED_COLOR = Color.RED;

    private static final float DEFAULT_SHADOW_RADIUS = 0.0f;
    private static final float DEFAULT_SHADOW_DX = 0.0f;
    private static final float DEFAULT_SHADOW_DY = 0.0f;
    private static final int DEFAULT_BORDER_WIDTH = 0;

    private static final float DEFAULT_PRESSED_RING_WIDTH = 0.0f;
    private static final int DEFAULT_PRESSED_RING_COLOR = Color.CYAN;

    private static final int ANIMATION_TIME = android.R.integer.config_shortAnimTime;
    private static final String ANIMATION_PROGRESS_ATTR = "animationProgress";

    private int mBorderWidth;
    private int mCanvasSize;
    private float mShadowRadius;
    private int mShadowColor;
    private float mShadowDx;
    private float mShadowDy;

    private Bitmap mImageBitmap;
    private Drawable mDrawable;
    private Paint mPaintImage;
    private Paint mPaintBorder;
    private Paint mPaintBackground;

    private int mBorderColor;
    private int mBorderSelectedColor;

    private int mCenterX;
    private int mCenterY;
    private int mRadius;

    private ItemSelectedListener mSelectedListener;
    private boolean mIsSelectedState;

    private int mBackgroundColor;
    private float mAnimationProgress;

    private ObjectAnimator mPressedAnimator;
    private int mPressedRingColor;
    private int mPressedRingWidth;
    private int mCurrentPressedRingWidth;

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

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setClickable(true);

        // Load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyleAttr, 0);
        mBackgroundColor = attributes.getColor(R.styleable.CircleImageView_view_backgroundColor,
                context.getResources().getColor(android.R.color.transparent));
        mBorderColor = attributes.getColor(R.styleable.CircleImageView_view_borderColor, DEFAULT_BORDER_COLOR);
        mBorderWidth = attributes.getDimensionPixelOffset(R.styleable.CircleImageView_view_borderWidth, DEFAULT_BORDER_WIDTH);
        mBorderSelectedColor = attributes.getColor(R.styleable.CircleImageView_view_selectedColor, DEFAULT_BORDER_SELECTED_COLOR);
        mShadowRadius = attributes.getDimension(R.styleable.CircleImageView_view_shadowRadius, DEFAULT_SHADOW_RADIUS);
        mShadowColor = attributes.getColor(R.styleable.CircleImageView_view_shadowColor, DEFAULT_SHADOW_COLOR);
        mShadowDx = attributes.getDimension(R.styleable.CircleImageView_view_shadowDx, DEFAULT_SHADOW_DX);
        mShadowDy = attributes.getDimension(R.styleable.CircleImageView_view_shadowDy, DEFAULT_SHADOW_DY);
        mPressedRingWidth = (int) attributes.getDimension(R.styleable.CircleImageView_view_pressedRingWidth, DEFAULT_PRESSED_RING_WIDTH);
        mPressedRingColor = attributes.getColor(R.styleable.CircleImageView_view_pressedRingColor, DEFAULT_PRESSED_RING_COLOR);
        attributes.recycle();

        //Init pressed animation
        mPressedAnimator = ObjectAnimator.ofFloat(this, ANIMATION_PROGRESS_ATTR, 0f, 0f);
        mPressedAnimator.setDuration(getResources().getInteger(ANIMATION_TIME));

        // Init paint
        mPaintImage = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);

        //background color
        setBackgroundColor(mBackgroundColor);

        //border color
        setBorderColor(mBorderColor);

        //border width
        setBorderWidth(mBorderWidth);

        //shadow radius, color, dx, dy
        drawShadow(mShadowRadius, mShadowColor, mShadowDx, mShadowDy);
    }

    public void setOnItemSelectedClickListener(ItemSelectedListener listener) {
        this.mSelectedListener = listener;
    }

    public void setBorderWidth(int borderWidth) {
        if (mBackgroundColor != borderWidth) {
            this.mBorderWidth = borderWidth;
        }
        requestLayout();
        invalidate();
    }

    public void setBorderColor(int borderColor) {
        if (mPaintBorder != null)
            mPaintBorder.setColor(borderColor);
        invalidate();
    }

    public void setBackgroundColor(int backgroundColor) {
        if (mPaintBackground != null)
            mPaintBackground.setColor(backgroundColor);
        invalidate();
    }

    public void addShadow() {
        if (mShadowRadius == 0)
            mShadowRadius = DEFAULT_SHADOW_RADIUS;
        drawShadow(mShadowRadius, mShadowColor, mShadowDx, mShadowDy);
        invalidate();
    }

    public void setShadowRadius(float shadowRadius) {
        drawShadow(shadowRadius, mShadowColor, mShadowDx, mShadowDy);
        invalidate();
    }

    public void setShadowColor(int shadowColor) {
        drawShadow(mShadowRadius, shadowColor, mShadowDx, mShadowDy);
        invalidate();
    }

    public void setShadowDx(float shadowDx) {
        drawShadow(mShadowRadius, mShadowColor, shadowDx, mShadowDy);
        invalidate();
    }

    public void setShadowDy(float shadowDy) {
        drawShadow(mShadowRadius, mShadowColor, mShadowDx, shadowDy);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {

        initImage();

        if (mImageBitmap == null)
            return;

        calculateCircleData(canvas);

        canvas.drawCircle(
                mCenterX + mBorderWidth,
                mCenterY + mBorderWidth,
                mRadius + mBorderWidth - (mShadowRadius + mShadowRadius / 2) + mAnimationProgress,
                mPaintBorder);

        canvas.drawCircle(
                mCenterX + mBorderWidth,
                mCenterY + mBorderWidth,
                mRadius - (mShadowRadius + mShadowRadius / 2),
                mPaintBackground);

        canvas.drawCircle(
                mCenterX + mBorderWidth,
                mCenterY + mBorderWidth,
                mRadius - (mShadowRadius + mShadowRadius / 2),
                mPaintImage);
    }

    private void calculateCircleData(Canvas canvas) {
        if (mRadius != 0)
            return;

        if (!isInEditMode()) {
            mCanvasSize = canvas.getWidth() - getPaddingLeft() - getPaddingRight() - twiceValue(getBorderMax(mPressedRingWidth, mBorderWidth));
            if ((canvas.getHeight() - getPaddingTop() - getPaddingBottom() - twiceValue(getBorderMax(mPressedRingWidth, mBorderWidth))) < mCanvasSize) {
                mCanvasSize = canvas.getHeight() - getPaddingTop() - getPaddingBottom() - twiceValue(getBorderMax(mPressedRingWidth, mBorderWidth));
            }
        }

        //Calculate radius
        mRadius = (mCanvasSize - (getBorderMax(mPressedRingWidth, mBorderWidth) * 2)) / 2;

        //calculate center points
        mCenterX = getPaddingLeft() + mRadius + Math.abs(getBorderMax(mPressedRingWidth, mBorderWidth));
        mCenterY = getPaddingTop() + mRadius + Math.abs(getBorderMax(mPressedRingWidth, mBorderWidth));

        updateShader();
    }

    private void initImage() {
        if (this.mDrawable == getDrawable())
            return;

        this.mDrawable = getDrawable();
        this.mImageBitmap = drawableToBitmap(this.mDrawable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasSize = w;
        mRadius = 0;
        if (h < mCanvasSize)
            mCanvasSize = h;
        if (mImageBitmap != null)
            updateShader();
    }

    private void drawShadow(float shadowRadius, int shadowColor, float shadowDx, float shadowDy) {
        if (mShadowRadius != shadowRadius) {
            this.mShadowRadius = shadowRadius;
        }
        if (mShadowColor != shadowColor) {
            this.mShadowColor = shadowColor;
        }
        if (mShadowDx != shadowDx) {
            this.mShadowDx = shadowDx;
        }
        if (mShadowDy != shadowDy) {
            this.mShadowDy = shadowDy;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, mPaintBorder);
        }
        mPaintBorder.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
    }

    private void updateShader() {
        if (this.mImageBitmap == null)
            return;

        Matrix matrix = new Matrix();

        mImageBitmap = cropBitmap(mImageBitmap);

        BitmapShader shader = new BitmapShader(
                mImageBitmap,
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        RectF bitmapRect = new RectF(0, 0, mImageBitmap.getWidth(), mImageBitmap.getHeight());
        RectF viewRect = new RectF(
                0,
                0,
                mCanvasSize - twiceValue(getBorderMax(mPressedRingWidth, mBorderWidth)),
                mCanvasSize - twiceValue(getBorderMax(mPressedRingWidth, mBorderWidth)));

        matrix.setRectToRect(bitmapRect, viewRect, Matrix.ScaleToFit.CENTER);
        matrix.postTranslate(
                getBorderMax(mPressedRingWidth, mBorderWidth) + getPaddingLeft(),
                getBorderMax(mPressedRingWidth, mBorderWidth) + getPaddingTop());

        shader.setLocalMatrix(matrix);
        mPaintImage.setShader(shader);
    }

    private Bitmap cropBitmap(Bitmap bitmap) {
        Bitmap bmp;
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            bmp = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                    0,
                    bitmap.getHeight(), bitmap.getHeight());
        } else {
            bmp = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                    bitmap.getWidth(), bitmap.getWidth());
        }
        return bmp;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
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
            Log.e(getClass().toString(), "Encountered OutOfMemoryError while generating bitmap!");
            return null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        int imageSize = (width < height) ? width : height;
        setMeasuredDimension(imageSize, imageSize);
    }

    private int measureWidth(int measureSpec) {

        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // The parent has not imposed any constraint on the child.
            result = mCanvasSize;
        }
        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = mCanvasSize;
        }
        return (result + 2);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        if (mPaintBorder != null) {
            if (mIsSelectedState) {
                mPaintBorder.setColor(pressed ? mPressedRingColor : mBorderSelectedColor);
            } else {
                mPaintBorder.setColor(pressed ? mPressedRingColor : mBorderColor);
            }
        }

        mCurrentPressedRingWidth = (int) getAnimationProgress();
        if (pressed) {
            showPressedRing();
        } else {
            hidePressedRing(mCurrentPressedRingWidth);

        }
    }

    private float getAnimationProgress() {
        return mAnimationProgress;
    }

    private void setAnimationProgress(float progress) {
        this.mAnimationProgress = progress;
        invalidate();
    }

    private void showPressedRing() {
        mCurrentPressedRingWidth = 0;
        mPressedAnimator.setFloatValues(mAnimationProgress, mPressedRingWidth);
        mPressedAnimator.start();
        mIsSelectedState = !mIsSelectedState;
    }

    private void hidePressedRing(int currentPressedRingWidth) {
        mPressedAnimator.setFloatValues(currentPressedRingWidth, 0f);
        mPressedAnimator.start();
        if (mIsSelectedState && mSelectedListener != null) {
            mSelectedListener.onSelected(this);
        } else if (!mIsSelectedState && mSelectedListener != null) {
            mSelectedListener.onUnselected(this);
        }
    }
}
