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

/**
 * Android 11 以上的 TypeEvaluator
 * <p/>
 * 其实例对象提供给{@link ValueAnimator#setEvaluator(TypeEvaluator)}方法设置上
 * Evaluators允许开发者创建任意属性类型的动画
 * 并且允许它们支持自定义类型Evaluators
 *
 * @param <T> T
 */
public interface TypeEvaluator<T> {
    /**
     * @param fraction 从开始到结束的分数值
     * @param startValue 开始值
     * @param endValue   结束值
     * @return T 返回一个在startValue和endValue之间的线性插值
     */
    T evaluate(float fraction, T startValue, T endValue);
}
