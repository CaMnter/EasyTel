/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * Android 14 以上的 IntProperty
 * 处理 Integer 类型的 Property
 *
 * @param <T>
 */
public abstract class IntProperty<T> extends Property<T, Integer> {

    public IntProperty(String name) {
        super(Integer.class, name);
    }

    /**
     * 让子类去实现 setValue 的逻辑
     *
     * @param object object
     * @param value  value
     */
    public abstract void setValue(T object, int value);

    /**
     * 实现了 Integer 的逻辑
     * 不可让子类覆写了
     *
     * @param object object
     * @param value  value
     */
    @Override
    final public void set(T object, Integer value) {
        setValue(object, value.intValue());
    }
}