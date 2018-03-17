package com.lewis_v.ycardlayoutlib;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * auth: lewis-v
 * time: 2018/3/16.
 */

public class YCardLayout extends FrameLayout {
    private static final String TAG = "YCardLayout";
    private int minLength;//滑动最小距离，小于这个认为未滑动
    private float downX = 0;//点击时的X
    private float downY = 0;//点击时的Y

    private float cacheX = -1;//上一次点击或移动的X
    private float cacheY = -1;//上一次点击或移动的Y
    private Point firstPoint = null;//原始位置
    private int maxWidth = 0;//最大宽度，默认为屏幕宽度
    private OnYCardMoveListener onYCardMoveListener;
    private volatile boolean isRemove = false;//是否已被删除
    private volatile boolean moveAble = true;//是否可以移动
    private volatile boolean isInit = false;//是否初始化完成
    private volatile boolean isRunAnim = false;//是否正在进行动画

    public YCardLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public YCardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public YCardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public YCardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context){
        setClickable(true);
        setEnabled(true);
        minLength = ViewConfiguration.get(context).getScaledTouchSlop();
        post(new Runnable() {
            @Override
            public void run() {
                maxWidth = getWidth();
                firstPoint = new Point(0,0);
                isInit = true;
            }
        });
    }

    /**
     * 重置数据
     */
    public void reset(){
        if (firstPoint != null) {
            setX(firstPoint.x);
            setY(firstPoint.y);
        }
        isRemove = false;
        moveAble = true;
        setRotation(0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = super.onInterceptTouchEvent(ev);
        if (!isInit || isRunAnim){
            return false;
        }
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                cacheX = ev.getRawX();
                cacheY = ev.getRawY();
                if (firstPoint == null){
                    firstPoint = new Point((int) getX(),(int) getY());
                }
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if ((Math.abs(downX-ev.getRawX()) > minLength || Math.abs(downY-ev.getRawY()) > minLength) && !isRemove && moveAble){
                    intercepted = true;
                }else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isRemove && moveAble && isInit && !isRunAnim) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.e(TAG, "down:"+event.getRawX()+";"+event.getRawY());
                    cacheX = event.getRawX();
                    cacheY = event.getRawY();
                    downX = event.getRawX();
                    downY = event.getRawY();
                    if (firstPoint == null) {
                        firstPoint = new Point((int) getX(), (int) getY());
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if ((Math.abs(downX-event.getRawX()) > minLength || Math.abs(downY-event.getRawY()) > minLength)) {
                        float moveX = event.getRawX();
                        float moveY = event.getRawY();

                        if (moveY > 0) {
                            setY(getY() + (moveY - cacheY));
                        }
                        if (moveX > 0) {
                            setX(getX() + (moveX - cacheX));
                            float moveLen = (moveX - downX) / maxWidth;
                            int moveProgress = (int) ((moveLen) * 100);
                            setRotation((moveLen) * 45f);
                            if (onYCardMoveListener != null) {
                                onYCardMoveListener.onMove(this, moveProgress);
                            }
                        }
                        cacheX = moveX;
                        cacheY = moveY;
                    }
                    return false;
                case MotionEvent.ACTION_UP:
                    if ((Math.abs(downX-event.getRawX()) > minLength || Math.abs(downY-event.getRawY()) > minLength)) {
                        int moveEndProgress = (int) (((event.getRawX() - downX) / maxWidth) * 100);
                        if (onYCardMoveListener != null) {
                            if (onYCardMoveListener.onMoveEnd(this, moveEndProgress)) {
                                return true;
                            }
                        }
                        animToReBack(this, firstPoint);
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    /**
     * 移动动画
     * @param view
     * @param point
     * @param rotation
     */
    public AnimatorSet getAnimToMove(View view, Point point, float rotation){
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view,"translationX",point.x);
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view,"translationY",point.y);
        ObjectAnimator objectAnimatorR = ObjectAnimator.ofFloat(view,"rotation",rotation);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorR,objectAnimatorX,objectAnimatorY);
        return animatorSet;
    }

    /**
     * 还原动画
     * @param view
     * @param point
     */
    public void animToReBack(View view,Point point){
        Log.e(TAG,"原："+view.getX()+view.getY()+"back:"+point.toString());
        AnimatorSet animatorSet = getAnimToMove(view,point,0);
        isRunAnim = true;
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isRunAnim = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isRunAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }


    /**
     *  向左移除控件
     * @param removeAnimListener
     */
    public void removeToLeft(RemoveAnimListener removeAnimListener){
        remove(true,removeAnimListener);
    }

    /**
     * 向右移除控件
     * @param removeAnimListener
     */
    public void removeToRight(RemoveAnimListener removeAnimListener){
        remove(false,removeAnimListener);
    }

    /**
     * 移除控件并notify
     * @param isLeft 是否是向左
     * @param removeAnimListener
     */
    public void remove(boolean isLeft, final RemoveAnimListener removeAnimListener){
        isRemove = true;
        final Point point = calculateEndPoint(this,this.firstPoint,isLeft);
        AnimatorSet animatorSet = getReMoveAnim(this,point);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (removeAnimListener != null){
                    removeAnimListener.OnAnimStart(YCardLayout.this);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (removeAnimListener != null){
                    removeAnimListener.OnAnimEnd(YCardLayout.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.e("cancel","");
                reset();
                if (removeAnimListener != null){
                    removeAnimListener.OnAnimCancel(YCardLayout.this);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    /**
     * 计算移除动画终点
     * @param view
     * @param point
     * @param isLeft
     * @return
     */
    public Point calculateEndPoint(View view, Point point, boolean isLeft){
        Point endPoint = new Point();
        if (isLeft) {
            endPoint.x = point.x - (int) (view.getWidth() * 1.5);
        }else {
            endPoint.x = point.x + (int) (view.getWidth() * 1.5);
        }
        if (((int)view.getX()) == point.x && ((int)view.getY()) == point.y){//还在原来位置
            endPoint.y = point.y + (int)(view.getHeight()*1.5);
        }else {
            int endY = getEndY(view,point);
            if (isLeft) {
                endPoint.y = (int) view.getY() - endY;
            }else {
                endPoint.y = (int)view.getY() + endY;
            }
        }
        return endPoint;
    }

    /**
     * 获取终点Y轴与初始位置Y轴的距离
     * @param view
     * @param point
     * @return
     */
    public int getEndY(View view,Point point){
        return (int) ((point.y-view.getY())/(point.x-view.getX())*1.5*view.getWidth());
    }

    /**
     * 获取移除动画
     * @param view
     * @param point
     * @return
     */
    public AnimatorSet getReMoveAnim(View view, Point point){
        AnimatorSet animatorSet = getAnimToMove(view,point,view.getRotation());
        animatorSet.setDuration(500);
        return animatorSet;
    }

    public interface OnYCardMoveListener{
        void onMove(YCardLayout view, int moveProgress);
        boolean onMoveEnd(YCardLayout view, int moveProgress);
    }

    public void setOnYCardMoveListener(OnYCardMoveListener onYCardMoveListener) {
        this.onYCardMoveListener = onYCardMoveListener;
    }

    public interface RemoveAnimListener{
        void OnAnimStart(FrameLayout view);
        void OnAnimCancel(FrameLayout view);
        void OnAnimEnd(FrameLayout view);
    }

    public YCardLayout setMoveAble(boolean moveAble) {
        this.moveAble = moveAble;
        return this;
    }
}
