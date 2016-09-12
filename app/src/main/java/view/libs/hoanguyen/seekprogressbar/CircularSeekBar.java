package view.libs.hoanguyen.seekprogressbar;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by hoa.nguyen
 */
public class CircularSeekBar extends CircularProgressBar {
    private int mNextQuadrant;
    private int mPreviousQuadrant;
    private float mAngleStart;

    public CircularSeekBar(Context context) {
        super(context);
    }

    public CircularSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CircularSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CircularSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs, int style) {
        super.init(attrs, style);
        mAngleStart = -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int) event.getRawX();
        int touchY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mAngleStart = pointToAngle(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                float angle = pointToAngle(touchX, touchY);
                if (mAngleStart != -1) {
                    float dAngle = angle - mAngleStart;
                    if (dAngle > 0 && mPreviousQuadrant == 1 && mNextQuadrant == 4) {
                        dAngle = 360 - dAngle;
                    } else if (dAngle < 0 && mPreviousQuadrant == 4 && mNextQuadrant == 1) {
                        dAngle = dAngle + 360;
                    }
                    if (Math.abs(dAngle) > 0.5f) {
                        if (validateMoveAction(dAngle)) {
                            setAngle(mAngle + dAngle);
                        } else if (mQuadrant == 4) {
                            setProgressValue(mMaxProgress);
                        } else if (mQuadrant == 1 || mAngle > 0) {
                            setProgressValue(mMinProgress);
                        }
                        mAngleStart = angle;
                    }
                } else {
                    mAngleStart = angle;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mAngleStart = -1;

                if (mListener != null) {
                    mListener.onChanged(this, getProgressValue());
                }
                break;
        }
        return true;
    }

    @Override
    protected void setAngle(float angle) {
        if (angle < mMinAngle)
            angle = mMinAngle;
        else if (angle > 360)
            angle = 360;
        super.setAngle(angle);
    }

    private boolean validateMoveAction(float dAngle) {
        boolean validate = false;
        if (mAngle == mMinAngle) {
            if (mPreviousQuadrant == mNextQuadrant - 1 && dAngle > 5) {
                validate = true;
            } else if (mPreviousQuadrant == mNextQuadrant && dAngle > 0) {
                validate = true;
            }
        } else if (mAngle == 360) {
            if (mPreviousQuadrant == mNextQuadrant + 1 && dAngle < -5) {
                validate = true;
            } else if (mPreviousQuadrant == mNextQuadrant && dAngle < 0) {
                validate = true;
            }
        } else {
            if (mQuadrant == 4) {
                if (dAngle < 0 && mNextQuadrant + 1 >= mPreviousQuadrant) {
                    return true;
                } else if (dAngle > 0 && mNextQuadrant + 1 >= mPreviousQuadrant) {
                    return true;
                }
            } else if (mQuadrant == 1) {
                if (dAngle > 0 && mNextQuadrant - 1 <= mPreviousQuadrant) {
                    return true;
                } else if (dAngle < 0 && mNextQuadrant >= mPreviousQuadrant - 1) {
                    return true;
                }
            } else {
                validate = true;
            }
        }
        return validate;
    }

    private boolean checkPointInBounds(int x, int y, RectF bounds) {
        if (x < bounds.right && x > bounds.left && y > bounds.top && y < bounds.bottom) {
            return true;
        }
        return false;
    }

    private float pointToAngle(int x, int y) {
        mPreviousQuadrant = mNextQuadrant;
        int cx = (int) (mCx + mX);
        int cy = (int) (mCy + mY);

        if (x >= cx && y < cy) // [0..90]
        {
            mNextQuadrant = 1;
            double opp = x - cx;
            double adj = cy - y;
            return (float) Math.toDegrees(Math.atan(opp / adj));
        } else if (x > cx && y >= cy) // [90..180]
        {
            mNextQuadrant = 2;
            double opp = y - cy;
            double adj = x - cx;
            return 90 + (float) Math.toDegrees(Math.atan(opp / adj));
        } else if (x <= cx && y > cy) // [180..270]
        {
            mNextQuadrant = 3;
            double opp = cx - x;
            double adj = y - cy;
            return 180 + (float) Math.toDegrees(Math.atan(opp / adj));
        } else if (x < cx && y <= cy) // [270..359]
        {
            mNextQuadrant = 4;
            double opp = cy - y;
            double adj = cx - x;
            return 270 + (float) Math.toDegrees(Math.atan(opp / adj));
        }

        throw new IllegalArgumentException();
    }

    private int angleToProgress(int angle) {
        return (mMaxProgress * angle) / 360;
    }
}
