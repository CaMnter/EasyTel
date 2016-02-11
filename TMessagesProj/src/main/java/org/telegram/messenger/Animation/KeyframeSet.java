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

import android.util.Log;
import android.view.animation.Interpolator;

import org.telegram.messenger.Animation.Keyframe.FloatKeyframe;
import org.telegram.messenger.Animation.Keyframe.IntKeyframe;
import org.telegram.messenger.Animation.Keyframe.ObjectKeyframe;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 这个类持有Keyframe对象的集合
 * 并且被ValueAnimator调用来计算那些Keyframes之间给定动画的值
 * 这个类的内部进行动画包装
 * 因为它是一个Keyframe存储和使用的实现细节
 */
class KeyframeSet {

    // 记录所有关键帧的个数
    int mNumKeyframes;

    // 记录第一个 关键帧
    Keyframe mFirstKeyframe;

    // 记录最有一个 关键帧
    Keyframe mLastKeyframe;

    /*
     * 插入器
     * 只有在 2-Keyframe 的情况下使用
     */
    Interpolator mInterpolator;

    /*
     * 记录所有关键帧
     * 只在不是 2 keyframes 的情况下使用
     */
    ArrayList<Keyframe> mKeyframes;

    TypeEvaluator mEvaluator;

    public KeyframeSet(Keyframe... keyframes) {
        mNumKeyframes = keyframes.length;
        mKeyframes = new ArrayList<Keyframe>();
        mKeyframes.addAll(Arrays.asList(keyframes));
        mFirstKeyframe = mKeyframes.get(0);
        mLastKeyframe = mKeyframes.get(mNumKeyframes - 1);
        mInterpolator = mLastKeyframe.getInterpolator();
    }

    /**
     * 传入一组 int
     * 生成一个 IntKeyframeSet
     *
     * @param values values
     * @return IntKeyframeSet
     */
    public static KeyframeSet ofInt(int... values) {
        // 记录 int 数组的长度
        int numKeyframes = values.length;
        // 创建一个 长度最少大于2 的 IntKeyframe数组
        IntKeyframe keyframes[] = new IntKeyframe[Math.max(numKeyframes, 2)];
        // int 数组长度 等于 1
        if (numKeyframes == 1) {
            // 第一祯 用 0f 实例化 IntKeyframe
            keyframes[0] = (IntKeyframe) Keyframe.ofInt(0f);
            // 第二祯 用 1f 和 values[0] 实例化 IntKeyframe
            keyframes[1] = (IntKeyframe) Keyframe.ofInt(1f, values[0]);
        } else {
            // 第一祯 用 1f 和 values[0] 实例化 IntKeyframe
            keyframes[0] = (IntKeyframe) Keyframe.ofInt(0f, values[0]);
            // 其它祯 通过 该祯所在位置的百分比 和 values[i] 实例化 IntKeyframe
            for (int i = 1; i < numKeyframes; ++i) {
                keyframes[i] = (IntKeyframe) Keyframe.ofInt((float) i / (numKeyframes - 1), values[i]);
            }
        }
        // 通过 一个 IntKeyframe 数组 构造一个 IntKeyframeSet 实例对象
        return new IntKeyframeSet(keyframes);
    }

    /**
     * 传入一组 float
     * 生成一个 FloatKeyframeSet
     *
     * @param values values
     * @return KeyframeSet
     */
    public static KeyframeSet ofFloat(float... values) {
        // 记录 float 数组中是否有 Not-a-Number
        boolean badValue = false;
        // 记录 float 数组的长度
        int numKeyframes = values.length;
        // 创建一个 长度最少大于2 的 FloatKeyframe数组
        FloatKeyframe keyframes[] = new FloatKeyframe[Math.max(numKeyframes, 2)];
        // float 数组长度 等于 1
        if (numKeyframes == 1) {
            // 第一祯 用 0f 实例化 FloatKeyframe
            keyframes[0] = (FloatKeyframe) Keyframe.ofFloat(0f);
            // 第二祯 用 1f 和 values[0] 实例化 FloatKeyframe
            keyframes[1] = (FloatKeyframe) Keyframe.ofFloat(1f, values[0]);
            // 如果 Not-a-Number
            if (Float.isNaN(values[0])) {
                // 标记为坏的值
                badValue = true;
            }
        } else {
            // 第一祯 用 1f 和 values[0] 实例化 FloatKeyframe
            keyframes[0] = (FloatKeyframe) Keyframe.ofFloat(0f, values[0]);
            // 其它祯 通过 该祯所在位置的百分比 和 values[i] 实例化 FloatKeyframe
            for (int i = 1; i < numKeyframes; ++i) {
                keyframes[i] = (FloatKeyframe) Keyframe.ofFloat((float) i / (numKeyframes - 1), values[i]);
                // 如果 Not-a-Number
                if (Float.isNaN(values[i])) {
                    // 标记为坏的值
                    badValue = true;
                }
            }
        }
        // 如果Not-a-Number的值
        if (badValue) {
            Log.w("Animator", "Bad value (NaN) in float animator");
        }
        // 通过 一个 FloatKeyframe 数组 构造一个 IntKeyframeSet 实例对象
        return new FloatKeyframeSet(keyframes);
    }

