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

/**
 * Android 14 以上的 Keyframe
 * 这个类持有时间/值对一个动画,Keyframe被ValueAnimator类用来定义动画目标将要超过动画的值
 * 随着时间的进行从一个Keyframe,目标对象的值将在前一个Keyframe动画之间的价值和价值下一个Keyframe
 * Keyframe还可以定义 Interpolator 插值
 */
public abstract class Keyframe implements Cloneable {

    // 分数 mValue使用的时间
    float mFraction;

    // Keyframe的类型
    Class mValueType;

    // 设置插入器,默认是线性插入器
    private Interpolator mInterpolator = null;

    // 标志 Keyframe 是否是一个有效值
    boolean mHasValue = false;

    /**
     * @param fraction 表示为一个值在0和1之间,代表整个动画的时间分数的持续时间
     * @param value    该对象将动画来作为动画时间接近的时间在此关键帧的值，并且从作为时间的推移在这个关键帧中的时间的动画的值
     * @return IntKeyframe
     */
    public static Keyframe ofInt(float fraction, int value) {
        return new IntKeyframe(fraction, value);
    }

    /**
     * @param fraction 表示为一个值在0和1之间,代表整个动画的时间分数的持续时间
     * @return IntKeyframe
     */
    public static Keyframe ofInt(float fraction) {
        return new IntKeyframe(fraction);
    }

    /**
     * @param fraction 表示为一个值在0和1之间,代表整个动画的时间分数的持续时间
     * @param value    该对象将动画来作为动画时间接近的时间在此关键帧的值，并且从作为时间的推移在这个关键帧中的时间的动画的值
     * @return FloatKeyframe
     */
    public static Keyframe ofFloat(float fraction, float value) {
        return new FloatKeyframe(fraction, value);
    }

    /**
     * @param fraction 表示为一个值在0和1之间,代表整个动画的时间分数的持续时间
     * @return FloatKeyframe
     */
    public static Keyframe ofFloat(float fraction) {
        return new FloatKeyframe(fraction);
    }

    /**
     * @param fraction 表示为一个值在0和1之间,代表整个动画的时间分数的持续时间
     * @param value    该对象将动画来作为动画时间接近的时间在此关键帧的值，并且从作为时间的推移在这个关键帧中的时间的动画的值
     * @return ObjectKeyframe
     */
    public static Keyframe ofObject(float fraction, Object value) {
        return new ObjectKeyframe(fraction, value);
    }

    /**
     * @param fraction 表示为一个值在0和1之间,代表整个动画的时间分数的持续时间
     * @return ObjectKeyframe
     */
    public static Keyframe ofObject(float fraction) {
        return new ObjectKeyframe(fraction, null);
    }

    /**
     * 返回 有效值 标记
     *
     * @return boolean
     */
    public boolean hasValue() {
        return mHasValue;
    }

    /**
     * 抽象方法
     * 让子类获取Value的值
     *
     * @return Object
     */
    public abstract Object getValue();

    /**
     * 抽象方法
     * 让子类去设置Value的值
     *
     * @param value Object
     */
    public abstract void setValue(Object value);

    /**
     * 设置动画的时间分数的持续时间
     *
     * @return float
     */
    public float getFraction() {
        return mFraction;
    }

    /**
     * 设置动画的时间分数的持续时间
     *
     * @param fraction fraction
     */
    public void setFraction(float fraction) {
        mFraction = fraction;
    }

    /**
     * 获取该 Keyframe 的插入器
     *
     * @return
     */
    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    /**
     * 设置该 Keyframe 的插入器
     *
     * @param interpolator interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * 获取 Keyframe 的类型
     *
     * @return Class
     */
    public Class getType() {
        return mValueType;
    }

    /**
     * 抽象方法
     * 让子类去实现 对应子类的 克隆方法
     *
     * @return Keyframe
     */
    @Override
    public abstract Keyframe clone();

    /**
     * Keyframe的一个内部实现类
     * 用于除了 int 或者 float 之外的情景
     */
    static class ObjectKeyframe extends Keyframe {

        Object mValue;

        ObjectKeyframe(float fraction, Object value) {
            mFraction = fraction;
            mValue = value;
            mHasValue = (value != null);
            mValueType = mHasValue ? value.getClass() : Object.class;
        }

        public Object getValue() {
            return mValue;
        }

        public void setValue(Object value) {
            mValue = value;
            mHasValue = (value != null);
        }

        @Override
        public ObjectKeyframe clone() {
            ObjectKeyframe kfClone = new ObjectKeyframe(getFraction(), mHasValue ? mValue : null);
            kfClone.setInterpolator(getInterpolator());
            return kfClone;
        }
    }

    /**
     * Keyframe的一个内部实现类
     * 用于 int
     */
    static class IntKeyframe extends Keyframe {

        int mValue;

        IntKeyframe(float fraction, int value) {
            mFraction = fraction;
            mValue = value;
            mValueType = int.class;
            mHasValue = true;
        }

        IntKeyframe(float fraction) {
            mFraction = fraction;
            mValueType = int.class;
        }

        public int getIntValue() {
            return mValue;
        }

        public Object getValue() {
            return mValue;
        }

        public void setValue(Object value) {
            if (value != null && value.getClass() == Integer.class) {
                mValue = (Integer) value;
                mHasValue = true;
            }
        }

        @Override
        public IntKeyframe clone() {
            IntKeyframe kfClone = mHasValue ? new IntKeyframe(getFraction(), mValue) : new IntKeyframe(getFraction());
            kfClone.setInterpolator(getInterpolator());
            return kfClone;
        }
    }

    /**
     * Keyframe的一个内部实现类
     * 用于 float
     */
    static class FloatKeyframe extends Keyframe {

        float mValue;

        FloatKeyframe(float fraction, float value) {
            mFraction = fraction;
            mValue = value;
            mValueType = float.class;
            mHasValue = true;
        }

        FloatKeyframe(float fraction) {
            mFraction = fraction;
            mValueType = float.class;
        }

        public float getFloatValue() {
            return mValue;
        }

        public Object getValue() {
            return mValue;
        }

        public void setValue(Object value) {
            if (value != null && value.getClass() == Float.class) {
                mValue = (Float) value;
                mHasValue = true;
            }
        }

        @Override
        public FloatKeyframe clone() {
            FloatKeyframe kfClone = mHasValue ? new FloatKeyframe(getFraction(), mValue) : new FloatKeyframe(getFraction());
            kfClone.setInterpolator(getInterpolator());
            return kfClone;
        }
    }
}
