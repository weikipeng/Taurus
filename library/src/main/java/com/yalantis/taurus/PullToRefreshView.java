package com.yalantis.taurus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;

public class PullToRefreshView extends ViewGroup {

    private static final int   DRAG_MAX_DISTANCE               = 120;
    private static final float DRAG_RATE                       = .5f;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    public static final int MAX_OFFSET_ANIMATION_DURATION = 700;
    public static final int RESTORE_ANIMATION_DURATION    = 2350;

    private static final int INVALID_POINTER = -1;

    private View         mTarget;
    private ImageView    mRefreshImageView;
    private Interpolator mDecelerateInterpolator;

    /**
     * 滑动的临界值，当滑动距离超过这个值时才认为手势为滑动
     */
    private int               mTouchSlop;
    /**
     * 总共可以拖动的距离
     */
    private int               mTotalDragDistance;
    private RefreshView       mRefreshView;
    private float             mCurrentDragPercent;
    private int               mCurrentOffsetTop;
    private boolean           mRefreshing;
    /**
     * 当前活动的按下点ID
     */
    private int               mActivePointerId;
    private boolean           mIsBeingDragged;
    private float             mInitialMotionY;
    private int               mFrom;
    private float             mFromDragPercent;
    private boolean           mNotify;
    private OnRefreshListener mListener;

    public PullToRefreshView(Context context) {
        this(context, null);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        float density = context.getResources().getDisplayMetrics().density;
        mTotalDragDistance = Math.round((float) DRAG_MAX_DISTANCE * density);
        DevLogTool.getInstance(context).saveLog("手机设备密度density:" + density
                + "\n可以拖动的距离：" + mTotalDragDistance);

        mRefreshImageView = new ImageView(context);
        mRefreshView = new RefreshView(getContext(), this);
        mRefreshImageView.setImageDrawable(mRefreshView);

        addView(mRefreshImageView);
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    /**
     * 获取总共可以拖动的距离
     */
    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ensureTarget();
        if (mTarget == null)
            return;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshImageView.measure(widthMeasureSpec, heightMeasureSpec);
        DevLogTool.getInstance(getContext()).saveLog("------onMeasure mTarget:" + mTarget);
    }

    private void ensureTarget() {
        if (mTarget != null)
            return;
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mRefreshImageView)
                    mTarget = child;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        DevLogTool.getInstance(getContext()).saveLog(
                "-----onInterceptTouchEvent isEnabled():" + isEnabled()
                        + "  canChildScrollUp():" + canChildScrollUp()
                        + "  mRefreshing:" + mRefreshing
                        + "\nev:" + ev

        );
        if (!isEnabled() || canChildScrollUp() || mRefreshing) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTop(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                DevLogTool.getInstance(getContext()).saveLog(
                        "-----onInterceptTouchEvent MotionEvent.ACTION_DOWN"
                                + "\n当前活动的按下点ID mActivePointerId :" + mActivePointerId
                                + "\ninitialMotionY:" + initialMotionY
                );

                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    DevLogTool.getInstance(getContext()).saveLog("mActivePointerId == INVALID_POINTER");
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    DevLogTool.getInstance(getContext()).saveLog("getMotionEventY == -1");
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                DevLogTool.getInstance(getContext()).saveLog("-----onInterceptTouchEvent MotionEvent.ACTION_MOVE"
                        + " y:" + y + " mInitialMotionY:" + mInitialMotionY + " yDiff:" + yDiff
                        + " mTouchSlop:" + mTouchSlop + " mIsBeingDragged:" + mIsBeingDragged
                );
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        DevLogTool.getInstance(getContext()).saveLog(
                "-----onInterceptTouchEvent return mIsBeingDragged:" + mIsBeingDragged
        );

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        DevLogTool.getInstance(getContext()).saveLog(
                "-----onTouchEvent mIsBeingDragged:" + mIsBeingDragged
                        + "\nev:" + ev
        );

        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    DevLogTool.getInstance(getContext()).saveLog("-----onTouchEvent MotionEvent.ACTION_MOVE: pointerIndex < 0");
                    return false;
                }

                final float y         = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff     = y - mInitialMotionY;
                final float scrollTop = yDiff * DRAG_RATE;
                mCurrentDragPercent = scrollTop / mTotalDragDistance;
                DevLogTool.getInstance(getContext()).saveLog(
                        "-----onTouchEvent MotionEvent.ACTION_MOVE"
                                + "\nDRAG_RATE:" + DRAG_RATE
                                + "\nmInitialMotionY:" + mInitialMotionY
                                + "\ny:" + y
                                + "\nyDiff:" + yDiff
                                + "\nscrollTop:" + scrollTop
                                + "\nmCurrentDragPercent:" + mCurrentDragPercent
                );

