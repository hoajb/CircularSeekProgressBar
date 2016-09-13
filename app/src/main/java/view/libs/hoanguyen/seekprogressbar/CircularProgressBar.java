package view.libs.hoanguyen.seekprogressbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * Created by hoa.nguyen
 */
public class CircularProgressBar extends View {
    private static final int BACKGROUND_COLOR = Color.LTGRAY;
    private static final int MAX_PROGRESS = 100;
    private static final int MIN_PROGRESS = 0;
    private static final float STROKE_WIDTH = 20.0f;
    private RectF mProgressBounds;
    private RectF mHalfCircleBounds;

    private int mLayoutHeight = 0;
    private int mLayoutWidth = 0;
    protected float mStrokeWidth = STROKE_WIDTH;

    private Paint mPaintTextCenter;
    private Paint mPaintTextTop;
    private Paint mPaintTextBottom;
    private Paint mPaintProgress;
    private Paint mPaintHalfCircle;
    private Paint mPaintProgressBackground;

    protected int mTextTopColor;
    protected int mTextBottomColor;
    protected int mBackgroundColor;
    protected int mProgressBackgroundColor;
    private int mProgressColor;
    protected int mProgressValue = 0;
    protected float mAngle = 0;
    protected int mMaxProgress = MAX_PROGRESS;
    protected int mMinProgress = MIN_PROGRESS;
    protected float mMinAngle = 0;
    protected float mAnglePerProgress;
    private float mFontSizeCenter = 14;
    private float mFontSizeTop = 10;
    private float mFontSizeBottom = 10;
    private String mTextCenter;
    protected String mTextTop;
    protected String mTextBottom;

    protected float mCx = 0;
    protected float mCy = 0;
    protected float mX = 0;
    protected float mY = 0;
    protected float mProgressRadius = 0;

    protected int mQuadrant;
    protected String mFormatString;
    protected int[] mColors;
    protected int mPercentTouchVirtual;

    private boolean mAutoChangeTextColor;
    protected boolean mEnableCircleHead;

    public interface OnProgressChangeListener {
        void onProgressChange(CircularProgressBar seekBar, int newValue);

        void onChanged(CircularProgressBar seekBar, int value);
    }

    protected OnProgressChangeListener mListener;

