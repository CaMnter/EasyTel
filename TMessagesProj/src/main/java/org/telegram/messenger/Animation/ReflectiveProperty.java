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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Internal class to automatically generate a Property for a given class/name pair, given the
 * specification of {@link Property#of(java.lang.Class, java.lang.Class, java.lang.String)}
 * 根据给定的 class/name 对 自动生成 Property
 * 该类用于反射调用 Property的 setter 和 getter 存储 或 读取 属性
 */
class ReflectiveProperty<T, V> extends Property<T, V> {

    private static final String PREFIX_GET = "get";
    private static final String PREFIX_IS = "is";
    private static final String PREFIX_SET = "set";
    private Method mSetter;
    private Method mGetter;
    private Field mField;

    /**
     * For given property name 'name', look for getName/isName method or 'name' field.
     * Also look for setName method (optional - could be readonly). Failing method getters and
     * field results in throwing NoSuchPropertyException.
     * 通过给定的 property name,寻找 getName/isName 方法 或者 'name' 字段。
     * 也寻找 setName 方法 ( 可选的,可以是只读的 ) getters 方法失败后会抛出NoSuchPropertyException
     *
     * @param propertyHolder The class on which the methods or field are found
     * @param name           The name of the property, where this name is capitalized and appended to
     *                       "get" and "is to search for the appropriate methods. If the get/is methods are not found,
     *                       the constructor will search for a field with that exact name.
     */
    public ReflectiveProperty(Class<T> propertyHolder, Class<V> valueType, String name) {
        // TODO: cache reflection info for each new class/name pair
        // TODO: 每个新的类/名称对 缓存反射信息
        // 调用 Property 构造方法 缓存 valueType 和 name
        super(valueType, name);
        // 拿到 名字的 第一个大写字母
        char firstLetter = Character.toUpperCase(name.charAt(0));
        // 拿到 名字的 第二个字母
        String theRest = name.substring(1);
        // 拿到 大写的名字
        String capitalizedName = firstLetter + theRest;
        // 构造 getter 方法的名字
        String getterName = PREFIX_GET + capitalizedName;
        try {
            // 反射获取 该类 的 getter 方法
            mGetter = propertyHolder.getMethod(getterName, (Class<?>[]) null);
        } catch (NoSuchMethodException e) {
            try {
                /* The native implementation uses JNI to do reflection, which allows access to private methods.
                 * getDeclaredMethod(..) does not find superclass methods, so it's implemented as a fallback.
                 */
                // 再次尝试 通过 getDeclaredMethod 获取 getter 方法
                mGetter = propertyHolder.getDeclaredMethod(getterName, (Class<?>[]) null);
                // 取消 Java语言访问检查，提高反射速度
                mGetter.setAccessible(true);
            } catch (NoSuchMethodException e2) {
                // getName() not available - try isName() instead
                // 如果没有 getter 方法 就去 获取 is 方法
                getterName = PREFIX_IS + capitalizedName;
                try {
                    // 反射获取 该类 的 is 方法
                    mGetter = propertyHolder.getMethod(getterName, (Class<?>[]) null);
                } catch (NoSuchMethodException e3) {
                    try {
                        /* The native implementation uses JNI to do reflection, which allows access to private methods.
                         * getDeclaredMethod(..) does not find superclass methods, so it's implemented as a fallback.
                         */
                        // 再次尝试 通过 getDeclaredMethod 获取 is 方法
                        mGetter = propertyHolder.getDeclaredMethod(getterName, (Class<?>[]) null);
                        // 取消 Java语言访问检查，提高反射速度
                        mGetter.setAccessible(true);
                    } catch (NoSuchMethodException e4) {
                        // Try public field instead
                        try {
                            // 反射获取 该类 的 name 对应属性
                            mField = propertyHolder.getField(name);
                            // 获取 该属性的 类型
                            Class fieldType = mField.getType();
                            // 进行 属性类型 匹配
                            if (!typesMatch(valueType, fieldType)) {
                                throw new NoSuchPropertyException("Underlying type (" + fieldType + ") " +
                                        "does not match Property type (" + valueType + ")");
                            }
                            return;
                        } catch (NoSuchFieldException e5) {
                            // no way to access property - throw appropriate exception
                            throw new NoSuchPropertyException("No accessor method or field found for"
                                    + " property with name " + name);
                        }
                    }
                }
            }
        }
        // 拿到 getter 的返回类型
        Class getterType = mGetter.getReturnType();
        // Check to make sure our getter type matches our valueType
        // 检查 getter 的返回类型 是否 与 valueType 类型一致
        if (!typesMatch(valueType, getterType)) {
            throw new NoSuchPropertyException("Underlying type (" + getterType + ") " +
                    "does not match Property type (" + valueType + ")");
        }
        // 构造 setter 方法的名字
        String setterName = PREFIX_SET + capitalizedName;
        try {
            // mSetter = propertyHolder.getMethod(setterName, getterType);
            // The native implementation uses JNI to do reflection, which allows access to private methods.
            // 通过 getDeclaredMethod 获取 setter 方法
            mSetter = propertyHolder.getDeclaredMethod(setterName, getterType);
            // 取消 Java语言访问检查，提高反射速度
            mSetter.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
            // Okay to not have a setter - just a readonly property
        }
    }

    /**
     * Utility method to check whether the type of the underlying field/method on the target
     * object matches the type of the Property. The extra checks for primitive types are because
     * generics will force the Property type to be a class, whereas the type of the underlying
     * method/field will probably be a primitive type instead. Accept float as matching Float,
     * etc.
     */
    private boolean typesMatch(Class<V> valueType, Class getterType) {
        if (getterType != valueType) {
            // 是否是基本类型
            if (getterType.isPrimitive()) {
                // 再接着判断是否是以下类型
                return (getterType == float.class && valueType == Float.class) ||
                        (getterType == int.class && valueType == Integer.class) ||
                        (getterType == boolean.class && valueType == Boolean.class) ||
                        (getterType == long.class && valueType == Long.class) ||
                        (getterType == double.class && valueType == Double.class) ||
                        (getterType == short.class && valueType == Short.class) ||
                        (getterType == byte.class && valueType == Byte.class) ||
                        (getterType == char.class && valueType == Character.class);
            }
            return false;
        }
        return true;
    }

    /**
     * 反射调用 setter 设置 值
     *
     * @param object object
     * @param value  value
     */
    @Override
    public void set(T object, V value) {
        // 如果 setter 方法不为null
        if (mSetter != null) {
            try {
                // 反射调用 setter 方法
                mSetter.invoke(object, value);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        } else if (mField != null) {
            try {
                // 直接 反射属性 设置值
                mField.set(object, value);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        } else {
            throw new UnsupportedOperationException("Property " + getName() + " is read-only");
        }
    }

    @Override
    public V get(T object) {
        // 如果 getter 方法不为null
        if (mGetter != null) {
            try {
                // 调用 getter 方法
                return (V) mGetter.invoke(object, (Object[]) null);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        } else if (mField != null) {
            try {
                // 直接 反射属性 获取值
                return (V) mField.get(object);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }
        // Should not get here: there should always be a non-null getter or field
        throw new AssertionError();
    }

    /**
     * Returns false if there is no setter or public field underlying this Property.
     * 实现 ReflectiveProperty 的读写权限逻辑
     * 如果 setter 并且 属性 为null 则只读
     */
    @Override
    public boolean isReadOnly() {
        return (mSetter == null && mField == null);
    }
}
