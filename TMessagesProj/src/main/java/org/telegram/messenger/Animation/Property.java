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
 * Android 14 以上的 Property
 *
 * @param <T>
 * @param <V>
 */
public abstract class Property<T, V> {

    private final String mName;
    private final Class<V> mType;

    /**
     * 构造一个 ReflectiveProperty 对象
     *
     * @param hostType  hostType
     * @param valueType valueType
     * @param name      name
     * @param <T>       T
     * @param <V>       V
     * @return Property
     */
    public static <T, V> Property<T, V> of(Class<T> hostType, Class<V> valueType, String name) {
        return new ReflectiveProperty<T, V>(hostType, valueType, name);
    }

    /**
     * 构造方法
     * 需要 类型 和 名称
     *
     * @param type type
     * @param name name
     */
    public Property(Class<V> type, String name) {
        mName = name;
        mType = type;
    }

    /**
     * 设置是否 只读
     * 默认实现为 false
     *
     * @return boolean
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * 设置 值
     * 默认是不可设置的
     * 抛出异常
     *
     * @param object object
     * @param value  value
     */
    public void set(T object, V value) {
        throw new UnsupportedOperationException("Property " + getName() + " is read-only");
    }

    /**
     * 让子类去实现该 get 方法
     *
     * @param object object
     * @return V V
     */
    public abstract V get(T object);

    /**
     * 获取名称
     *
     * @return String
     */
    public String getName() {
        return mName;
    }

    /**
     * 获取类型
     *
     * @return Class<V>
     */
    public Class<V> getType() {
        return mType;
    }
}
