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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Android 11-20 之间的 PropertyValuesHolder
 * 这个类包含关于属性和该属性应在动画期间采取的信息的值
 * PropertyValuesHolder对象可以用于创建具有ValueAnimator或ObjectAnimator上并联几个不同的属性操作的动画
 */
public class PropertyValuesHolder implements Cloneable {

    // 与值对应的 属性名字
    String mPropertyName;

    // 缓存 实例时的 Property 对象
    protected Property mProperty;

    // 缓存 setter 方法
    Method mSetter = null;

    // 缓存 getter 方法
    private Method mGetter = null;

    // 缓存 值 类型
    Class mValueType;

    // 缓存 KeyframeSet 对象
    KeyframeSet mKeyframeSet = null;

    // 实例化 IntEvaluator 作为 备用评估器
    private static final TypeEvaluator sIntEvaluator = new IntEvaluator();

    // 实例化 FloatEvaluator 作为 备用评估器
    private static final TypeEvaluator sFloatEvaluator = new FloatEvaluator();

    // Float 类型数组
    private static Class[] FLOAT_VARIANTS = {float.class, Float.class, double.class, int.class, Double.class, Integer.class};
    // Integer 类型数组
    private static Class[] INTEGER_VARIANTS = {int.class, Integer.class, float.class, double.class, Float.class, Double.class};
    // Double 类型数组
    private static Class[] DOUBLE_VARIANTS = {double.class, Double.class, float.class, int.class, Float.class, Integer.class};

    // 缓存 查找过的 setter 方法
    private static final HashMap<Class, HashMap<String, Method>> sSetterPropertyMap = new HashMap<Class, HashMap<String, Method>>();

    // 缓存 查找过的 getter 方法
    private static final HashMap<Class, HashMap<String, Method>> sGetterPropertyMap = new HashMap<Class, HashMap<String, Method>>();

    // 用于 在 读取 setter or getter 的时候
    // 写缓存时的锁
    final ReentrantReadWriteLock mPropertyMapLock = new ReentrantReadWriteLock();

    //
    final Object[] mTmpValueArray = new Object[1];

    // 缓存 选用的 评估器
    private TypeEvaluator mEvaluator;

    // 缓存 mKeyframeSet 评估出来的值
    private Object mAnimatedValue;

    /**
     * 属性名 初始化 PropertyValuesHolder
     *
     * @param propertyName propertyName
     */
    private PropertyValuesHolder(String propertyName) {
        mPropertyName = propertyName;
    }

    /**
     * 属性 初始化 PropertyValuesHolder
     *
     * @param property property
     */
    private PropertyValuesHolder(Property property) {
        mProperty = property;
        if (property != null) {
            mPropertyName = property.getName();
        }
    }

