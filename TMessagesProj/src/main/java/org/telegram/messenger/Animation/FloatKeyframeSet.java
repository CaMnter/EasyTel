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

import org.telegram.messenger.Animation.Keyframe.FloatKeyframe;

import java.util.ArrayList;

/**
 * Android 14 以上的 FloatKeyframeSet
 * 祯的类型是FloatKeyframe
 */
class FloatKeyframeSet extends KeyframeSet {
    // 第一祯的值
    private float firstValue;
    // 最后一祯的值
    private float lastValue;
    // δ值
    private float deltaValue;
    // 标记是否是第一次取值
    private boolean firstTime = true;

    /**
     * 构造方法
     * 需要传入一组 FloatKeyframe
     *
     * @param keyframes keyframes
     */
    public FloatKeyframeSet(FloatKeyframe... keyframes) {
        super(keyframes);
    }

    /**
     * 获取 FloatKeyframeSet 的值
     *
     * @param fraction fraction
     * @return Object
     */
    @Override
    public Object getValue(float fraction) {
        return getFloatValue(fraction);
    }

    /**
     * FloatKeyframeSet 克隆方法
     *
     * @return FloatKeyframeSet
     */
    @Override
    public FloatKeyframeSet clone() {
        ArrayList<Keyframe> keyframes = mKeyframes;
        // 记录 FloatKeyframe 数量
        int numKeyframes = mKeyframes.size();
        // 创建一个同等大小的 FloatKeyframe 数组
        FloatKeyframe[] newKeyframes = new FloatKeyframe[numKeyframes];
        // 为 数组 赋值
        for (int i = 0; i < numKeyframes; ++i) {
            newKeyframes[i] = (FloatKeyframe) keyframes.get(i).clone();
        }
        // 用 数组数据 重新 实例化一个 FloatKeyframeSet
        return new FloatKeyframeSet(newKeyframes);
    }

    /**
     * 获取 FloatKeyframeSet 的Float值
     *
     * @param fraction fraction
     * @return float
     */
    @SuppressWarnings("unchecked")
    public float getFloatValue(float fraction) {
        // 如果 只有 两祯
        if (mNumKeyframes == 2) {
            // 第一次进行取值操作
            if (firstTime) {
                // 重新标记不是第一次取值
                firstTime = false;
                // 拿到第一祯的值
                firstValue = ((FloatKeyframe) mKeyframes.get(0)).getFloatValue();
                // 拿到最后一祯 （ 这里由于是只有两祯 所以取第二祯的值 ）
                lastValue = ((FloatKeyframe) mKeyframes.get(1)).getFloatValue();
                // 计算 δ值
                deltaValue = lastValue - firstValue;
            }
            // 如果插入器不为 null
            if (mInterpolator != null) {
                // 计算插值
                fraction = mInterpolator.getInterpolation(fraction);
            }
            // 如果评估器 为 null
            if (mEvaluator == null) {
                return firstValue + fraction * deltaValue;
            } else {
                // 评估器 不为 null
                // 计算评估值
                return ((Number) mEvaluator.evaluate(fraction, firstValue, lastValue)).floatValue();
            }
        }
        if (fraction <= 0f) {
            // 拿到 第一祯
            final FloatKeyframe prevKeyframe = (FloatKeyframe) mKeyframes.get(0);
            // 拿到 第二祯
            final FloatKeyframe nextKeyframe = (FloatKeyframe) mKeyframes.get(1);
            // 拿到 第一祯的值
            float prevValue = prevKeyframe.getFloatValue();
            // 拿到 第二祯的值
            float nextValue = nextKeyframe.getFloatValue();
            // 拿到 第一祯的分数
            float prevFraction = prevKeyframe.getFraction();
            // 拿到 第二祯的分数
            float nextFraction = nextKeyframe.getFraction();
            // 拿到 第二祯的插入器
            final Interpolator interpolator = nextKeyframe.getInterpolator();
            // 如果 第二祯的插入器存在
            if (interpolator != null) {
                // 计算第二祯的插值
                fraction = interpolator.getInterpolation(fraction);
            }
            // （ 第二祯的插值 - 第一祯的分数 ） / （ 第二祯的分数 - 第一祯的分数 ）
            float intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            // 计算评估值
            return mEvaluator == null ? prevValue + intervalFraction * (nextValue - prevValue) : ((Number) mEvaluator.evaluate(intervalFraction, prevValue, nextValue)).floatValue();
        } else if (fraction >= 1f) {
            // 拿到 倒数第二祯
            final FloatKeyframe prevKeyframe = (FloatKeyframe) mKeyframes.get(mNumKeyframes - 2);
            // 拿到 倒数第一祯
            final FloatKeyframe nextKeyframe = (FloatKeyframe) mKeyframes.get(mNumKeyframes - 1);
            // 拿到 倒数第二祯的值
            float prevValue = prevKeyframe.getFloatValue();
            // 拿到 倒数第一祯的值
            float nextValue = nextKeyframe.getFloatValue();
            // 拿到 倒数第二祯的分数
            float prevFraction = prevKeyframe.getFraction();
            // 拿到 倒数第一祯的分数
            float nextFraction = nextKeyframe.getFraction();
            // 拿到 倒数第一祯的插入器
            final Interpolator interpolator = nextKeyframe.getInterpolator();
            // 如果 倒数第一祯的插入器存在
            if (interpolator != null) {
                // 计算 倒数第一祯的插值
                fraction = interpolator.getInterpolation(fraction);
            }
            // ( 倒数第一祯插值 - 倒数第二祯的分数 ) / ( 倒数第一祯的分数 - 倒数第二祯的分数 )
            float intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            // 计算评估值
            return mEvaluator == null ? prevValue + intervalFraction * (nextValue - prevValue) : ((Number) mEvaluator.evaluate(intervalFraction, prevValue, nextValue)).floatValue();
        }

        /************************************
         * fraction ＝ 0.0f - 1.0f 之间的情况 *
         ************************************/

        // 记录上一祯
        // 开始的话先拿到第一祯
        FloatKeyframe prevKeyframe = (FloatKeyframe) mKeyframes.get(0);
        // 遍历 第一祯以后的 所有祯
        for (int i = 1; i < mNumKeyframes; ++i) {
            // 拿到每一祯
            FloatKeyframe nextKeyframe = (FloatKeyframe) mKeyframes.get(i);
            // 如果传入方法的分数 < 该祯的分数
            if (fraction < nextKeyframe.getFraction()) {
                // 拿到 当前祯的插入器
                final Interpolator interpolator = nextKeyframe.getInterpolator();
                if (interpolator != null) {
                    // 记录当前祯的插值
                    fraction = interpolator.getInterpolation(fraction);
                }
                // 当前祯的插值 - 上一祯的分数 / 当前祯的分数 - 上一祯的分数
                float intervalFraction = (fraction - prevKeyframe.getFraction()) /
                        (nextKeyframe.getFraction() - prevKeyframe.getFraction());
                // 拿到 上一祯的值
                float prevValue = prevKeyframe.getFloatValue();
                // 拿到 当前祯的值
                float nextValue = nextKeyframe.getFloatValue();
                // 计算评估值
                return mEvaluator == null ? prevValue + intervalFraction * (nextValue - prevValue) : ((Number) mEvaluator.evaluate(intervalFraction, prevValue, nextValue)).floatValue();
            }
            // 保存该祯为上一祯记录
            // 以供下次循环
            prevKeyframe = nextKeyframe;
        }
        // 返回最后一祯 的值
        return ((Number) mKeyframes.get(mNumKeyframes - 1).getValue()).floatValue();
    }
}
