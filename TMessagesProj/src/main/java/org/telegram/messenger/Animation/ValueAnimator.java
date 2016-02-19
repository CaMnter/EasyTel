/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.telegram.messenger.Animation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Android 19 以上的 ValueAnimator
 */
public class ValueAnimator extends Animator10 {

    // 动画持续时间 幅度比例
    private static float sDurationScale = 1.0f;

    /*
     * mPlayingState 的 三种状态
     * STOPPED = 还没用播放
     * RUNNING = 正常播放
     * SEEKED = 跳跃到一定值后继续播放
     */
    static final int STOPPED = 0;
    static final int RUNNING = 1;
    static final int SEEKED = 2;

    // 动画的第一次animateFrame()方法被调用
    // 这个时间是用来确定运行时间(因此经过分数)在随后的调用animateFrame()
    long mStartTime;

    // 当setCurrentPlayTime()被调用时就被设置上
    long mSeekTime = -1;

    /*
     * API 19 新增属性
     * 记录 下一祯 后 暂停时间
     * 用于计算 开始时间 或者 delayStartTime 时间
     */
    private long mPauseTime;

    // 记录 动画 是否需要 恢复
    private boolean mResumed = false;

    // 所有动画都基于 静态的sAnimationHandler进程内部定时循环
    protected static ThreadLocal<AnimationHandler> sAnimationHandler = new ThreadLocal<AnimationHandler>();

    // 如果没有设置动画
    // 则使用sDefaultInterpolator
    private static final Interpolator sDefaultInterpolator = new AccelerateDecelerateInterpolator();

    // 用于指示动画当前是否反向播放
    // 会导致运行分数是反向计算适当的值
    private boolean mPlayingBackwards = false;

    // 用于跟踪迭代标记
    // 当mCurrentIteration超过repeatCount(如果repeatCount ! =无限),动画结束
    private int mCurrentIteration = 0;

    // 在 getAnimatedFraction 时 被调用
    private float mCurrentFraction = 0f;

    // 标记动画开始是否有 延迟
    private boolean mStartedDelay = false;

    // 记录动画开始的 延迟时间
    private long mDelayStartTime;

    // mPlayingState 记录当前播放状态
    // 默认状态是STOPPED
    int mPlayingState = STOPPED;

    // 标记 是否正在运行
    private boolean mRunning = false;

    // 标记 是否已经开始
    private boolean mStarted = false;

    // 标记 动画开始回调是否被调用
    private boolean mStartListenersCalled = false;

    // 标记 是否初始化
    boolean mInitialized = false;

    // 记录 动画 持续时间
    private long mDuration = (long) (300 * sDurationScale);

    // 记录 不按比例 持续时间
    private long mUnscaledDuration = 300;

    // 记录 动画开始 延迟时间
    private long mStartDelay = 0;

    // 记录 不按比例 延迟时间
    private long mUnscaledStartDelay = 0;

    // 记录 动画重复 次数
    private int mRepeatCount = 0;

    // 记录 动画重复 模式
    private int mRepeatMode = RESTART;

    // 动画的运行部分将通过插入器内插计算分数
    // 然后用于计算动画值
    private Interpolator mInterpolator = sDefaultInterpolator;

    // 通过动画的生命周期 发送事件 到对应的 AnimatorUpdateListener
    private ArrayList<AnimatorUpdateListener> mUpdateListeners = null;

    // PropertyValuesHolder 数组
    PropertyValuesHolder[] mValues;

    // 在 getAnimatedValue(String) 方法里被调用
    // 用于查找对应的PropertyValuesHolder
    HashMap<String, PropertyValuesHolder> mValuesMap;

    // 重复模式 重新开始
    public static final int RESTART = 1;

    // 重复模式 反向播放
    public static final int REVERSE = 2;

    // 重复模式 无限循环
    public static final int INFINITE = -1;

    /**
     * 设置 动画持续时间 幅度比例
     *
     * @param durationScale durationScale
     */
    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    /**
     * 获取 动画持续时间 幅度比例
     *
     * @return float
     */
    public static float getDurationScale() {
        return sDurationScale;
    }

