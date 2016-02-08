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

import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * Android 19 以上的 Animator
 * 动画基础支持的超类
 */
public abstract class Animator10 implements Cloneable {

    // 缓存 AnimatorListener 集合
    ArrayList<AnimatorListener> mListeners = null;
    // 缓存 AnimatorPauseListener 集合
    ArrayList<AnimatorPauseListener> mPauseListeners = null;

    // 标记是否可以暂停
    boolean mPaused = false;

    /**
     * 获得延迟时间
     *
     * @return long
     */
    public abstract long getStartDelay();

    /**
     * 设置延迟时间
     *
     * @param startDelay startDelay
     */
    public abstract void setStartDelay(long startDelay);

    /**
     * 设置持续时间
     *
     * @param duration duration
     * @return Animator10
     */
    public abstract Animator10 setDuration(long duration);

    /**
     * 设置持续时间
     *
     * @return long
     */
    public abstract long getDuration();

    /**
     * 设置加速器
     *
     * @param value value
     */
    public abstract void setInterpolator(Interpolator value);

    /**
     * 判断动画是否正在运行
     *
     * @return boolean
     */
    public abstract boolean isRunning();

    /**
     * 开始动画
     */
    public void start() {

    }

    /**
     * 取消动画
     */
    public void cancel() {

    }

    /**
     * 结束动画
     */
    public void end() {

    }

    /**
     * 暂停动画
     */
    @SuppressWarnings("unchecked")
    public void pause() {
        if (isStarted() && !mPaused) {
            mPaused = true;
            if (mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> tmpListeners = (ArrayList<AnimatorPauseListener>) mPauseListeners.clone();
                int numListeners = tmpListeners.size();
                for (AnimatorPauseListener tmpListener : tmpListeners) {
                    tmpListener.onAnimationPause(this);
                }
            }
        }
    }

    /**
     * 恢复动画
     */
    @SuppressWarnings("unchecked")
    public void resume() {
        if (mPaused) {
            mPaused = false;
            if (mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> tmpListeners = (ArrayList<AnimatorPauseListener>) mPauseListeners.clone();
                int numListeners = tmpListeners.size();
                for (AnimatorPauseListener tmpListener : tmpListeners) {
                    tmpListener.onAnimationResume(this);
                }
            }
        }
    }

    /**
     * 是否暂停了
     *
     * @return boolean
     */
    public boolean isPaused() {
        return mPaused;
    }

    /**
     * 是否开始了
     *
     * @return boolean
     */
    public boolean isStarted() {
        return isRunning();
    }

    /**
     * 获得加速器
     * 默认为null
     *
     * @return Interpolator
     */
    public Interpolator getInterpolator() {
        return null;
    }

    /**
     * 添加 AnimatorListener
     *
     * @param listener listener
     */
    public void addListener(AnimatorListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<AnimatorListener>();
        }
        mListeners.add(listener);
    }

    /**
     * 移除 AnimatorListener
     *
     * @param listener listener
     */
    public void removeListener(AnimatorListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
        if (mListeners.size() == 0) {
            mListeners = null;
        }
    }

    /**
     * 获取全部 AnimatorListener
     *
     * @return ArrayList<AnimatorListener>
     */
    public ArrayList<AnimatorListener> getListeners() {
        return mListeners;
    }

    /**
     * 添加 AnimatorPauseListener
     *
     * @param listener listener
     */
    public void addPauseListener(AnimatorPauseListener listener) {
        if (mPauseListeners == null) {
            mPauseListeners = new ArrayList<AnimatorPauseListener>();
        }
        mPauseListeners.add(listener);
    }

    /**
     * 移除 AnimatorPauseListener
     *
     * @param listener listener
     */
    public void removePauseListener(AnimatorPauseListener listener) {
        if (mPauseListeners == null) {
            return;
        }
        mPauseListeners.remove(listener);
        if (mPauseListeners.size() == 0) {
            mPauseListeners = null;
        }
    }

    /**
     * 清空所有 Listener 缓存
     */
    public void removeAllListeners() {
        if (mListeners != null) {
            mListeners.clear();
            mListeners = null;
        }
        if (mPauseListeners != null) {
            mPauseListeners.clear();
            mPauseListeners = null;
        }
    }

    /**
     * Animator10 的克隆方法
     *
     * @return Animator10
     */
    @Override
    public Animator10 clone() {
        try {
            // 克隆一份 Animator10 作为副本Animator10
            final Animator10 anim = (Animator10) super.clone();
            if (mListeners != null) {
                // 保存一份 旧的 ArrayList<AnimatorListener>
                ArrayList<AnimatorListener> oldListeners = mListeners;
                // 副本Animator10 的 ArrayList<AnimatorListener>重新初始化
                anim.mListeners = new ArrayList<AnimatorListener>();
                int numListeners = oldListeners.size();
                /*
                 * 旧的 ArrayList<AnimatorListener> 给
                 * 副本Animator10 的 ArrayList<AnimatorListener>进行赋值
                 */
                for (AnimatorListener oldListener : oldListeners) {
                    anim.mListeners.add(oldListener);
                }
            }
            if (mPauseListeners != null) {
                // 保存一份 旧的 ArrayList<AnimatorPauseListener>
                ArrayList<AnimatorPauseListener> oldListeners = mPauseListeners;
                // 副本Animator10 的 ArrayList<AnimatorPauseListener>重新初始化
                anim.mPauseListeners = new ArrayList<AnimatorPauseListener>();
                int numListeners = oldListeners.size();
                /*
                 * 旧的 ArrayList<AnimatorPauseListener> 给
                 * 副本Animator10 的 ArrayList<AnimatorPauseListener>进行赋值
                 */
                for (AnimatorPauseListener oldListener : oldListeners) {
                    anim.mPauseListeners.add(oldListener);
                }
            }
            return anim;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * 设置开始值
     * 默认不实现
     */
    public void setupStartValues() {

    }

    /**
     * 设置结束值
     * 默认不实现
     */
    public void setupEndValues() {

    }

    /**
     * 设置 target
     * 默认不实现
     *
     * @param target target
     */
    public void setTarget(Object target) {

    }

    /**
     * 控制 动画的开始、结束、取消和重复
     */
    public interface AnimatorListener {
        void onAnimationStart(Animator10 animation);

        void onAnimationEnd(Animator10 animation);

        void onAnimationCancel(Animator10 animation);

        void onAnimationRepeat(Animator10 animation);
    }

    /**
     * 控制 动画的暂停和恢复
     */
    public interface AnimatorPauseListener {
        void onAnimationPause(Animator10 animation);

        void onAnimationResume(Animator10 animation);
    }
}
