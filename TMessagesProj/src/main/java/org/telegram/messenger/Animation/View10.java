/*
 Copyright 2012 Jake Wharton

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.telegram.messenger.Animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * A proxy class to allow for modifying post-3.0 view properties on all pre-3.0
 * platforms. <strong>DO NOT</strong> wrap your views with this class if you
 * are using {@code ObjectAnimator} as it will handle that itself.
 * View10 的原型是 Jake Wharton 的 NineOldAndroids 项目里的 AnimatorProxy。
 * 它是一个代理类，允许所有在Android3.0前的平台修改3.0的View属性。
 * 如果你正在使用ObjectAnimator，不要将你的View和这个代理类包裹起来，因为它会处理本身。
 */
public class View10 extends Animation {
    /**
     * Whether or not the current running platform needs to be proxied.
     * 判断当前运行的系统版本是否需要代理 （ 是否是3.0以下的系统 ）
     */
    public static boolean NEED_PROXY = Build.VERSION.SDK_INT < 11;

    // 缓存 View 和 View的代理类 （ View10 ）
    private static final WeakHashMap<View, View10> PROXIES = new WeakHashMap<>();

    /**
     * Create a proxy to allow for modifying post-3.0 view properties on all
     * pre-3.0 platforms. <strong>DO NOT</strong> wrap your views if you are
     * using {@code ObjectAnimator} as it will handle that itself.
     * 创建一个代理，允许所有在Android3.0前的平台修改3.0的View属性。
     * 如果你正在使用ObjectAnimator，不要将你的View和这个代理类包裹起来，因为它会处理本身。
     *
     * @param view View to wrap.
     * @return Proxy to post-3.0 properties. 代理后， 3.0的属性
     */
    public static View10 wrap(View view) {
        // 从缓存中查找 View 的对应 代理类
        View10 proxy = PROXIES.get(view);
        // 获取 View 设置上的 Animation
        Animation animation = view.getAnimation();
        // This checks if the proxy already exists and whether it still is the animation of the given view
        // 检查代理类是否存在 && 是否已经设置上了代理 || 是否存在动画
        if (proxy == null || proxy != animation && animation != null) {
            // 根据View，创建一个代理类
            proxy = new View10(view);
            // 加入到缓存中
            PROXIES.put(view, proxy);
        } else if (animation == null) {
            // 给 View 设置上 动画
            view.setAnimation(proxy);
        }
        // 返回代理
        return proxy;
    }

    private final WeakReference<View> mView;
    // 实例化 一个 Camera
    private final Camera mCamera = new Camera();
    // 标记 是否设置了 Pivot 相关值
    private boolean mHasPivot;

    private float mAlpha = 1;
    private float mPivotX;
    private float mPivotY;
    private float mRotationX;
    private float mRotationY;
    private float mRotationZ;
    private float mScaleX = 1;
    private float mScaleY = 1;
    private float mTranslationX;
    private float mTranslationY;

    private final RectF mBefore = new RectF();
    private final RectF mAfter = new RectF();
    private final Matrix mTempMatrix = new Matrix();

    private View10(View view) {
        // perform transformation immediately
        // 设置持续时间
        setDuration(0);
        // persist transformation beyond duration
        // 在持续时间内，持续执行
        setFillAfter(true);
        // 设置上 代理
        view.setAnimation(this);
        // 用 WeakReference 缓存 该 View
        mView = new WeakReference<>(view);
    }

    /**
     * 获取 透明度
     *
     * @return float
     */
    public float getAlpha() {
        return mAlpha;
    }

    /**
     * 设置 透明度
     *
     * @param alpha alpha
     */
    public void setAlpha(float alpha) {
        // 如果已经设置上的透明度
        if (mAlpha != alpha) {
            // 设置透明度
            mAlpha = alpha;
            // 从缓存中获取 View
            View view = mView.get();
            // 如果存在 View 缓存
            if (view != null) {
                // 调用 invalidate 重绘 View
                view.invalidate();
            }
        }
    }

