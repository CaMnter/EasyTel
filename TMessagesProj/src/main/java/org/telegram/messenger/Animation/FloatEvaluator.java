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
 * Android 11 以上的 FloatEvaluator
 * 用于执行float之间的插值
 */
public class FloatEvaluator implements TypeEvaluator<Number> {
    /**
     * @param fraction   从开始到结束的分数值
     * @param startValue float开始值
     * @param endValue   float结束值
     * @return Float 返回一个在startValue和endValue之间的线性插值
     */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        // start + fraction * ( end - start )
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
}