    /**
     * 传入一组 Keyframe
     * 生成一个 KeyframeSet
     *
     * @param keyframes keyframes
     * @return KeyframeSet
     */
    public static KeyframeSet ofKeyframe(Keyframe... keyframes) {
        // 记录 keyframes 数组的长度
        int numKeyframes = keyframes.length;
        // 记录 keyframes 数组中是否有 FloatKeyframe
        boolean hasFloat = false;
        // 记录 keyframes 数组中是否有 IntKeyframe
        boolean hasInt = false;
        // 记录 keyframes 数组中是否有 其他类型
        boolean hasOther = false;
        /*
         * 遍历所有Keyframe
         * 查看
         * 是否存在FloatKeyframe类型
         * 是否存在IntKeyframe类型
         * 是否存在其他类型
         */
        for (Keyframe keyframe : keyframes) {
            if (keyframe instanceof FloatKeyframe) {
                hasFloat = true;
            } else if (keyframe instanceof IntKeyframe) {
                hasInt = true;
            } else {
                hasOther = true;
            }
        }
        /*
         * 以下判断是什么类型
         * 返回什么类型的KeyframeSet
         * 其他类型返回KeyframeSet
         */
        if (hasFloat && !hasInt && !hasOther) {
            FloatKeyframe floatKeyframes[] = new FloatKeyframe[numKeyframes];
            for (int i = 0; i < numKeyframes; ++i) {
                floatKeyframes[i] = (FloatKeyframe) keyframes[i];
            }
            return new FloatKeyframeSet(floatKeyframes);
        } else if (hasInt && !hasFloat && !hasOther) {
            IntKeyframe intKeyframes[] = new IntKeyframe[numKeyframes];
            for (int i = 0; i < numKeyframes; ++i) {
                intKeyframes[i] = (IntKeyframe) keyframes[i];
            }
            return new IntKeyframeSet(intKeyframes);
        } else {
            return new KeyframeSet(keyframes);
        }
    }

    /**
     * @param values values
     * @return KeyframeSet
     */
    public static KeyframeSet ofObject(Object... values) {
        // 记录 Object 数组的长度
        int numKeyframes = values.length;
        // 创建一个 长度最少大于2 的 ObjectKeyframe 数组
        ObjectKeyframe keyframes[] = new ObjectKeyframe[Math.max(numKeyframes, 2)];
        // Object 数组长度 等于 1
        if (numKeyframes == 1) {
            // 第一祯 用 0f 实例化 ObjectKeyframe
            keyframes[0] = (ObjectKeyframe) Keyframe.ofObject(0f);
            // 第二祯 用 1f 和 values[0] 实例化 ObjectKeyframe
            keyframes[1] = (ObjectKeyframe) Keyframe.ofObject(1f, values[0]);
        } else {
            // 第一祯 用 1f 和 values[0] 实例化 ObjectKeyframe
            keyframes[0] = (ObjectKeyframe) Keyframe.ofObject(0f, values[0]);
            // 其它祯 通过 该祯所在位置的百分比 和 values[i] 实例化 ObjectKeyframe
            for (int i = 1; i < numKeyframes; ++i) {
                keyframes[i] = (ObjectKeyframe) Keyframe.ofObject((float) i / (numKeyframes - 1), values[i]);
            }
        }
        // 通过 一个 IntKeyframe 数组 构造一个 KeyframeSet 实例对象
        return new KeyframeSet(keyframes);
    }

    /**
     * 设置 mEvaluator 属性
     *
     * @param evaluator evaluator
     */
    public void setEvaluator(TypeEvaluator evaluator) {
        mEvaluator = evaluator;
    }