    /**
     * 获取 pivotX 的值
     *
     * @return float
     */
    public float getPivotX() {
        return mPivotX;
    }

    /**
     * 设置 pivotX 的值
     *
     * @param pivotX pivotX
     */
    public void setPivotX(float pivotX) {
        // 如果 mHasPivot 没设上标记 || PivotX 前后两次的值不相等
        if (!mHasPivot || mPivotX != pivotX) {
            // 准备更新
            prepareForUpdate();
            // 标记 已设置上 Pivot 值
            mHasPivot = true;
            // 设置上 PivotX 值
            mPivotX = pivotX;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 pivotY 的值
     *
     * @return float
     */
    public float getPivotY() {
        return mPivotY;
    }

    /**
     * 设置 pivotY 的值
     *
     * @param pivotY pivotY
     */
    public void setPivotY(float pivotY) {
        // 如果 mHasPivot 没设上标记 || PivotY 前后两次的值不相等
        if (!mHasPivot || mPivotY != pivotY) {
            // 准备更新
            prepareForUpdate();
            // 标记 已设置上 Pivot 值
            mHasPivot = true;
            // 设置上 PivotY 值
            mPivotY = pivotY;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 旋转 角度
     *
     * @return float
     */
    public float getRotation() {
        return mRotationZ;
    }

    /**
     * 设置 旋转 角度
     *
     * @param rotation rotation
     */
    public void setRotation(float rotation) {
        // rotationZ 前后两次的值不相等
        if (mRotationZ != rotation) {
            // 准备更新
            prepareForUpdate();
            // 设置上 rotationZ 值
            mRotationZ = rotation;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 X轴旋转 角度
     *
     * @return float
     */
    public float getRotationX() {
        return mRotationX;
    }

    /**
     * 设置 X轴旋转 角度
     *
     * @param rotationX rotationX
     */
    public void setRotationX(float rotationX) {
        // rotationX 前后两次的值不相等
        if (mRotationX != rotationX) {
            // 准备更新
            prepareForUpdate();
            // 设置上 rotationX 值
            mRotationX = rotationX;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 Y轴旋转 角度
     *
     * @return float
     */
    public float getRotationY() {
        return mRotationY;
    }

    /**
     * 设置 Y轴旋转 角度
     *
     * @param rotationY rotationY
     */
    public void setRotationY(float rotationY) {
        // rotationY 前后两次的值不相等
        if (mRotationY != rotationY) {
            // 准备更新
            prepareForUpdate();
            // 设置上 rotationY 值
            mRotationY = rotationY;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 X轴 缩放值
     *
     * @return float
     */
    public float getScaleX() {
        return mScaleX;
    }

    /**
     * 设置 X轴 缩放值
     *
     * @param scaleX scaleX
     */
    public void setScaleX(float scaleX) {
        // scaleX 前后两次的值不相等
        if (scaleX != scaleX) {
            // 准备更新
            prepareForUpdate();
            // 设置上 scaleX 值
            mScaleX = scaleX;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 Y轴 缩放值
     *
     * @return float
     */
    public float getScaleY() {
        return mScaleY;
    }

    /**
     * 设置 Y轴 缩放值
     *
     * @param scaleY scaleY
     */
    public void setScaleY(float scaleY) {
        // scaleY 前后两次的值不相等
        if (mScaleY != scaleY) {
            // 准备更新
            prepareForUpdate();
            // 设置上 scaleY 值
            mScaleY = scaleY;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 X轴 滚动值
     *
     * @return int
     */
    public int getScrollX() {
        // 获取 缓存View
        View view = mView.get();
        if (view == null) {
            return 0;
        }
        return view.getScrollX();
    }

    /**
     * 设置 X轴 滚动值
     *
     * @param value value
     */
    public void setScrollX(int value) {
        // 获取 缓存View
        View view = mView.get();
        if (view != null) {
            view.scrollTo(value, view.getScrollY());
        }
    }

    /**
     * 获取 Y轴 滚动值
     *
     * @return int
     */
    public int getScrollY() {
        // 获取 缓存View
        View view = mView.get();
        if (view == null) {
            return 0;
        }
        return view.getScrollY();
    }

    /**
     * 设置 Y轴 滚动值
     *
     * @param value value
     */
    public void setScrollY(int value) {
        // 获取 缓存View
        View view = mView.get();
        if (view != null) {
            view.scrollTo(view.getScrollX(), value);
        }
    }

    /**
     * 获取 X轴 偏移量
     *
     * @return float
     */
    public float getTranslationX() {
        return mTranslationX;
    }

    /**
     * 设置 X轴 偏移量
     *
     * @param translationX translationX
     */
    public void setTranslationX(float translationX) {
        // translationX 前后两次的值不相等
        if (mTranslationX != translationX) {
            // 准备更新
            prepareForUpdate();
            // 设置上 translationX 值
            mTranslationX = translationX;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 Y轴 偏移量
     *
     * @return float
     */
    public float getTranslationY() {
        return mTranslationY;
    }

    /**
     * 设置 Y轴 偏移量
     *
     * @param translationY translationY
     */
    public void setTranslationY(float translationY) {
        // translationY 前后两次的值不相等
        if (mTranslationY != translationY) {
            // 准备更新
            prepareForUpdate();
            // 设置上 translationY 值
            mTranslationY = translationY;
            // 更新后重绘
            invalidateAfterUpdate();
        }
    }

    /**
     * 获取 X 坐标
     *
     * @return float
     */
    public float getX() {
        // 获取 缓存View
        View view = mView.get();
        if (view == null) {
            return 0;
        }
        return view.getLeft() + mTranslationX;
    }

    /**
     * 设置 X 坐标
     *
     * @param x x
     */
    public void setX(float x) {
        // 获取 缓存View
        View view = mView.get();
        if (view != null) {
            setTranslationX(x - view.getLeft());
        }
    }

    /**
     * 获取 Y 坐标
     *
     * @return float
     */
    public float getY() {
        // 获取 缓存View
        View view = mView.get();
        if (view == null) {
            return 0;
        }
        return view.getTop() + mTranslationY;
    }

    /**
     * 设置 Y 坐标
     *
     * @param y y
     */
    public void setY(float y) {
        // 获取 缓存View
        View view = mView.get();
        if (view != null) {
            setTranslationY(y - view.getTop());
        }
    }

    /**
     * 准备更新
     */
    private void prepareForUpdate() {
        // 获取 缓存View
        View view = mView.get();
        if (view != null) {
            // 计算矩形
            computeRect(mBefore, view);
        }
    }

    /**
     * 更新后重绘
     */
    private void invalidateAfterUpdate() {
        // 获取 缓存View
        View view = mView.get();
        // 如果 缓存View 不存在 或者 缓存View 是最外层的Layout
        if (view == null || view.getParent() == null) {
            return;
        }

        // 拿到 invalidateAfterUpdate方法 操作过的 矩形缓存
        final RectF after = mAfter;
        // 计算该缓存矩形
        computeRect(after, view);
        // 包含 mBefore 矩形
        after.union(mBefore);

        // 重回 父容器
        ((View) view.getParent()).invalidate(
                (int) Math.floor(after.left),
                (int) Math.floor(after.top),
                (int) Math.ceil(after.right),
                (int) Math.ceil(after.bottom));
    }

    /**
     * 计算矩形
     *
     * @param r    r
     * @param view view
     */
    private void computeRect(final RectF r, View view) {
        // compute current rectangle according to matrix transformation
        // 根据矩阵变换计算当前矩形

        // 获取 缓存View 的 宽度
        final float w = view.getWidth();
        // 获取 缓存View 的 高度
        final float h = view.getHeight();

        // use a rectangle at 0,0 to make sure we don't run into issues with scaling
        // 使用矩形0,0,以确保我们不遇到问题

        // 重新设置 传入进来的 RectF 从 x=0 y=0 位置绘制
        r.set(0, 0, w, h);

        // 先拿到 临时矩阵
        final Matrix m = mTempMatrix;
        // 先重置 临时矩阵
        m.reset();
        // 变换矩形
        transformMatrix(m, view);
        //
        mTempMatrix.mapRect(r);
        // 设置
        r.offset(view.getLeft(), view.getTop());

        // Straighten coords if rotations flipped them
        // 如果旋转翻转它们伸直坐标
        // 如果旋转了，则left和right互换
        if (r.right < r.left) {
            final float f = r.right;
            r.right = r.left;
            r.left = f;
        }
        // 如果旋转了，top和bottom互换
        if (r.bottom < r.top) {
            final float f = r.top;
            r.top = r.bottom;
            r.bottom = f;
        }
    }

    /**
     * 工具方法 将 Matrix 应用到 View 中
     *
     * @param m    m
     * @param view view
     */
    private void transformMatrix(Matrix m, View view) {
        // 获取 view 的 宽度
        final float w = view.getWidth();
        // 获取 view 的 高度
        final float h = view.getHeight();
        // 获取 mHasPivot 标记 是否设置了 Pivot 相关值
        final boolean hasPivot = mHasPivot;
        // 获取 PivotX 的值
        final float pX = hasPivot ? mPivotX : w / 2f;
        // 获取 PivotY 的值
        final float pY = hasPivot ? mPivotY : h / 2f;

        // 获取 RotationX 的值
        final float rX = mRotationX;
        // 获取 RotationY 的值
        final float rY = mRotationY;
        // 获取 RotationZ 的值
        final float rZ = mRotationZ;
        // 只要有一个 Rotation 值
        if ((rX != 0) || (rY != 0) || (rZ != 0)) {
            // 获取 已经实例的 Camera
            final Camera camera = mCamera;
            // 保存 Camera 状态
            camera.save();
            // 进行 旋转
            camera.rotateX(rX);
            camera.rotateY(rY);
            camera.rotateZ(-rZ);
            // 设置 矩阵
            camera.getMatrix(m);
            // 恢复 Camera 状态
            camera.restore();

            // 设置 Preconcats 矩阵指定转换
            // Matrix 调用native进行预转换
            m.preTranslate(-pX, -pY);
            // Matrix 调用native进行转换
            m.postTranslate(pX, pY);
        }

        // 获取 X轴 缩放值
        final float sX = mScaleX;
        // 获取 Y轴 缩放值
        final float sY = mScaleY;
        // 如果 两个 缩放值 只要不为1.0 （ 就是什么都不改变 ）
        if ((sX != 1.0f) || (sY != 1.0f)) {
            // Matrix 调用native进行缩放
            m.postScale(sX, sY);
            final float sPX = -(pX / w) * ((sX * w) - w);
            final float sPY = -(pY / h) * ((sY * h) - h);
            // Matrix 调用native进行转换
            m.postTranslate(sPX, sPY);
        }
        // Matrix 调用native进行转换
        m.postTranslate(mTranslationX, mTranslationY);
    }

    /**
     * 覆写 Animation.applyTransformation
     * 能拿到动画 的 Transformation
     *
     * @param interpolatedTime interpolatedTime 规范化的价值时间(0.0 - 1.0)后通过插值函数运行。
     * @param t                t Transformation对象去填充当前的transforms
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        // 获取 缓存View
        View view = mView.get();
        // 如果存在缓存View
        if (view != null) {
            // Transformation 设置上透明度
            t.setAlpha(mAlpha);
            // 调用工具方法 将 Transformation.Matrix 应用到 View 中
            transformMatrix(t.getMatrix(), view);
        }
    }
}