                if (mCurrentDragPercent < 0) {
                    return false;
                }
                float boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                float extraOS            = Math.abs(scrollTop) - mTotalDragDistance;
                float slingshotDist      = mTotalDragDistance;
                //张力弹力百分比
                float tensionSlingshotPercent = Math.max(0,
                        Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                        (tensionSlingshotPercent / 4), 2)) * 2f;
                float extraMove = (slingshotDist) * tensionPercent / 2;
                int   targetY   = (int) ((slingshotDist * boundedDragPercent) + extraMove);

                DevLogTool.getInstance(getContext()).saveLog(
                        "-----onTouchEvent MotionEvent.ACTION_MOVE"
                                + "\nboundedDragPercent:" + boundedDragPercent
                                + "\nextraOS:" + extraOS
                                + "\nslingshotDist:" + slingshotDist
                                + "\ntensionSlingshotPercent:" + tensionSlingshotPercent
                                + "\ntensionPercent:" + tensionPercent
                                + "\nslingshotDist:" + slingshotDist
                                + "\nextraMove:" + extraMove
                                + "\ntargetY:" + targetY
                                + "\nmCurrentOffsetTop:" + mCurrentOffsetTop
                );
                mRefreshView.setPercent(mCurrentDragPercent);
                setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final int   pointerIndex  = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y             = MotionEventCompat.getY(ev, pointerIndex);
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                DevLogTool.getInstance(getContext()).saveLog("-----onInterceptTouchEvent MotionEvent.ACTION_CANCEL"
                    + "\noverScrollTop:     "+overScrollTop
                    + "\nmTotalDragDistance:    "+mTotalDragDistance
                );
                if (overScrollTop > mTotalDragDistance) {
                    setRefreshing(true, true);
                } else {
                    mRefreshing = false;
                    animateOffsetToPosition(mAnimateToStartPosition);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    private void animateOffsetToPosition(Animation animation) {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = (long) Math.abs(MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent);

        animation.reset();
        animation.setDuration(animationDuration);
        animation.setInterpolator(mDecelerateInterpolator);
        animation.setAnimationListener(mToStartListener);
        mRefreshImageView.clearAnimation();
        mRefreshImageView.startAnimation(animation);
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;

        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(RESTORE_ANIMATION_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        mRefreshImageView.clearAnimation();
        mRefreshImageView.startAnimation(mAnimateToCorrectPosition);

        if (mRefreshing) {
            mRefreshView.start();
            if (mNotify) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
            }
        } else {
            mRefreshView.stop();
            animateOffsetToPosition(mAnimateToStartPosition);
        }
        mCurrentOffsetTop = mTarget.getTop();
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private Animation mAnimateToEndPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
            moveToEnd(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
            int targetTop;
            int endTarget = mTotalDragDistance;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mTarget.getTop();

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime;
            mRefreshView.setPercent(mCurrentDragPercent);

            setTargetOffsetTop(offset, false /* requires update */);
        }

    };

    private void moveToStart(float interpolatedTime) {
        int   targetTop     = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
        int   offset        = targetTop - mTarget.getTop();

        mCurrentDragPercent = targetPercent;
        mRefreshView.setPercent(mCurrentDragPercent);
        setTargetOffsetTop(offset, false);
    }

    private void moveToEnd(float interpolatedTime) {
        int   targetTop     = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f + interpolatedTime);
        int   offset        = targetTop - mTarget.getTop();

        mCurrentDragPercent = targetPercent;
        mRefreshView.setPercent(mCurrentDragPercent);
        setTargetOffsetTop(offset, false);
    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        DevLogTool.getInstance(getContext()).saveLog("-----setRefreshing"
                + " refreshing: "+refreshing
                + " notify: "+notify
        );
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                mRefreshView.setPercent(1f);
                animateOffsetToCorrectPosition();
            } else {
                mRefreshView.setEndOfRefreshing(true);
                animateOffsetToPosition(mAnimateToEndPosition);
            }
        }
    }

    private Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mRefreshView.stop();
            mCurrentOffsetTop = mTarget.getTop();
        }
    };

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId    = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
        mTarget.offsetTopAndBottom(offset);
        mRefreshView.offsetTopAndBottom(offset);
        mCurrentOffsetTop = mTarget.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        ensureTarget();
        if (mTarget == null)
            return;

        int height = getMeasuredHeight();
        int width  = getMeasuredWidth();
        int left   = getPaddingLeft();
        int top    = getPaddingTop();
        int right  = getPaddingRight();
        int bottom = getPaddingBottom();

        DevLogTool.getInstance(getContext()).saveLog("------onLayout left:" + left
                + " \nmRefreshImageView top:" + top
                + " \n目标 mTarget top:" + (top + mCurrentOffsetTop)
                + " \nmRefreshImageView bottom:" + (top + height - bottom)
                + " \n目标 mTarget bottom:" + (top + height - bottom + mCurrentOffsetTop)
        );

        mTarget.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mCurrentOffsetTop);
        mRefreshImageView.layout(left, top, left + width - right, top + height - bottom);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

}