    /**
     * setIntValues
     * 转换 为 IntPropertyValuesHolder
     *
     * @param propertyName propertyName
     * @param values       values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofInt(String propertyName, int... values) {
        return new IntPropertyValuesHolder(propertyName, values);
    }

    /**
     * setIntValues
     * 转换 为 IntPropertyValuesHolder
     *
     * @param property property
     * @param values   values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofInt(Property<?, Integer> property, int... values) {
        return new IntPropertyValuesHolder(property, values);
    }

    /**
     * setFloatValues
     * 转换 为 FloatPropertyValuesHolder
     *
     * @param propertyName propertyName
     * @param values       values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofFloat(String propertyName, float... values) {
        return new FloatPropertyValuesHolder(propertyName, values);
    }

    /**
     * setFloatValues
     * 转换 为 FloatPropertyValuesHolder
     *
     * @param property property
     * @param values   values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofFloat(Property<?, Float> property, float... values) {
        return new FloatPropertyValuesHolder(property, values);
    }

    /**
     * setObjectValues
     * 设置后 返回PropertyValuesHolder
     *
     * @param propertyName propertyName
     * @param evaluator    evaluator
     * @param values       values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofObject(String propertyName, TypeEvaluator evaluator,
                                                Object... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    /**
     * setObjectValues
     * 设置后 返回PropertyValuesHolder
     *
     * @param property  property
     * @param evaluator evaluator
     * @param values    values
     * @param <V>       V
     * @return V
     */
    public static <V> PropertyValuesHolder ofObject(Property property,
                                                    TypeEvaluator<V> evaluator, V... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    /**
     * 设置 mKeyframeSet
     * 返回PropertyValuesHolder
     *
     * @param propertyName propertyName
     * @param values       values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofKeyframe(String propertyName, Keyframe... values) {
        KeyframeSet keyframeSet = KeyframeSet.ofKeyframe(values);
        if (keyframeSet instanceof IntKeyframeSet) {
            return new IntPropertyValuesHolder(propertyName, (IntKeyframeSet) keyframeSet);
        } else if (keyframeSet instanceof FloatKeyframeSet) {
            return new FloatPropertyValuesHolder(propertyName, (FloatKeyframeSet) keyframeSet);
        } else {
            PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
            pvh.mKeyframeSet = keyframeSet;
            pvh.mValueType = values[0].getType();
            return pvh;
        }
    }

    /**
     * 设置 mKeyframeSet
     * 返回PropertyValuesHolder
     *
     * @param property property
     * @param values   values
     * @return PropertyValuesHolder
     */
    public static PropertyValuesHolder ofKeyframe(Property property, Keyframe... values) {
        KeyframeSet keyframeSet = KeyframeSet.ofKeyframe(values);
        if (keyframeSet instanceof IntKeyframeSet) {
            return new IntPropertyValuesHolder(property, (IntKeyframeSet) keyframeSet);
        } else if (keyframeSet instanceof FloatKeyframeSet) {
            return new FloatPropertyValuesHolder(property, (FloatKeyframeSet) keyframeSet);
        } else {
            PropertyValuesHolder pvh = new PropertyValuesHolder(property);
            pvh.mKeyframeSet = keyframeSet;
            pvh.mValueType = values[0].getType();
            return pvh;
        }
    }

    /**
     * 设置 int 数组
     *
     * @param values values
     */
    public void setIntValues(int... values) {
        mValueType = int.class;
        mKeyframeSet = KeyframeSet.ofInt(values);
    }

    /**
     * 设置 float 数组
     *
     * @param values values
     */
    public void setFloatValues(float... values) {
        mValueType = float.class;
        mKeyframeSet = KeyframeSet.ofFloat(values);
    }

    /**
     * 设置 Keyframe 数组
     *
     * @param values values
     */
    public void setKeyframes(Keyframe... values) {
        int numKeyframes = values.length;
        Keyframe keyframes[] = new Keyframe[Math.max(numKeyframes, 2)];
        mValueType = values[0].getType();
        System.arraycopy(values, 0, keyframes, 0, numKeyframes);
        mKeyframeSet = new KeyframeSet(keyframes);
    }

    /**
     * 设置 Object 数组
     *
     * @param values values
     */
    public void setObjectValues(Object... values) {
        mValueType = values[0].getClass();
        mKeyframeSet = KeyframeSet.ofObject(values);
    }

    /**
     * 获取 属性 方法
     *
     * @param targetClass targetClass
     * @param prefix      prefix
     * @param valueType   valueType
     * @return Method
     */
    @SuppressWarnings("unchecked")
    private Method getPropertyFunction(Class targetClass, String prefix, Class valueType) {
        // 记录 返回 方法
        Method returnVal = null;
        // 获取 方法名称
        String methodName = getMethodName(prefix, mPropertyName);
        Class args[] = null;
        if (valueType == null) {
            try {
                // 根据方法名 反射方法获取之
                returnVal = targetClass.getMethod(methodName);
            } catch (Throwable e) {
                try {
                    // 再次尝试 getDeclaredMethod 反射方法获取之
                    returnVal = targetClass.getDeclaredMethod(methodName);
                    // 取消 Java语言访问检查，提高反射速度
                    returnVal.setAccessible(true);
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            args = new Class[1];
            Class typeVariants[];
            // 判断 属性的类型 并设置给typeVariants
            if (mValueType.equals(Float.class)) {
                typeVariants = FLOAT_VARIANTS;
            } else if (mValueType.equals(Integer.class)) {
                typeVariants = INTEGER_VARIANTS;
            } else if (mValueType.equals(Double.class)) {
                typeVariants = DOUBLE_VARIANTS;
            } else {
                typeVariants = new Class[1];
                typeVariants[0] = mValueType;
            }

            // 根据类型数组 去尝试找 方法
            for (Class typeVariant : typeVariants) {
                args[0] = typeVariant;
                try {
                    // 第一次 用 getMethod 尝试
                    returnVal = targetClass.getMethod(methodName, args);
                    mValueType = typeVariant;
                    return returnVal;
                } catch (Throwable e) {
                    try {
                        // 第二次 用 getDeclaredMethod 尝试
                        returnVal = targetClass.getDeclaredMethod(methodName, args);
                        returnVal.setAccessible(true);
                        mValueType = typeVariant;
                        return returnVal;
                    } catch (Throwable e2) {
                        // Swallow the error and keep trying other variants
                        // 什么都没用 继续 循环
                    }
                }
            }
        }
        return returnVal;
    }

    /**
     * 设置 setter 或者 getter 方法
     *
     * @param targetClass    targetClass
     * @param propertyMapMap propertyMapMap
     * @param prefix         prefix
     * @param valueType      valueType
     * @return Method
     */
    private Method setupSetterOrGetter(Class targetClass, HashMap<Class, HashMap<String, Method>> propertyMapMap, String prefix, Class valueType) {
        // 搁置 setter 或者 getter 方法
        Method setterOrGetter = null;
        try {
            // 写锁锁定
            mPropertyMapLock.writeLock().lock();
            // 尝试 拿 类所有方法 缓存
            HashMap<String, Method> propertyMap = propertyMapMap.get(targetClass);
            // 类所有方法缓存不为空
            if (propertyMap != null) {
                // 获取 setter or getter 方法 缓存
                setterOrGetter = propertyMap.get(mPropertyName);
            }
            // 如果获取 到 setter or getter 方法 缓存
            if (setterOrGetter == null) {
                // 反射获取 setter or getter 方法
                setterOrGetter = getPropertyFunction(targetClass, prefix, valueType);
                // 类所有方法缓存为空
                if (propertyMap == null) {
                    // 创建 类所有方法缓存 map
                    propertyMap = new HashMap<String, Method>();
                    // 先加入到 class-所有方法map 缓存里
                    propertyMapMap.put(targetClass, propertyMap);
                }
                // 类所有方法缓存中放入 setter or getter 方法
                propertyMap.put(mPropertyName, setterOrGetter);
            }
        } finally {
            // 写锁打开
            mPropertyMapLock.writeLock().unlock();
        }
        // 返回 setter or getter 方法
        // 最坏的情况下 null
        return setterOrGetter;
    }

    /**
     * 设置 setter 方法
     *
     * @param targetClass targetClass
     */
    void setupSetter(Class targetClass) {
        mSetter = setupSetterOrGetter(targetClass, sSetterPropertyMap, "set", mValueType);
    }

    /**
     * 设置 getter 方法
     *
     * @param targetClass targetClass
     */
    private void setupGetter(Class targetClass) {
        mGetter = setupSetterOrGetter(targetClass, sGetterPropertyMap, "get", null);
    }

    /**
     * 设置 setter and getter
     *
     * @param target target
     */
    @SuppressWarnings("unchecked")
    void setupSetterAndGetter(Object target) {
        // 如果 传进来的 Property
        if (mProperty != null) {
            try {
                // 基本上 都是调用 ReflectiveProperty get() 去反射调用get取值
                Object testValue = mProperty.get(target);
                // 拿到 每一祯
                for (Keyframe kf : mKeyframeSet.mKeyframes) {
                    // 如果 祯 没有值
                    if (!kf.hasValue()) {
                        // 设置上 Property 的值
                        kf.setValue(mProperty.get(target));
                    }
                }
                return;
            } catch (Throwable e) {
                mProperty = null;
            }
        }
        // 拿到 目标类（ 需要添加动画的类 ）
        Class targetClass = target.getClass();
        // 如果 setter 为null 还要设置一遍
        if (mSetter == null) {
            setupSetter(targetClass);
        }
        // 拿到 每一祯
        for (Keyframe kf : mKeyframeSet.mKeyframes) {
            // 如果 祯 没有值
            if (!kf.hasValue()) {
                // 如果 getter 为 null
                if (mGetter == null) {
                    // 这是 getter 方法
                    setupGetter(targetClass);
                    if (mGetter == null) {
                        return;
                    }
                }
                try {
                    // 给每一祯 设置上 getter反射取到的值
                    kf.setValue(mGetter.invoke(target));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置值
     *
     * @param target target
     * @param kf     kf
     */
    @SuppressWarnings("unchecked")
    private void setupValue(Object target, Keyframe kf) {
        // 如果 属性 不为 null
        if (mProperty != null) {
            // 给这一祯赋值
            kf.setValue(mProperty.get(target));
        }
        try {
            // 如果 getter
            if (mGetter == null) {
                Class targetClass = target.getClass();
                setupGetter(targetClass);
                if (mGetter == null) {
                    return;
                }
            }
            kf.setValue(mGetter.invoke(target));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置 开始值
     *
     * @param target target
     */
    void setupStartValue(Object target) {
        setupValue(target, mKeyframeSet.mKeyframes.get(0));
    }

    /**
     * 设置 结束值
     *
     * @param target target
     */
    void setupEndValue(Object target) {
        setupValue(target, mKeyframeSet.mKeyframes.get(mKeyframeSet.mKeyframes.size() - 1));
    }

    /**
     * PropertyValuesHolder 克隆方法
     *
     * @return PropertyValuesHolder
     */
    @Override
    public PropertyValuesHolder clone() {
        try {
            // native 层克隆一份 PropertyValuesHolder
            PropertyValuesHolder newPVH = (PropertyValuesHolder) super.clone();
            newPVH.mPropertyName = mPropertyName;
            newPVH.mProperty = mProperty;
            newPVH.mKeyframeSet = mKeyframeSet.clone();
            newPVH.mEvaluator = mEvaluator;
            return newPVH;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * 设置 动画 的值
     *
     * @param target target
     */
    @SuppressWarnings("unchecked")
    void setAnimatedValue(Object target) {
        // 如果 属性 不为null
        if (mProperty != null) {
            // 反射调用 setter 设置 值
            mProperty.set(target, getAnimatedValue());
        }
        // 如果 setter 不为null
        if (mSetter != null) {
            try {
                mTmpValueArray[0] = getAnimatedValue();
                // 反射调用 setter 设置 值
                mSetter.invoke(target, mTmpValueArray);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化
     */
    void init() {
        // 如果评估器为null
        // 选择备选评估器
        if (mEvaluator == null) {
            mEvaluator = (mValueType == Integer.class) ? sIntEvaluator : (mValueType == Float.class) ? sFloatEvaluator : null;
        }
        // 给 KeyframeSet 设置上 评估器
        if (mEvaluator != null) {
            mKeyframeSet.setEvaluator(mEvaluator);
        }
    }

    /**
     * 设置 评估器
     *
     * @param evaluator evaluator
     */
    public void setEvaluator(TypeEvaluator evaluator) {
        mEvaluator = evaluator;
        mKeyframeSet.setEvaluator(evaluator);
    }

    /**
     * 根据 分数 KeyframeSet计算值
     *
     * @param fraction fraction
     */
    void calculateValue(float fraction) {
        mAnimatedValue = mKeyframeSet.getValue(fraction);
    }

    /**
     * 设置 属性名
     *
     * @param propertyName propertyName
     */
    public void setPropertyName(String propertyName) {
        mPropertyName = propertyName;
    }

    /**
     * 设置 属性
     *
     * @param property property
     */
    public void setProperty(Property property) {
        mProperty = property;
    }

    /**
     * 获取 属性名
     *
     * @return String
     */
    public String getPropertyName() {
        return mPropertyName;
    }

    /**
     * 获取 动画 值
     *
     * @return
     */
    Object getAnimatedValue() {
        return mAnimatedValue;
    }

    /**
     * 附加了 属性名 和 每一祯的数据
     *
     * @return String
     */
    @Override
    public String toString() {
        return mPropertyName + ": " + mKeyframeSet.toString();
    }

    /**
     * 工具方法 获取 方法名字
     *
     * @param prefix       prefix
     * @param propertyName propertyName
     * @return String
     */
    static String getMethodName(String prefix, String propertyName) {
        // 此时 Property 没有 name
        if (propertyName == null || propertyName.length() == 0) {
            return prefix;
        }
        // 拿到 propertyName 的第一个大写字母
        char firstLetter = Character.toUpperCase(propertyName.charAt(0));
        // 剩下的字母再取出来
        String theRest = propertyName.substring(1);
        // 重新拼接
        return prefix + firstLetter + theRest;
    }

    /**
     * KeyframeSet = IntKeyframeSet
     * IntPropertyValuesHolder
     */
    static class IntPropertyValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Integer>> sJNISetterPropertyMap = new HashMap<Class, HashMap<String, Integer>>();
        private IntProperty mIntProperty;

        IntKeyframeSet mIntKeyframeSet;
        int mIntAnimatedValue;

        /**
         * IntPropertyValuesHolder 构造方法
         *
         * @param propertyName propertyName
         * @param keyframeSet  keyframeSet
         */
        public IntPropertyValuesHolder(String propertyName, IntKeyframeSet keyframeSet) {
            super(propertyName);
            mValueType = int.class;
            mKeyframeSet = keyframeSet;
            mIntKeyframeSet = (IntKeyframeSet) mKeyframeSet;
        }

        /**
         * IntPropertyValuesHolder 构造方法
         *
         * @param property    property
         * @param keyframeSet keyframeSet
         */
        public IntPropertyValuesHolder(Property property, IntKeyframeSet keyframeSet) {
            super(property);
            mValueType = int.class;
            mKeyframeSet = keyframeSet;
            mIntKeyframeSet = (IntKeyframeSet) mKeyframeSet;
            if (property instanceof IntProperty) {
                mIntProperty = (IntProperty) mProperty;
            }
        }

        /**
         * IntPropertyValuesHolder 构造方法
         *
         * @param propertyName propertyName
         * @param values       values
         */
        public IntPropertyValuesHolder(String propertyName, int... values) {
            super(propertyName);
            setIntValues(values);
        }

        /**
         * IntPropertyValuesHolder 构造方法
         *
         * @param property property
         * @param values   values
         */
        public IntPropertyValuesHolder(Property property, int... values) {
            super(property);
            setIntValues(values);
            if (property instanceof IntProperty) {
                mIntProperty = (IntProperty) mProperty;
            }
        }

        /**
         * 覆写 设置 int 数据
         *
         * @param values values
         */
        @Override
        public void setIntValues(int... values) {
            super.setIntValues(values);
            mIntKeyframeSet = (IntKeyframeSet) mKeyframeSet;
        }

        /**
         * 覆写 计算 值
         *
         * @param fraction fraction
         */
        @Override
        void calculateValue(float fraction) {
            mIntAnimatedValue = mIntKeyframeSet.getIntValue(fraction);
        }

        /**
         * 覆写 获取 动画 值
         *
         * @return Object
         */
        @Override
        Object getAnimatedValue() {
            return mIntAnimatedValue;
        }

        /**
         * 覆写 IntPropertyValuesHolder 克隆方法
         *
         * @return IntPropertyValuesHolder
         */
        @Override
        public IntPropertyValuesHolder clone() {
            IntPropertyValuesHolder newPVH = (IntPropertyValuesHolder) super.clone();
            newPVH.mIntKeyframeSet = (IntKeyframeSet) newPVH.mKeyframeSet;
            return newPVH;
        }

        /**
         * 覆写 设置 动画 值
         *
         * @param target target
         */
        @SuppressWarnings("unchecked")
        @Override
        void setAnimatedValue(Object target) {
            if (mIntProperty != null) {
                mIntProperty.setValue(target, mIntAnimatedValue);
                return;
            }
            if (mProperty != null) {
                mProperty.set(target, mIntAnimatedValue);
                return;
            }
            if (mSetter != null) {
                try {
                    mTmpValueArray[0] = mIntAnimatedValue;
                    // 反射调用 setter 设置 值
                    mSetter.invoke(target, mTmpValueArray);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 覆写 setter 方法
         *
         * @param targetClass targetClass
         */
        @Override
        void setupSetter(Class targetClass) {
            if (mProperty != null) {
                return;
            }

            super.setupSetter(targetClass);
        }
    }

    /**
     * KeyframeSet = FloatKeyframeSet
     * FloatPropertyValuesHolder
     */
    static class FloatPropertyValuesHolder extends PropertyValuesHolder {

        private static final HashMap<Class, HashMap<String, Integer>> sJNISetterPropertyMap = new HashMap<Class, HashMap<String, Integer>>();
        private FloatProperty10 mFloatProperty;

        FloatKeyframeSet mFloatKeyframeSet;
        float mFloatAnimatedValue;

        /**
         * FloatPropertyValuesHolder 构造方法
         *
         * @param propertyName propertyName
         * @param keyframeSet  keyframeSet
         */
        public FloatPropertyValuesHolder(String propertyName, FloatKeyframeSet keyframeSet) {
            super(propertyName);
            mValueType = float.class;
            mKeyframeSet = keyframeSet;
            mFloatKeyframeSet = (FloatKeyframeSet) mKeyframeSet;
        }

        /**
         * FloatPropertyValuesHolder 构造方法
         *
         * @param property    property
         * @param keyframeSet keyframeSet
         */
        public FloatPropertyValuesHolder(Property property, FloatKeyframeSet keyframeSet) {
            super(property);
            mValueType = float.class;
            mKeyframeSet = keyframeSet;
            mFloatKeyframeSet = (FloatKeyframeSet) mKeyframeSet;
            if (property instanceof FloatProperty10) {
                mFloatProperty = (FloatProperty10) mProperty;
            }
        }

        /**
         * FloatPropertyValuesHolder 构造方法
         *
         * @param propertyName propertyName
         * @param values       values
         */
        public FloatPropertyValuesHolder(String propertyName, float... values) {
            super(propertyName);
            setFloatValues(values);
        }

        /**
         * FloatPropertyValuesHolder 构造方法
         *
         * @param property property
         * @param values   values
         */
        public FloatPropertyValuesHolder(Property property, float... values) {
            super(property);
            setFloatValues(values);
            if (property instanceof FloatProperty10) {
                mFloatProperty = (FloatProperty10) mProperty;
            }
        }

        /**
         * 覆写 设置 float 数据
         *
         * @param values values
         */
        @Override
        public void setFloatValues(float... values) {
            super.setFloatValues(values);
            mFloatKeyframeSet = (FloatKeyframeSet) mKeyframeSet;
        }

        /**
         * 覆写 计算 动画 值
         *
         * @param fraction fraction
         */
        @Override
        void calculateValue(float fraction) {
            mFloatAnimatedValue = mFloatKeyframeSet.getFloatValue(fraction);
        }

        /**
         * 覆写 获取 动画 值
         *
         * @return Object
         */
        @Override
        Object getAnimatedValue() {
            return mFloatAnimatedValue;
        }

        /**
         * 覆写 FloatPropertyValuesHolder 克隆方法
         *
         * @return FloatPropertyValuesHolder
         */
        @Override
        public FloatPropertyValuesHolder clone() {
            FloatPropertyValuesHolder newPVH = (FloatPropertyValuesHolder) super.clone();
            newPVH.mFloatKeyframeSet = (FloatKeyframeSet) newPVH.mKeyframeSet;
            return newPVH;
        }

        /**
         * 覆写 设置 动画 值
         *
         * @param target target
         */
        @SuppressWarnings("unchecked")
        @Override
        void setAnimatedValue(Object target) {
            if (mFloatProperty != null) {
                mFloatProperty.setValue(target, mFloatAnimatedValue);
                return;
            }
            if (mProperty != null) {
                mProperty.set(target, mFloatAnimatedValue);
                return;
            }
            if (mSetter != null) {
                try {
                    mTmpValueArray[0] = mFloatAnimatedValue;
                    // 反射调用 setter 设置 值
                    mSetter.invoke(target, mTmpValueArray);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 覆写 setter 方法
         *
         * @param targetClass targetClass
         */
        @Override
        void setupSetter(Class targetClass) {
            if (mProperty != null) {
                return;
            }
            super.setupSetter(targetClass);
        }
    }
}