    /**
     * KeyframeSet 克隆方法
     *
     * @return KeyframeSet
     */
    @Override
    public KeyframeSet clone() {
        /*
         * 就是 拷贝一份新的 Keyframe 数组
         * 再用这份拷贝数组 去实例化 一个
         * 新的 KeyframeSet
         */
        ArrayList<Keyframe> keyframes = mKeyframes;
        int numKeyframes = mKeyframes.size();
        Keyframe[] newKeyframes = new Keyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; ++i) {
            newKeyframes[i] = keyframes.get(i).clone();
        }
        return new KeyframeSet(newKeyframes);
    }

    /**
     * @param fraction fraction
     * @return Object
     */
    @SuppressWarnings("unchecked")
    public Object getValue(float fraction) {
        // 如果只有 两祯
        if (mNumKeyframes == 2) {
            // 如果插入器为null
            if (mInterpolator != null) {
                // 通过插入器 拿到 插值
                fraction = mInterpolator.getInterpolation(fraction);
            }
            /*
             * 拿到评估器
             * 传入 插值、第一祯值、最后一祯值 去计算评估值
             */
            return mEvaluator.evaluate(fraction, mFirstKeyframe.getValue(), mLastKeyframe.getValue());
        }
        if (fraction <= 0f) {
            // 拿到第二祯
            final Keyframe nextKeyframe = mKeyframes.get(1);
            // 拿到第二祯的 插入器
            final Interpolator interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                // 根据第二祯 插入器 计算插值
                fraction = interpolator.getInterpolation(fraction);
            }
            // 拿到第一祯 的 分数
            final float prevFraction = mFirstKeyframe.getFraction();
            // 第二祯的插值 - 第一祯分数 / 第二祯的分数 - 第一祯分数
            float intervalFraction = (fraction - prevFraction) / (nextKeyframe.getFraction() - prevFraction);
            // 返回评估器评估的值
            return mEvaluator.evaluate(intervalFraction, mFirstKeyframe.getValue(), nextKeyframe.getValue());
        } else if (fraction >= 1f) {
            // 拿到 倒数第二祯
            final Keyframe prevKeyframe = mKeyframes.get(mNumKeyframes - 2);
            // 拿到 最后一祯 的插入器
            final Interpolator interpolator = mLastKeyframe.getInterpolator();
            if (interpolator != null) {
                // 根据最后一祯 插入器 计算插值
                fraction = interpolator.getInterpolation(fraction);
            }
            // 倒数第二祯 的 分数
            final float prevFraction = prevKeyframe.getFraction();
            // 最后一祯的插值 - 倒数第二祯的分数 / 最后一祯的分数 - 倒数第二祯的分数
            float intervalFraction = (fraction - prevFraction) / (mLastKeyframe.getFraction() - prevFraction);
            // 返回评估器评估的值
            return mEvaluator.evaluate(intervalFraction, prevKeyframe.getValue(), mLastKeyframe.getValue());
        }

        /************************************
         * fraction ＝ 0.0f - 1.0f 之间的情况 *
         ************************************/

        // 记录上一祯
        // 开始的话先拿到第一祯
        Keyframe prevKeyframe = mFirstKeyframe;
        // 遍历 第一祯以后的 所有祯
        for (int i = 1; i < mNumKeyframes; ++i) {
            // 拿到每一祯
            Keyframe nextKeyframe = mKeyframes.get(i);
            // 如果传入方法的分数 < 该祯的分数
            if (fraction < nextKeyframe.getFraction()) {
                // 拿到 当前祯的插入器
                final Interpolator interpolator = nextKeyframe.getInterpolator();
                if (interpolator != null) {
                    // 记录当前祯的插值
                    fraction = interpolator.getInterpolation(fraction);
                }
                // 拿到第一祯的 分数
                final float prevFraction = prevKeyframe.getFraction();
                // 当前祯的插值 - 上一祯的分数 / 当前祯的分数 - 上一祯的分数
                float intervalFraction = (fraction - prevFraction) / (nextKeyframe.getFraction() - prevFraction);
                // 返回评估器评估的值
                return mEvaluator.evaluate(intervalFraction, prevKeyframe.getValue(), nextKeyframe.getValue());
            }
            // 保存该祯为上一祯记录
            // 以供下次循环
            prevKeyframe = nextKeyframe;
        }
        // 返回最后一祯 的值
        return mLastKeyframe.getValue();
    }

    /**
     * 就多了输出所有祯的值
     *
     * @return String
     */
    @Override
    public String toString() {
        String returnVal = " ";
        for (int i = 0; i < mNumKeyframes; ++i) {
            returnVal += mKeyframes.get(i).getValue() + "  ";
        }
        return returnVal;
    }
}