    public ValueAnimator() {

    }

    /**
     * @param values values
     * @return ValueAnimator
     */
    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        return anim;
    }

    /**
     * @param values values
     * @return ValueAnimator
     */
    public static ValueAnimator ofFloat(float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }

    public static ValueAnimator ofPropertyValuesHolder(PropertyValuesHolder... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setValues(values);
        return anim;
    }

    public static ValueAnimator ofObject(TypeEvaluator evaluator, Object... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }


    public void setIntValues(int... values) {
        if (values == null || values.length == 0) {
            return;
        }
        if (mValues == null || mValues.length == 0) {
            setValues(PropertyValuesHolder.ofInt("", values));
        } else {
            PropertyValuesHolder valuesHolder = mValues[0];
            valuesHolder.setIntValues(values);
        }
        mInitialized = false;
    }

    public void setFloatValues(float... values) {
        if (values == null || values.length == 0) {
            return;
        }
        if (mValues == null || mValues.length == 0) {
            setValues(PropertyValuesHolder.ofFloat("", values));
        } else {
            PropertyValuesHolder valuesHolder = mValues[0];
            valuesHolder.setFloatValues(values);
        }
        mInitialized = false;
    }

    public void setObjectValues(Object... values) {
        if (values == null || values.length == 0) {
            return;
        }
        if (mValues == null || mValues.length == 0) {
            setValues(PropertyValuesHolder.ofObject("", null, values));
        } else {
            PropertyValuesHolder valuesHolder = mValues[0];
            valuesHolder.setObjectValues(values);
        }
        mInitialized = false;
    }

    public void setValues(PropertyValuesHolder... values) {
        int numValues = values.length;
        mValues = values;
        mValuesMap = new HashMap<String, PropertyValuesHolder>(numValues);
        for (PropertyValuesHolder valuesHolder : values) {
            mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder);
        }
        mInitialized = false;
    }

    public PropertyValuesHolder[] getValues() {
        return mValues;
    }

    void initAnimation() {
        if (!mInitialized) {
            int numValues = mValues.length;
            for (PropertyValuesHolder mValue : mValues) {
                mValue.init();
            }
            mInitialized = true;
        }
    }

    public ValueAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
        }
        mUnscaledDuration = duration;
        mDuration = (long) (duration * sDurationScale);
        return this;
    }

    public long getDuration() {
        return mUnscaledDuration;
    }

    public void setCurrentPlayTime(long playTime) {
        initAnimation();
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (mPlayingState != RUNNING) {
            mSeekTime = playTime;
            mPlayingState = SEEKED;
        }
        mStartTime = currentTime - playTime;
        doAnimationFrame(currentTime);
    }

    public long getCurrentPlayTime() {
        if (!mInitialized || mPlayingState == STOPPED) {
            return 0;
        }
        return AnimationUtils.currentAnimationTimeMillis() - mStartTime;
    }

    @SuppressWarnings("unchecked")
    protected static class AnimationHandler implements Runnable {

        protected final ArrayList<ValueAnimator> mAnimations = new ArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mTmpAnimations = new ArrayList<ValueAnimator>();
        protected final ArrayList<ValueAnimator> mPendingAnimations = new ArrayList<ValueAnimator>();
        protected final ArrayList<ValueAnimator> mDelayedAnims = new ArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mEndingAnims = new ArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mReadyAnims = new ArrayList<ValueAnimator>();

        private boolean mAnimationScheduled;

        public void start() {
            scheduleAnimation();
        }

        private void doAnimationFrame(long frameTime) {
            while (mPendingAnimations.size() > 0) {
                ArrayList<ValueAnimator> pendingCopy = (ArrayList<ValueAnimator>) mPendingAnimations.clone();
                mPendingAnimations.clear();
                int count = pendingCopy.size();
                for (ValueAnimator anim : pendingCopy) {
                    if (anim.mStartDelay == 0) {
                        anim.startAnimation(this);
                    } else {
                        mDelayedAnims.add(anim);
                    }
                }
            }

            int numDelayedAnims = mDelayedAnims.size();
            for (ValueAnimator anim : mDelayedAnims) {
                if (anim.delayedAnimationFrame(frameTime)) {
                    mReadyAnims.add(anim);
                }
            }
            int numReadyAnims = mReadyAnims.size();
            if (numReadyAnims > 0) {
                for (ValueAnimator anim : mReadyAnims) {
                    anim.startAnimation(this);
                    anim.mRunning = true;
                    mDelayedAnims.remove(anim);
                }
                mReadyAnims.clear();
            }

            int numAnims = mAnimations.size();
            for (ValueAnimator mAnimation : mAnimations) {
                mTmpAnimations.add(mAnimation);
            }
            for (int i = 0; i < numAnims; ++i) {
                ValueAnimator anim = mTmpAnimations.get(i);
                if (mAnimations.contains(anim) && anim.doAnimationFrame(frameTime)) {
                    mEndingAnims.add(anim);
                }
            }
            mTmpAnimations.clear();
            if (mEndingAnims.size() > 0) {
                for (ValueAnimator mEndingAnim : mEndingAnims) {
                    mEndingAnim.endAnimation(this);
                }
                mEndingAnims.clear();
            }

            if (!mAnimations.isEmpty() || !mDelayedAnims.isEmpty()) {
                scheduleAnimation();
            }
        }

        @Override
        public void run() {
            mAnimationScheduled = false;
            doAnimationFrame(System.nanoTime() / 1000000);
        }

        private void scheduleAnimation() {
            if (!mAnimationScheduled) {
                AndroidUtilities.runOnUIThread(this);
                mAnimationScheduled = true;
            }
        }
    }

    public long getStartDelay() {
        return mUnscaledStartDelay;
    }

    public void setStartDelay(long startDelay) {
        this.mStartDelay = (long) (startDelay * sDurationScale);
        mUnscaledStartDelay = startDelay;
    }

    public Object getAnimatedValue() {
        if (mValues != null && mValues.length > 0) {
            return mValues[0].getAnimatedValue();
        }
        return null;
    }

    public Object getAnimatedValue(String propertyName) {
        PropertyValuesHolder valuesHolder = mValuesMap.get(propertyName);
        if (valuesHolder != null) {
            return valuesHolder.getAnimatedValue();
        } else {
            return null;
        }
    }

    public void setRepeatCount(int value) {
        mRepeatCount = value;
    }

    public int getRepeatCount() {
        return mRepeatCount;
    }

    public void setRepeatMode(int value) {
        mRepeatMode = value;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (mUpdateListeners == null) {
            mUpdateListeners = new ArrayList<AnimatorUpdateListener>();
        }
        mUpdateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        if (mUpdateListeners == null) {
            return;
        }
        mUpdateListeners.clear();
        mUpdateListeners = null;
    }

    public void removeUpdateListener(AnimatorUpdateListener listener) {
        if (mUpdateListeners == null) {
            return;
        }
        mUpdateListeners.remove(listener);
        if (mUpdateListeners.size() == 0) {
            mUpdateListeners = null;
        }
    }

    @Override
    public void setInterpolator(Interpolator value) {
        if (value != null) {
            mInterpolator = value;
        } else {
            mInterpolator = new LinearInterpolator();
        }
    }

    @Override
    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public void setEvaluator(TypeEvaluator value) {
        if (value != null && mValues != null && mValues.length > 0) {
            mValues[0].setEvaluator(value);
        }
    }

    @SuppressWarnings("unchecked")
    private void notifyStartListeners() {
        if (mListeners != null && !mStartListenersCalled) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList<AnimatorListener>) mListeners.clone();
            int numListeners = tmpListeners.size();
            for (AnimatorListener tmpListener : tmpListeners) {
                tmpListener.onAnimationStart(this);
            }
        }
        mStartListenersCalled = true;
    }

    private void start(boolean playBackwards) {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        mPlayingBackwards = playBackwards;
        mCurrentIteration = 0;
        mPlayingState = STOPPED;
        mStarted = true;
        mStartedDelay = false;
        mPaused = false;
        AnimationHandler animationHandler = getOrCreateAnimationHandler();
        animationHandler.mPendingAnimations.add(this);
        if (mStartDelay == 0) {
            setCurrentPlayTime(0);
            mPlayingState = STOPPED;
            mRunning = true;
            notifyStartListeners();
        }
        animationHandler.start();
    }

    @Override
    public void start() {
        start(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cancel() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (mPlayingState != STOPPED || handler.mPendingAnimations.contains(this) || handler.mDelayedAnims.contains(this)) {
            if ((mStarted || mRunning) && mListeners != null) {
                if (!mRunning) {
                    notifyStartListeners();
                }
                ArrayList<AnimatorListener> tmpListeners = (ArrayList<AnimatorListener>) mListeners.clone();
                for (AnimatorListener listener : tmpListeners) {
                    listener.onAnimationCancel(this);
                }
            }
            endAnimation(handler);
        }
    }

    @Override
    public void end() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (!handler.mAnimations.contains(this) && !handler.mPendingAnimations.contains(this)) {
            mStartedDelay = false;
            startAnimation(handler);
            mStarted = true;
        } else if (!mInitialized) {
            initAnimation();
        }
        animateValue(mPlayingBackwards ? 0f : 1f);
        endAnimation(handler);
    }

    @Override
    public void resume() {
        if (mPaused) {
            mResumed = true;
        }
        super.resume();
    }

    @Override
    public void pause() {
        boolean previouslyPaused = mPaused;
        super.pause();
        if (!previouslyPaused && mPaused) {
            mPauseTime = -1;
            mResumed = false;
        }
    }

    @Override
    public boolean isRunning() {
        return (mPlayingState == RUNNING || mRunning);
    }

    @Override
    public boolean isStarted() {
        return mStarted;
    }

    public void reverse() {
        mPlayingBackwards = !mPlayingBackwards;
        if (mPlayingState == RUNNING) {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            long currentPlayTime = currentTime - mStartTime;
            long timeLeft = mDuration - currentPlayTime;
            mStartTime = currentTime - timeLeft;
        } else if (mStarted) {
            end();
        } else {
            start(true);
        }
    }

    @SuppressWarnings("unchecked")
    private void endAnimation(AnimationHandler handler) {
        handler.mAnimations.remove(this);
        handler.mPendingAnimations.remove(this);
        handler.mDelayedAnims.remove(this);
        mPlayingState = STOPPED;
        mPaused = false;
        if ((mStarted || mRunning) && mListeners != null) {
            if (!mRunning) {
                notifyStartListeners();
            }
            ArrayList<AnimatorListener> tmpListeners = (ArrayList<AnimatorListener>) mListeners.clone();
            int numListeners = tmpListeners.size();
            for (AnimatorListener tmpListener : tmpListeners) {
                tmpListener.onAnimationEnd(this);
            }
        }
        mRunning = false;
        mStarted = false;
        mStartListenersCalled = false;
        mPlayingBackwards = false;
    }

    private void startAnimation(AnimationHandler handler) {
        initAnimation();
        handler.mAnimations.add(this);
        if (mStartDelay > 0 && mListeners != null) {
            notifyStartListeners();
        }
    }

    private boolean delayedAnimationFrame(long currentTime) {
        if (!mStartedDelay) {
            mStartedDelay = true;
            mDelayStartTime = currentTime;
        } else {
            if (mPaused) {
                if (mPauseTime < 0) {
                    mPauseTime = currentTime;
                }
                return false;
            } else if (mResumed) {
                mResumed = false;
                if (mPauseTime > 0) {
                    mDelayStartTime += (currentTime - mPauseTime);
                }
            }
            long deltaTime = currentTime - mDelayStartTime;
            if (deltaTime > mStartDelay) {
                mStartTime = currentTime - (deltaTime - mStartDelay);
                mPlayingState = RUNNING;
                return true;
            }
        }
        return false;
    }

    boolean animationFrame(long currentTime) {
        boolean done = false;
        switch (mPlayingState) {
            case RUNNING:
            case SEEKED:
                float fraction = mDuration > 0 ? (float) (currentTime - mStartTime) / mDuration : 1f;
                if (fraction >= 1f) {
                    if (mCurrentIteration < mRepeatCount || mRepeatCount == INFINITE) {
                        if (mListeners != null) {
                            int numListeners = mListeners.size();
                            for (AnimatorListener mListener : mListeners) {
                                mListener.onAnimationRepeat(this);
                            }
                        }
                        if (mRepeatMode == REVERSE) {
                            mPlayingBackwards = !mPlayingBackwards;
                        }
                        mCurrentIteration += (int) fraction;
                        fraction = fraction % 1f;
                        mStartTime += mDuration;
                    } else {
                        done = true;
                        fraction = Math.min(fraction, 1.0f);
                    }
                }
                if (mPlayingBackwards) {
                    fraction = 1f - fraction;
                }
                animateValue(fraction);
                break;
        }

        return done;
    }

    final boolean doAnimationFrame(long frameTime) {
        if (mPlayingState == STOPPED) {
            mPlayingState = RUNNING;
            if (mSeekTime < 0) {
                mStartTime = frameTime;
            } else {
                mStartTime = frameTime - mSeekTime;
                mSeekTime = -1;
            }
        }
        if (mPaused) {
            if (mPauseTime < 0) {
                mPauseTime = frameTime;
            }
            return false;
        } else if (mResumed) {
            mResumed = false;
            if (mPauseTime > 0) {
                mStartTime += (frameTime - mPauseTime);
            }
        }
        final long currentTime = Math.max(frameTime, mStartTime);
        return animationFrame(currentTime);
    }

    public float getAnimatedFraction() {
        return mCurrentFraction;
    }

    void animateValue(float fraction) {
        fraction = mInterpolator.getInterpolation(fraction);
        mCurrentFraction = fraction;
        int numValues = mValues.length;
        for (PropertyValuesHolder mValue : mValues) {
            mValue.calculateValue(fraction);
        }
        if (mUpdateListeners != null) {
            int numListeners = mUpdateListeners.size();
            for (AnimatorUpdateListener mUpdateListener : mUpdateListeners) {
                mUpdateListener.onAnimationUpdate(this);
            }
        }
    }

    @Override
    public ValueAnimator clone() {
        final ValueAnimator anim = (ValueAnimator) super.clone();
        if (mUpdateListeners != null) {
            ArrayList<AnimatorUpdateListener> oldListeners = mUpdateListeners;
            anim.mUpdateListeners = new ArrayList<AnimatorUpdateListener>();
            int numListeners = oldListeners.size();
            for (AnimatorUpdateListener oldListener : oldListeners) {
                anim.mUpdateListeners.add(oldListener);
            }
        }
        anim.mSeekTime = -1;
        anim.mPlayingBackwards = false;
        anim.mCurrentIteration = 0;
        anim.mInitialized = false;
        anim.mPlayingState = STOPPED;
        anim.mStartedDelay = false;
        PropertyValuesHolder[] oldValues = mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap<String, PropertyValuesHolder>(numValues);
            for (int i = 0; i < numValues; ++i) {
                PropertyValuesHolder newValuesHolder = oldValues[i].clone();
                anim.mValues[i] = newValuesHolder;
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder);
            }
        }
        return anim;
    }

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator animation);
    }

    public static int getCurrentAnimationsCount() {
        AnimationHandler handler = sAnimationHandler.get();
        return handler != null ? handler.mAnimations.size() : 0;
    }

    public static void clearAllAnimations() {
        AnimationHandler handler = sAnimationHandler.get();
        if (handler != null) {
            handler.mAnimations.clear();
            handler.mPendingAnimations.clear();
            handler.mDelayedAnims.clear();
        }
    }

    private static AnimationHandler getOrCreateAnimationHandler() {
        AnimationHandler handler = sAnimationHandler.get();
        if (handler == null) {
            handler = new AnimationHandler();
            sAnimationHandler.set(handler);
        }
        return handler;
    }
}
