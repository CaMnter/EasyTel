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

import org.telegram.messenger.Animation.Keyframe.IntKeyframe;

import java.util.ArrayList;

/**
 * Android 14 以上的 IntKeyframeSet
 * 祯的类型是IntKeyframe
 */
class IntKeyframeSet extends KeyframeSet {
    // 第一祯的值
    private int firstValue;
    // 最后一祯的值
    private int lastValue;
    // δ值
    private int deltaValue;
    // 标记是否是第一次取值
    private boolean firstTime = true;

    /**
     * 构造方法
     * 需要传入一组 IntKeyframe
     *
     * @param keyframes keyframes
     */
    public IntKeyframeSet(IntKeyframe... keyframes) {
        super(keyframes);
    }

    /**
     * 获取 IntKeyframeSet 的值
     *
     * @param fraction fraction
     * @return Object
     */
    @Override
    public Object getValue(float fraction) {
        return getIntValue(fraction);
    }

    /**
     * IntKeyframeSet 克隆方法
     *
     * @return IntKeyframeSet
     */
    @Override
    public IntKeyframeSet clone() {
        ArrayList<Keyframe> keyframes = mKeyframes;
        // 记录 IntKeyframe 数量
        int numKeyframes = mKeyframes.size();
        // 创建一个同等大小的 IntKeyframe 数组
        IntKeyframe[] newKeyframes = new IntKeyframe[numKeyframes];
        // 为 数组 赋值
        for (int i = 0; i < numKeyframes; ++i) {
            newKeyframes[i] = (IntKeyframe) keyframes.get(i).clone();
        }
        // 用 数组数据 重新 实例化一个 IntKeyframeSet
        return new IntKeyframeSet(newKeyframes);
    }

    /**
     * 获取 IntKeyframeSet 的Float值
     *
     * @param fraction fraction
     * @return int
     */
    @SuppressWarnings("unchecked")
    public int getIntValue(float fraction) {
        // 如果 只有 两祯
        if (mNumKeyframes == 2) {
            // 第一次进行取值操作
            if (firstTime) {
                // 重新标记不是第一次取值
                firstTime = false;
                // 拿到第一祯的值
                firstValue = ((IntKeyframe) mKeyframes.get(0)).getIntValue();
                // 拿到最后一祯 （ 这里由于是只有两祯 所以取第二祯的值 ）
                lastValue = ((IntKeyframe) mKeyframes.get(1)).getIntValue();
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
                return firstValue + (int) (fraction * deltaValue);
            } else {
                // 评估器 不为 null
                // 计算评估值
                return ((Number) mEvaluator.evaluate(fraction, firstValue, lastValue)).intValue();
            }
        }
        if (fraction <= 0f) {
            // 拿到 第一祯
            final IntKeyframe prevKeyframe = (IntKeyframe) mKeyframes.get(0);
            // 拿到 第二祯
            final IntKeyframe nextKeyframe = (IntKeyframe) mKeyframes.get(1);
            // 拿到 第一祯的值
            int prevValue = prevKeyframe.getIntValue();
            // 拿到 第二祯的值
            int nextValue = nextKeyframe.getIntValue();
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
            return mEvaluator == null ? prevValue + (int) (intervalFraction * (nextValue - prevValue)) : ((Number) mEvaluator.evaluate(intervalFraction, prevValue, nextValue)).intValue();
        } else if (fraction >= 1f) {
            // 拿到 倒数第二祯
            final IntKeyframe prevKeyframe = (IntKeyframe) mKeyframes.get(mNumKeyframes - 2);
            // 拿到 倒数第一祯
            final IntKeyframe nextKeyframe = (IntKeyframe) mKeyframes.get(mNumKeyframes - 1);
            // 拿到 倒数第二祯的值
            int prevValue = prevKeyframe.getIntValue();
            // 拿到 倒数第一祯的值
            int nextValue = nextKeyframe.getIntValue();
            // 拿到 倒数第二祯的分数
            float prevFraction = prevKeyframe.getFraction();
            // 拿到 倒数第一祯的分数
            float nextFraction = nextKeyframe.getFraction();
            // 拿到 倒数第一祯的插入器
            final Interpolator interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                // 计算 倒数第一祯的插值
                fraction = interpolator.getInterpolation(fraction);
            }
            // ( 倒数第一祯插值 - 倒数第二祯的分数 ) / ( 倒数第一祯的分数 - 倒数第二祯的分数 )
            float intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            // 计算评估值
            return mEvaluator == null ? prevValue + (int) (intervalFraction * (nextValue - prevValue)) : ((Number) mEvaluator.evaluate(intervalFraction, prevValue, nextValue)).intValue();
        }

        /************************************
         * fraction ＝ 0.0f - 1.0f 之间的情况 *
         ************************************/

        // 记录上一祯
        // 开始的话先拿到第一祯
        IntKeyframe prevKeyframe = (IntKeyframe) mKeyframes.get(0);
        // 遍历 第一祯以后的 所有祯
        for (int i = 1; i < mNumKeyframes; ++i) {
            // 拿到每一祯
            IntKeyframe nextKeyframe = (IntKeyframe) mKeyframes.get(i);
            // 如果传入方法的分数 < 该祯的分数
            if (fraction < nextKeyframe.getFraction()) {
                // 拿到 当前祯的插入器
                final Interpolator interpolator = nextKeyframe.getInterpolator();
                if (interpolator != null) {
                    // 记录当前祯的插值
                    fraction = interpolator.getInterpolation(fraction);
                }
                // 当前祯的插值 - 上一祯的分数 / 当前祯的分数 - 上一祯的分数
                float intervalFraction = (fraction - prevKeyframe.getFraction()) / (nextKeyframe.getFraction() - prevKeyframe.getFraction());
                // 拿到 上一祯的值
                int prevValue = prevKeyframe.getIntValue();
                // 拿到 当前祯的值
                int nextValue = nextKeyframe.getIntValue();
                // 计算评估值
                return mEvaluator == null ? prevValue + (int) (intervalFraction * (nextValue - prevValue)) : ((Number) mEvaluator.evaluate(intervalFraction, prevValue, nextValue)).intValue();
            }
            // 保存该祯为上一祯记录
            // 以供下次循环
            prevKeyframe = nextKeyframe;
        }
        // 返回最后一祯 的值
        return ((Number) mKeyframes.get(mNumKeyframes - 1).getValue()).intValue();
    }
}