    public CircularProgressBar(Context context) {
        super(context);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);

    }

    protected void init(AttributeSet attrs, int style) {
        int green = getResources().getColor(R.color.circle_Green);
        int orange = getResources().getColor(R.color.circle_Orange);
        int lightOrange = getResources().getColor(R.color.circle_Light_Orange);
        int lime = getResources().getColor(R.color.circle_Lime);
        int yellow = getResources().getColor(R.color.circle_Yellow);
        mColors = new int[]{green, yellow, lime, lightOrange, orange};
        mProgressBounds = new RectF();
        mHalfCircleBounds = new RectF();
        TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar, style, 0);
        mStrokeWidth = ta.getDimension(R.styleable.RoundProgressBar_cp_strokeWidth,
                STROKE_WIDTH);
        mAutoChangeTextColor = ta.getBoolean(R.styleable.RoundProgressBar_cp_auto_change_text_color,
                true);
        mEnableCircleHead = ta.getBoolean(R.styleable.RoundProgressBar_cp_enable_circle_head,
                false);
        mBackgroundColor = ta.getColor(R.styleable.RoundProgressBar_cp_bg_color,
                BACKGROUND_COLOR);
        mProgressColor = ta.getColor(R.styleable.RoundProgressBar_cp_progress_color,
                Color.MAGENTA);
        mProgressBackgroundColor = ta.getColor(R.styleable.RoundProgressBar_cp_progress_background_color,
                Color.MAGENTA);
        mMaxProgress = ta.getInteger(R.styleable.RoundProgressBar_cp_max_progress,
                MAX_PROGRESS);
        mMinProgress = ta.getInteger(R.styleable.RoundProgressBar_cp_min_progress,
                MIN_PROGRESS);
        this.setMaxProgress(mMaxProgress);
        mProgressValue = ta.getInteger(R.styleable.RoundProgressBar_cp_progress,
                MAX_PROGRESS);
        mPercentTouchVirtual = ta.getInteger(R.styleable.RoundProgressBar_cp_percent_touch_virtual,
                100);
        mPercentTouchVirtual = Math.max(100, mPercentTouchVirtual);

        final int formatBase = ta.getResourceId(R.styleable.RoundProgressBar_cp_format_string, R.string.format_string_base_2);
        mFormatString = getResources().getString(formatBase);
        mTextTop = ta.getString(R.styleable.RoundProgressBar_cp_text_top);
        mTextBottom = ta.getString(R.styleable.RoundProgressBar_cp_text_bottom);

        mTextTopColor = ta.getColor(R.styleable.RoundProgressBar_cp_text_topColor,
                BACKGROUND_COLOR);

        mTextBottomColor = ta.getColor(R.styleable.RoundProgressBar_cp_text_bottomColor,
                BACKGROUND_COLOR);
        setFormatString(mFormatString);
        setProgressValue(mProgressValue);
        mAngle = getAngle(mProgressValue);

        mFontSizeCenter = ta.getDimension(R.styleable.RoundProgressBar_cp_textSize,
                mFontSizeCenter);
        mFontSizeTop = ta.getDimension(R.styleable.RoundProgressBar_cp_text_topSize,
                mFontSizeTop);
        mFontSizeBottom = ta.getDimension(R.styleable.RoundProgressBar_cp_text_bottomSize,
                mFontSizeBottom);

        mTextCenter = ta.getString(R.styleable.RoundProgressBar_cp_text_center);

        setupPaints();
        ta.recycle();
    }

    protected void setupPaints() {
        Resources r = getResources();
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
                mStrokeWidth,
                r.getDisplayMetrics());

        mPaintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextTop = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextBottom = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaintTextCenter.setColor(mProgressColor);
        mPaintTextCenter.setTextAlign(Paint.Align.CENTER);
        mPaintTextCenter.setTextSize(mFontSizeCenter);

        mPaintTextTop.setColor(mTextTopColor);
        mPaintTextTop.setTextAlign(Paint.Align.CENTER);
        mPaintTextTop.setTextSize(mFontSizeTop);

        mPaintTextBottom.setColor(mTextBottomColor);
        mPaintTextBottom.setTextAlign(Paint.Align.CENTER);
        mPaintTextBottom.setTextSize(mFontSizeBottom);

        setupDefaultPaint(mPaintProgress, mStrokeWidth);

        mPaintHalfCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintHalfCircle.setStyle(Paint.Style.FILL);

        mPaintProgressBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintProgressBackground.setColor(mProgressBackgroundColor);
        setupDefaultPaint(mPaintProgressBackground, mStrokeWidth);
    }

    protected void setupDefaultPaint(Paint paint, float strokeWidth) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        mLayoutWidth = newWidth;
        mLayoutHeight = newHeight;
        mCx = newWidth / 2;
        mCy = newHeight / 2;
        int[] location = new int[2];
        getLocationOnScreen(location);
        mX = location[0];
        mY = location[1];
        setupBounds();
    }

    protected void setupBounds() {
        int minValue = Math.min(mLayoutWidth, mLayoutHeight);

        int xOffset = mLayoutWidth - minValue;
        int yOffset = mLayoutHeight - minValue;
        mProgressRadius = Math.min(mLayoutWidth - xOffset, mLayoutHeight - yOffset) / 2;
        updateProgressBounds();

        mProgressRadius = mProgressRadius * 100 / mPercentTouchVirtual;
        updateProgressBounds();
        Shader s = new SweepGradient(mCx, mCy, mColors, null);
        mPaintProgress.setShader(s);
        mPaintHalfCircle.setShader(s);
    }

    protected void updateProgressBounds() {
        float halfStroke = mStrokeWidth / 2;
        mProgressBounds.left = mCx - mProgressRadius + halfStroke;
        mProgressBounds.top = mCy - mProgressRadius + halfStroke;
        mProgressBounds.right = mCx + mProgressRadius - halfStroke;
        mProgressBounds.bottom = mCy + mProgressRadius - halfStroke;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mProgressValue > 0) {
            this.mTextCenter = String.format(mFormatString, mProgressValue);
        } else {
            this.mTextCenter = String.format(getResources().getString(R.string.format_string_base_2), mProgressValue);
        }

        this.drawMultilineText(canvas);
        this.drawTextTop(canvas);
        this.drawTextBottom(canvas);

        canvas.rotate(270, mCx, mCy);

        canvas.drawArc(mProgressBounds, mAngle, 360, false, mPaintProgressBackground);
        canvas.drawArc(mProgressBounds, 0, mAngle, false, mPaintProgress);

        if (mEnableCircleHead) {
            reCalBound(mAngle);
            canvas.drawArc(mHalfCircleBounds, 0, 360, false, mPaintHalfCircle);
        }
    }

    private void reCalBound(float angleDe) {
        double ang = (angleDe / 180) * Math.PI;
        float halfStroke = mStrokeWidth / 2;
        mHalfCircleBounds.left = (float) (mCx - halfStroke + (mProgressRadius - halfStroke) * Math.cos(ang));
        mHalfCircleBounds.top = (float) (mCy - halfStroke + (mProgressRadius - halfStroke) * Math.sin(ang));
        mHalfCircleBounds.right = (float) (mCx + halfStroke + (mProgressRadius - halfStroke) * Math.cos(ang));
        mHalfCircleBounds.bottom = (float) (mCy + halfStroke + (mProgressRadius - halfStroke) * Math.sin(ang));
    }


    private void drawMultilineText(Canvas canvas) {
        if (!TextUtils.isEmpty(mTextCenter)) {
            float xPos = mCx;
            float yPos = (int) (mCy - ((mPaintTextCenter.descent() + mPaintTextCenter.ascent()) / 2));
            canvas.drawText(mTextCenter, xPos, yPos, mPaintTextCenter);
        }
    }

    private void drawTextTop(Canvas canvas) {
        if (!TextUtils.isEmpty(mTextTop)) {
            float xPos = mCx;
            float yPos = (int) (mCy * (1 - ((float) 100 / (2 * mPercentTouchVirtual))) - ((mPaintTextTop.descent() + mPaintTextTop.ascent()) / 2));
            canvas.drawText(mTextTop, xPos, yPos, mPaintTextTop);
        }
    }

    private void drawTextBottom(Canvas canvas) {
        if (!TextUtils.isEmpty(mTextBottom)) {
            float xPos = mCx;
            float yPos = (int) (mCy * (1 + ((float) 100 / (2 * mPercentTouchVirtual))) - ((mPaintTextBottom.descent() + mPaintTextBottom.ascent()) / 2));
            canvas.drawText(mTextBottom, xPos, yPos, mPaintTextBottom);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mCx = mCy = min / 2;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
        mAnglePerProgress = 360f / maxProgress;
        mMinAngle = mAnglePerProgress * mMinProgress;
        updateAngle(mProgressValue);
        updateProgressColor();
        updateQuadrant();
        postInvalidate();
    }

    public void setMinProgress(int minProgress) {
        mMinProgress = minProgress;
        mMinAngle = minProgress * mAnglePerProgress;
        postInvalidate();
    }

    protected void updateAngle(int progressValue) {
        if (progressValue > mMaxProgress) {
            progressValue = mMaxProgress;
        }
        mAngle = getAngle(progressValue);
    }

    private void updateProgressColor() {
        if (mAutoChangeTextColor) {
            float unit = mAngle / 360;
            if (unit < 0) {
                unit += 1;
            }
            int newColor = interpColor(mColors, unit);
            if (mProgressColor != newColor) {
                mProgressColor = newColor;
//                if (mPaintProgress != null)
//                    mPaintProgress.setColor(mProgressColor);
                if (mPaintTextCenter != null)
                    mPaintTextCenter.setColor(mProgressColor);
            }
        }
    }

    private void updateQuadrant() {
        if (mAngle <= 90 && mAngle > 0) {
            mQuadrant = 1;
        } else if (mAngle <= 180) {
            mQuadrant = 2;
        } else if (mAngle <= 270) {
            mQuadrant = 3;
        } else {
            mQuadrant = 4;
        }
    }

    private void updateProgress(float angle) {
        int progressValue = (int) (angle / mAnglePerProgress);
        if (mProgressValue != progressValue) {
            mProgressValue = progressValue;
            if (mListener != null) {
                mListener.onProgressChange(this, mProgressValue);
            }
        }
    }

    public void setProgressValue(int progressValue) {
        validateProgressValue(progressValue);
        if (mProgressValue != progressValue) {
            this.mProgressValue = progressValue;
            if (mListener != null) {
                mListener.onProgressChange(this, mProgressValue);
            }
        }
        updateAngle(mProgressValue);
        updateQuadrant();
        updateProgressColor();
        postInvalidate();

    }

    protected void setAngle(float angle) {
//        Log.d("canvas", "angle: " + angle);
        mAngle = angle;
        updateProgress(angle);
        updateQuadrant();
        updateProgressColor();
        postInvalidate();
    }

    private void validateProgressValue(int mProgressValue) {
        if (mProgressValue < mMinProgress || mProgressValue > mMaxProgress) {
            throw new IllegalArgumentException("Value=" + mProgressValue + " must be between min and max");
        }
    }

    private float getAngle(int mProgressValue) {
        return (360 * (((float) mProgressValue) / mMaxProgress));
    }

    public int getProgressValue() {
        return mProgressValue;
    }


    public void setFormatString(String format) {
        mFormatString = format;
        mTextCenter = String.format(format, mProgressValue);
        postInvalidate();
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        mListener = listener;
    }
}
