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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Android 19 以上的 AnimatorSet
 */
public final class AnimatorSet10 extends Animator10 {

    // 缓存正在播放的动画
    private ArrayList<Animator10> mPlayingSet = new ArrayList<>();

    // 缓存 Animator10 和 Node 的关系
    private HashMap<Animator10, Node> mNodeMap = new HashMap<>();

    // 缓存所有节点
    private ArrayList<Node> mNodes = new ArrayList<>();

    // 缓存排序后的节点
    private ArrayList<Node> mSortedNodes = new ArrayList<>();

    // 是否需要排序
    private boolean mNeedsSort = true;

    // AnimatorSet10 的回调接口
    private AnimatorSetListener mSetListener = null;

    // 是否终止
    boolean mTerminated = false;

    // 是否开始
    private boolean mStarted = false;

    // 开始延迟时间
    private long mStartDelay = 0;

    // 延迟动画
    private ValueAnimator mDelayAnim = null;

    // 持续时间
    private long mDuration = -1;

    // 动画插入器
    private Interpolator mInterpolator = null;

    /**
     * 动画数组里的动画同时执行
     *
     * @param items items
     */
    public void playTogether(Animator10... items) {
        // 如果 动画数组 不为空
        if (items != null) {
            // 标记为需要排序
            mNeedsSort = true;
            /*
             * 拿到第一个 动画 作为 Builder
             * 剩下的 动画 只需要调用 Builder.with 让
             * 动画数组里的动画 同时执行
             */
            Builder builder = play(items[0]);
            /*
             * 拿到每个动画
             * 调用Builder.with
             * 设置上动画同时执行
             */
            for (int i = 1; i < items.length; ++i) {
                builder.with(items[i]);
            }
        }
    }

    /**
     * 动画集合里的动画同时执行
     *
     * @param items items
     */
    public void playTogether(Collection<Animator10> items) {
        /*
         * 如果 动画集合 不为空
         * 并且 动画集合 长度大于0
         */
        if (items != null && items.size() > 0) {
            // 标记为需要排序
            mNeedsSort = true;
            Builder builder = null;
            /*
             * 拿到每个 动画
             * 如果第一个动画都没拿到
             * 即 builder = null
             * 需要 这是上 builder的值
             * 否则 存在的话 就代表不是集合的第一个数据
             * builder存在的话
             * 就可以直接with
             */
            for (Animator10 anim : items) {
                if (builder == null) {
                    builder = play(anim);
                } else {
                    builder.with(anim);
                }
            }
        }
    }

    /**
     * 按照动画数组的顺序
     * 顺序播放动画
     *
     * @param items items
     */
    public void playSequentially(Animator10... items) {
        // 如果数组不为空
        if (items != null) {
            // 标记为需要排序
            mNeedsSort = true;
            /*
             * 如果动画数组里只有一个动画
             * 则 就直接调用 play 一次就可以了
             */
            if (items.length == 1) {
                play(items[0]);
            } else {
                /*
                 * 遍历 动画数组
                 * 当前 索引 作为 play
                 * 索引+1 作为 before
                 * play:0 before:1
                 * play:1 before:2
                 * play:2 before:3
                 * play:3 before:4
                 * ...    ...
                 */
                for (int i = 0; i < items.length - 1; ++i) {
                    play(items[i]).before(items[i + 1]);
                }
            }
        }
    }

    /**
     * 按照动画集合的顺序
     * 顺序播放动画
     *
     * @param items items
     */
    public void playSequentially(List<Animator10> items) {
        /*
         * 如果 集合 不为空
         * 并且 集合 长度大于0
         */
        if (items != null && items.size() > 0) {
            // 标记为需要排序
            mNeedsSort = true;
            /*
             * 如果动画数组里只有一个动画
             * 则 就直接调用 play 一次就可以了
             */
            if (items.size() == 1) {
                play(items.get(0));
            } else {
                /*
                 * 遍历 动画集合
                 * 当前 索引 作为 play
                 * 索引+1 作为 before
                 * play:0 before:1
                 * play:1 before:2
                 * play:2 before:3
                 * play:3 before:4
                 * ...    ...
                 */
                for (int i = 0; i < items.size() - 1; ++i) {
                    play(items.get(i)).before(items.get(i + 1));
                }
            }
        }
    }

    /**
     * 拿到 该 AnimatorSet10 的所有缓存Node数据
     *
     * @return ArrayList<Animator10>
     */
    public ArrayList<Animator10> getChildAnimations() {
        ArrayList<Animator10> childList = new ArrayList<>();
        for (Node node : mNodes) {
            childList.add(node.animation);
        }
        return childList;
    }

    /**
     * 设置 Target 数据
     *
     * @param target target
     */
    @Override
    public void setTarget(Object target) {
        // 遍历 所有 Node 缓存数据
        for (Node node : mNodes) {
            // 拿到 每个 Node 的 Animator10 对象
            Animator10 animation = node.animation;
            /*
             * 如果 Node 数据的 Animator10 对象类型属于
             * AnimatorSet10 或者 ObjectAnimator10
             */
            if (animation instanceof AnimatorSet10) {
                animation.setTarget(target);
            } else if (animation instanceof ObjectAnimator10) {
                animation.setTarget(target);
            }
        }
    }

    /**
     * 给 AnimatorSet10 设置上 插入器
     *
     * @param interpolator interpolator
     */
    @Override
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * 拿到 该 AnimatorSet10 的插入器
     *
     * @return Interpolator
     */
    @Override
    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    /**
     * 拿到 Animator10 播放动画
     * 初始化 Builder
     *
     * @param anim anim
     * @return Builder
     */
    public Builder play(Animator10 anim) {
        // 如果动画不为空
        if (anim != null) {
            // 标记为需要排序
            mNeedsSort = true;
            // 返回一个 新的 Builder
            return new Builder(anim);
        }
        return null;
    }

    /**
     * 动画取消
     */
    @SuppressWarnings("unchecked")
    @Override
    public void cancel() {
        // 标记为终止
        mTerminated = true;
        // 如果 动画 已经开始了
        if (isStarted()) {
            ArrayList<AnimatorListener> tmpListeners = null;
            // 并且 缓存 AnimatorListener 集合 不为空
            if (mListeners != null) {
                // 克隆一份 缓存 AnimatorListener 集合数据
                tmpListeners = (ArrayList<AnimatorListener>) mListeners.clone();
                /*
                 * 然后遍历AnimatorListener
                 * 拿到 AnimatorListener
                 * 调用其回调 AnimatorListener.onAnimationCancel
                 */
                for (AnimatorListener listener : tmpListeners) {
                    listener.onAnimationCancel(this);
                }
            }
            /*
             * 如果 延迟动画 不为空
             * 并且 延迟动画 正在运行
             */
            if (mDelayAnim != null && mDelayAnim.isRunning()) {
                // 调用延迟动画（ ValueAnimator ）的cancel
                mDelayAnim.cancel();
            } else if (mSortedNodes.size() > 0) {
                /*
                 * 遍历 缓存排序后的节点
                 * 逐个调用其cancel逻辑
                 */
                for (Node node : mSortedNodes) {
                    node.animation.cancel();
                }
            }
            // 缓存 AnimatorListener 集合数据 不为空
            if (tmpListeners != null) {
                /*
                 * 然后遍历AnimatorListener
                 * 拿到 AnimatorListener
                 * 调用其回调 AnimatorListener.onAnimationEnd
                 */
                for (AnimatorListener listener : tmpListeners) {
                    listener.onAnimationEnd(this);
                }
            }
            // 标记为没有开始
            mStarted = false;
        }
    }

    /**
     * 动画结束
     */
    @SuppressWarnings("unchecked")
    @Override
    public void end() {
        // 标记为终止
        mTerminated = true;
        // 如果 动画 已经开始了
        if (isStarted()) {
            // 如果排序节点集合长度 不等于 节点集合长度
            if (mSortedNodes.size() != mNodes.size()) {
                // hasn't been started yet - sort the nodes now, then end them
                // 还没有开始-现在对节点进行排序,然后结束
                sortNodes();
                // 拿到每个 排序后的 节点
                for (Node node : mSortedNodes) {
                    if (mSetListener == null) {
                        mSetListener = new AnimatorSetListener(this);
                    }
                    // 逐个设置上 AnimatorSetListener 回调方法
                    node.animation.addListener(mSetListener);
                }
            }
            // 如果 延迟动画 不为 空
            if (mDelayAnim != null) {
                // 调用 延迟动画 的 cancel 逻辑
                mDelayAnim.cancel();
            }
            // 如果 排序节点的长度 大于 0
            if (mSortedNodes.size() > 0) {
                // 再次拿到每个 排序后的 节点
                for (Node node : mSortedNodes) {
                    // 调用每个节点 Animator10 的 end 回调
                    node.animation.end();
                }
            }
            // 如果改 AnimatorSet10 的 缓存 AnimatorListener 集合 为空
            if (mListeners != null) {
                // 备份一份 缓存 AnimatorListener 集合 到临时集合里
                ArrayList<AnimatorListener> tmpListeners = (ArrayList<AnimatorListener>) mListeners.clone();
                // 再操作临时集合
                for (AnimatorListener listener : tmpListeners) {
                    /*
                     * 拿到临时集合里的 每一个 AnimatorListener
                     * 调用其 onAnimationEnd 回调方法
                     */
                    listener.onAnimationEnd(this);
                }
            }
            // 标记为没开始
            mStarted = false;
        }
    }

    /**
     * 动画是否在运行
     *
     * @return boolean
     */
    @Override
    public boolean isRunning() {
        /*
         * 遍历所有节点
         * 只要有一个节点里的动画在运行
         * 直接返回true
         */
        for (Node node : mNodes) {
            if (node.animation.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 动画是否开始
     *
     * @return boolean
     */
    @Override
    public boolean isStarted() {
        return mStarted;
    }

    /**
     * 获取动画延迟时间
     *
     * @return long
     */
    @Override
    public long getStartDelay() {
        return mStartDelay;
    }

    /**
     * 设置 动画 开始延迟时间
     *
     * @param startDelay startDelay
     */
    @Override
    public void setStartDelay(long startDelay) {
        mStartDelay = startDelay;
    }

    /**
     * 获取动画持续时间
     *
     * @return long
     */
    @Override
    public long getDuration() {
        return mDuration;
    }

    /**
     * 设置 动画 持续时间
     *
     * @param duration duration
     * @return AnimatorSet10
     */
    @Override
    public AnimatorSet10 setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be a value of zero or greater");
        }
        mDuration = duration;
        return this;
    }

    /**
     * 设置 每个 节点动画( ValueAnimator )的 开始值
     */
    @Override
    public void setupStartValues() {
        for (Node node : mNodes) {
            node.animation.setupStartValues();
        }
    }

    /**
     * 设置 每个 节点动画( ValueAnimator )的 结束值
     */
    @Override
    public void setupEndValues() {
        for (Node node : mNodes) {
            node.animation.setupEndValues();
        }
    }

    /**
     *
     */
    @Override
    public void pause() {
        /*
         * 拿到 之前的 暂停标记， 正常逻辑都在false
         * 因为现在才要暂停 如果标记显示true
         * 肯定是 “炸” 了
         */
        boolean previouslyPaused = mPaused;
        /*
         * 此时走 Animator10 的 暂停逻辑
         * 会给 mPaused 设置上 true
         * 并且执行对应的暂停逻辑处理
         */
        super.pause();

        /*
         * 之前标记为 false
         * 并且执行了Animator10 的 暂停逻辑
         */
        if (!previouslyPaused && mPaused) {
            // 如果 延迟动画 不为空
            if (mDelayAnim != null) {
                // 调用 延迟动画的 pause 逻辑
                mDelayAnim.pause();
            } else {
                // 逐个拿到所有节点 调用其Animator10 回调 pause 逻辑
                for (Node node : mNodes) {
                    node.animation.pause();
                }
            }
        }
    }

    /**
     * 动画恢复
     */
    @Override
    public void resume() {
        /*
         * 拿到 之前的 暂停标记， 正常逻辑都在true
         * 因为现在才要恢复 如果标记显示false
         * 肯定是 “炸” 了
         */
        boolean previouslyPaused = mPaused;
        /*
         * 此时走 Animator10 的 恢复逻辑
         * 会给 mPaused 设置上 false
         * 并且执行对应的恢复逻辑处理
         */
        super.resume();
        /*
         * 之前标记为 true
         * 并且执行了Animator10 的 恢复逻辑
         */
        if (previouslyPaused && !mPaused) {
            // 如果 延迟动画 不为空
            if (mDelayAnim != null) {
                // 调用 延迟动画的 resume 逻辑
                mDelayAnim.resume();
            } else {
                // 逐个拿到所有节点 调用其Animator10 回调 resume 逻辑
                for (Node node : mNodes) {
                    node.animation.resume();
                }
            }
        }
    }

    /**
     * 动画开始
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        // 标记为没终止
        mTerminated = false;
        // 标记为开始
        mStarted = true;
        // 标记为没暂停
        mPaused = false;

        // 如果持续时间 大于 0
        if (mDuration >= 0) {
            // 给所有节点动画设置上 持续时间
            for (Node node : mNodes) {
                node.animation.setDuration(mDuration);
            }
        }

        // 如果插入器 不为 空
        if (mInterpolator != null) {
            // 给所有节点动画设置上 插入器
            for (Node node : mNodes) {
                node.animation.setInterpolator(mInterpolator);
            }
        }

        // 执行排序节点逻辑
        sortNodes();

        int numSortedNodes = mSortedNodes.size();
        // 遍历 排序后节点集合
        for (Node node : mSortedNodes) {
            // 拿到每个节点的 ArrayList<AnimatorListener>
            ArrayList<AnimatorListener> oldListeners = node.animation.getListeners();
            /*
             * 如果 ArrayList<AnimatorListener> 长度 大于 0
             */
            if (oldListeners != null && oldListeners.size() > 0) {
                // 先克隆一份
                final ArrayList<AnimatorListener> clonedListeners = new
                        ArrayList<>(oldListeners);
                // 然后遍历克隆样本
                for (AnimatorListener listener : clonedListeners) {
                    /*
                     * 如果 每个 AnimatorListener 属于
                     * DependencyListener类型 或者 AnimatorSetListener类型
                     * 从节点Animator10中删除改 AnimatorListener
                     */
                    if (listener instanceof DependencyListener ||
                            listener instanceof AnimatorSetListener) {
                        node.animation.removeListener(listener);
                    }
                }
            }
        }

        //
        final ArrayList<Node> nodesToStart = new ArrayList<>();
        // 再次遍历 排序后节点集合
        for (Node node : mSortedNodes) {
            /*
             * 如果 AnimatorSetListener 为 null
             * 则初始化
             */
            if (mSetListener == null) {
                mSetListener = new AnimatorSetListener(this);
            }
            // 如果节点的 依赖信息为空 或者 长度为0
            if (node.dependencies == null || node.dependencies.size() == 0) {
                // 添加要 nodesToStart 集合里
                nodesToStart.add(node);
            } else {
                int numDependencies = node.dependencies.size();
                for (int j = 0; j < numDependencies; ++j) {
                    // 拿到该节点的 每一个依赖信息
                    Dependency dependency = node.dependencies.get(j);
                    /*
                     * 给依赖信息中包裹的 节点
                     * 设置上动画监听( DependencyListener )
                     */
                    dependency.node.animation.addListener(
                            new DependencyListener(this, node, dependency.rule));
                }
                // 克隆一份 依赖信息 数据 放到 tmpDependencies里
                node.tmpDependencies = (ArrayList<Dependency>) node.dependencies.clone();
            }
            // 最后给该节点设置上 AnimatorSetListener
            node.animation.addListener(mSetListener);
        }

        // 如果开始延迟时间 小于等于0
        if (mStartDelay <= 0) {
            // 遍历 nodesToStart 集合
            for (Node node : nodesToStart) {
                // 每个节点动画 执行 start 逻辑
                node.animation.start();
                // 将该动画 缓存到 正在播放的动画集合
                mPlayingSet.add(node.animation);
            }
        } else {
            // 初始化 延迟动画 （ ValueAnimator ）
            mDelayAnim = ValueAnimator.ofFloat(0f, 1f);
            // 延迟动画 设置上 持续时间
            mDelayAnim.setDuration(mStartDelay);
            // 延迟动画 设置上 AnimatorListenerAdapter10 监听
            mDelayAnim.addListener(new AnimatorListenerAdapter10() {

                // 只有 在执行了 onAnimationCancel 回调后才设置为true
                boolean canceled = false;

                public void onAnimationCancel(Animator10 anim) {
                    canceled = true;
                }

                public void onAnimationEnd(Animator10 anim) {
                    // onAnimationCancel 之前
                    if (!canceled) {
                        int numNodes = nodesToStart.size();
                        // 遍历 nodesToStart 集合
                        for (Node node : nodesToStart) {
                            // 每个节点动画 执行 start 逻辑
                            node.animation.start();
                            // 将该动画 缓存到 正在播放的动画集合
                            mPlayingSet.add(node.animation);
                        }
                    }
                    // 延迟动画 置为 null
                    mDelayAnim = null;
                }
            });
            // 延迟动画开启
            mDelayAnim.start();
        }

        // 如果缓存 AnimatorListener 集合 不为null
        if (mListeners != null) {
            // 克隆一份 缓存 AnimatorListener 集合
            ArrayList<AnimatorListener> tmpListeners =
                    (ArrayList<AnimatorListener>) mListeners.clone();
            int numListeners = tmpListeners.size();
            // 操作克隆样本 AnimatorListener 集合
            for (AnimatorListener tmpListener : tmpListeners) {
                // 逐个调用 onAnimationStart 回调
                tmpListener.onAnimationStart(this);
            }
        }

        // 如果节点长度为0 并且 开始延迟时间为0
        if (mNodes.size() == 0 && mStartDelay == 0) {
            // 标记为没开始
            mStarted = false;
            // 如果缓存 AnimatorListener 集合 为 null
            if (mListeners != null) {
                // 再一次 克隆一份 AnimatorListener 集合
                ArrayList<AnimatorListener> tmpListeners =
                        (ArrayList<AnimatorListener>) mListeners.clone();
                int numListeners = tmpListeners.size();
                for (AnimatorListener tmpListener : tmpListeners) {
                    // 逐个回调 onAnimationEnd
                    tmpListener.onAnimationEnd(this);
                }
            }
        }
    }

    /**
     * AnimatorSet10 克隆方法
     *
     * @return AnimatorSet10
     */
    @Override
    public AnimatorSet10 clone() {
        // 先执行 Animator10 的克隆方法
        final AnimatorSet10 anim = (AnimatorSet10) super.clone();

        // 标记需要排序
        anim.mNeedsSort = true;
        // 标记没终止
        anim.mTerminated = false;
        // 标记没开始
        anim.mStarted = false;
        // 重新初始化 正在播放的动画 缓存
        anim.mPlayingSet = new ArrayList<>();
        // 重新初始化 Animator10 和 Node 的关系 缓存
        anim.mNodeMap = new HashMap<>();
        // 重新初始化 节点 缓存
        anim.mNodes = new ArrayList<>();
        // 重新初始化 排序节点 缓存
        anim.mSortedNodes = new ArrayList<>();

        HashMap<Node, Node> nodeCloneMap = new HashMap<>();
        // 遍历每一个 节点 缓存
        for (Node node : mNodes) {
            // 克隆每一个 节点
            Node nodeClone = node.clone();
            // 放入 nodeCloneMap
            nodeCloneMap.put(node, nodeClone);
            // 添加到 克隆 AnimatorSet10 的 节点 缓存中
            anim.mNodes.add(nodeClone);
            // 添加到 克隆 AnimatorSet10 的 Animator10 和 Node 的 关系缓存中
            anim.mNodeMap.put(nodeClone.animation, nodeClone);
            // 初始化 克隆 节点 的数据
            nodeClone.dependencies = null;
            nodeClone.tmpDependencies = null;
            nodeClone.nodeDependents = null;
            nodeClone.nodeDependencies = null;

            // 克隆 克隆节点的 ArrayList<AnimatorListener>
            ArrayList<AnimatorListener> cloneListeners = nodeClone.animation.getListeners();
            // ArrayList<AnimatorListener> 不为空
            if (cloneListeners != null) {
                ArrayList<AnimatorListener> listenersToRemove = null;
                // 遍历 克隆ArrayList<AnimatorListener>
                // 将集合数据全部加入到listenersToRemove
                for (AnimatorListener listener : cloneListeners) {
                    if (listener instanceof AnimatorSetListener) {
                        if (listenersToRemove == null) {
                            listenersToRemove = new ArrayList<>();
                        }
                        listenersToRemove.add(listener);
                    }
                }

                // 如果 克隆ArrayList<AnimatorListener> 不为空
                if (listenersToRemove != null) {
                    // 清空 克隆ArrayList<AnimatorListener> 数据
                    for (AnimatorListener listener : listenersToRemove) {
                        cloneListeners.remove(listener);
                    }
                }
            }
        }

        // 遍历 节点 缓存数据
        for (Node node : mNodes) {
            // 根据 node key 从 nodeCloneMap 取出 node
            Node nodeClone = nodeCloneMap.get(node);
            // 如果节点 依赖信息不为空
            if (node.dependencies != null) {
                // 开始遍历 依赖信息
                for (Dependency dependency : node.dependencies) {
                    // 根据依赖信息里的节点数据 从nodeCloneMap
                    Node clonedDependencyNode = nodeCloneMap.get(dependency.node);
                    // 根据依赖信息 创建一个 新 的依赖信息
                    Dependency cloneDependency = new Dependency(clonedDependencyNode, dependency.rule);
                    // 然后给克隆节点添加 依赖信息
                    nodeClone.addDependency(cloneDependency);
                }
            }
        }
        return anim;
    }

    private static class DependencyListener implements AnimatorListener {

        private AnimatorSet10 mAnimatorSet;
        private Node mNode;
        private int mRule;

        public DependencyListener(AnimatorSet10 animatorSet, Node node, int rule) {
            this.mAnimatorSet = animatorSet;
            this.mNode = node;
            this.mRule = rule;
        }

        public void onAnimationCancel(Animator10 animation) {

        }

        public void onAnimationEnd(Animator10 animation) {
            // 如果依赖信息规则 == Dependency.AFTER
            if (mRule == Dependency.AFTER) {
                startIfReady(animation);
            }
        }

        public void onAnimationRepeat(Animator10 animation) {

        }

        public void onAnimationStart(Animator10 animation) {
            // 如果依赖信息规则 == Dependency.WITH
            if (mRule == Dependency.WITH) {
                startIfReady(animation);
            }
        }

        private void startIfReady(Animator10 dependencyAnimation) {
            // 如果动画停止了 直接返回
            if (mAnimatorSet.mTerminated) {
                return;
            }
            Dependency dependencyToRemove = null;
            // 这里拿 临时依赖信息 集合
            // 是因为根据正常逻辑是必走AnimationSet10.start()
            int numDependencies = mNode.tmpDependencies.size();
            for (int i = 0; i < numDependencies; ++i) {
                Dependency dependency = mNode.tmpDependencies.get(i);
                // 如果 规则 和 动画都对应了 临时依赖信息集合里的数据
                // 就是 初始化 DependencyListener 的依赖信息集合中存在 dependencyAnimation 动画
                // 然后 依赖信息 为 dependencyToRemove
                if (dependency.rule == mRule && dependency.node.animation == dependencyAnimation) {
                    dependencyToRemove = dependency;
                    // 拿到依赖信息 移除监听
                    dependencyAnimation.removeListener(this);
                    break;
                }
            }
            // 移除 临时依赖信息集合 对应记录的 dependencyToRemove 依赖信息
            mNode.tmpDependencies.remove(dependencyToRemove);

            if (mNode.tmpDependencies.size() == 0) {
                // 开始动画
                mNode.animation.start();
                // 并加入到 正在播放的动画 缓存中
                mAnimatorSet.mPlayingSet.add(mNode.animation);
            }
        }
    }

    private class AnimatorSetListener implements AnimatorListener {

        private AnimatorSet10 mAnimatorSet;

        AnimatorSetListener(AnimatorSet10 animatorSet) {
            mAnimatorSet = animatorSet;
        }

        public void onAnimationCancel(Animator10 animation) {
            // 如果没有终止
            if (!mTerminated) {
                // 如果 正在播放的动画 集合 长度为 0
                if (mPlayingSet.size() == 0) {
                    // 如果 AnimatorListener 集合 不等于 null
                    if (mListeners != null) {
                        int numListeners = mListeners.size();
                        for (AnimatorListener mListener : mListeners) {
                            // 拿到每个 AnimatorListener 回调  onAnimationCancel
                            mListener.onAnimationCancel(mAnimatorSet);
                        }
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void onAnimationEnd(Animator10 animation) {
            // 移除监听
            animation.removeListener(this);
            // 移除正在播放缓存
            mPlayingSet.remove(animation);
            // 拿到动画对应节点
            Node animNode = mAnimatorSet.mNodeMap.get(animation);
            // 标记节点done = true
            animNode.done = true;
            // 如果没终止
            if (!mTerminated) {
                ArrayList<Node> sortedNodes = mAnimatorSet.mSortedNodes;
                boolean allDone = true;
                int numSortedNodes = sortedNodes.size();
                // 拿到所有排序后节点
                for (Node sortedNode : sortedNodes) {
                    // 如果有一个节点动画没执行过了
                    if (!sortedNode.done) {
                        // allDone标记为false
                        allDone = false;
                        break;
                    }
                }
                // 如果节点动画都执行过了
                if (allDone) {
                    // 如果 AnimatorListener 集合 不为 null
                    if (mListeners != null) {
                        // 克隆一份 AnimatorListener 集合
                        ArrayList<AnimatorListener> tmpListeners =
                                (ArrayList<AnimatorListener>) mListeners.clone();
                        int numListeners = tmpListeners.size();
                        // 遍历克隆样本 AnimatorListener 集合
                        for (AnimatorListener tmpListener : tmpListeners) {
                            // 逐个调用AnimatorListener的回调 onAnimationEnd
                            tmpListener.onAnimationEnd(mAnimatorSet);
                        }
                    }
                    // AnimatorSet10 标记为没开始
                    mAnimatorSet.mStarted = false;
                    // AnimatorSet10 标记为没暂停
                    mAnimatorSet.mPaused = false;
                }
            }
        }

        public void onAnimationRepeat(Animator10 animation) {

        }

        public void onAnimationStart(Animator10 animation) {

        }
    }

    /**
     * 开始对节点排序
     */
    private void sortNodes() {
        // 是否需要配需
        if (mNeedsSort) {
            // 清空排序节点集合
            mSortedNodes.clear();
            /*
             * 初始化一个List
             * 先用于备份一份所有节点的依赖信息
             */
            ArrayList<Node> roots = new ArrayList<>();
            int numNodes = mNodes.size();
            for (Node node : mNodes) {
                if (node.dependencies == null || node.dependencies.size() == 0) {
                    roots.add(node);
                }
            }
            ArrayList<Node> tmpRoots = new ArrayList<>();
            // roots长度 大于 0
            while (roots.size() > 0) {
                int numRoots = roots.size();
                for (Node root : roots) {
                    /*
                     * 逐个将每个节点
                     * 添加到排序节点集合内
                     */
                    mSortedNodes.add(root);
                    // 如果 该节点存在要依赖的节点
                    if (root.nodeDependents != null) {
                        int numDependents = root.nodeDependents.size();
                        for (int j = 0; j < numDependents; ++j) {
                            // 拿到每一个要依赖的节点
                            Node node = root.nodeDependents.get(j);
                            // 尝试从依赖该节点的节点集合中删除
                            node.nodeDependencies.remove(root);
                            // 如果此时 依赖该节点的节点集合 长度 为 0
                            if (node.nodeDependencies.size() == 0) {
                                // 先放入到一个临时 集合里
                                tmpRoots.add(node);
                            }
                        }
                    }
                }
                // roots.size() == 0
                roots.clear();
                /*
                 * 如果 tmpRoots.size() == 0
                 * 则跳出循环了
                 */
                roots.addAll(tmpRoots);
                tmpRoots.clear();
            }
            // 标记为不需要配需
            mNeedsSort = false;
            /*
             * 如果排序后的节点集合 再次不等于 节点集合
             * “炸”
             */
            if (mSortedNodes.size() != mNodes.size()) {
                throw new IllegalStateException("Circular dependencies cannot exist in AnimatorSet");
            }
        } else {
            int numNodes = mNodes.size();
            // 遍历所有节点集合
            for (Node node : mNodes) {
                /*
                 * 如果 节点的依赖信息 不为 null
                 * 节点的依赖信息 长度 大于 0
                 */
                if (node.dependencies != null && node.dependencies.size() > 0) {
                    int numDependencies = node.dependencies.size();
                    for (int j = 0; j < numDependencies; ++j) {
                        // 拿到每个节点的 依赖信息
                        Dependency dependency = node.dependencies.get(j);
                        /*
                         * 如果 该依赖该节点的节点集合 为空
                         * 初始化一个新的集合
                         */
                        if (node.nodeDependencies == null) {
                            node.nodeDependencies = new ArrayList<>();
                        }
                        /*
                         * 如果 该依赖该节点的节点集合 不存在 对应该依赖信息的 节点
                         * 加入 对应该依赖信息的节点 到 该依赖该节点的节点集合 里
                         */
                        if (!node.nodeDependencies.contains(dependency.node)) {
                            node.nodeDependencies.add(dependency.node);
                        }
                    }
                }
                node.done = false;
            }
        }
    }

    /**
     * 封装 Node 对象 及其相关信息和规则
     */
    private static class Dependency {
        // 依赖的Node必须和被依赖的Node一起启动
        static final int WITH = 0;

        // 依赖的Node必须在被依赖的Node结束后启动
        static final int AFTER = 1;

        public Node node;

        // WITH or AFTER
        public int rule;

        public Dependency(Node node, int rule) {
            this.node = node;
            this.rule = rule;
        }
    }

    private static class Node implements Cloneable {

        // 该节点的动画
        public Animator10 animation;

        // 该节点依赖信息集合
        public ArrayList<Dependency> dependencies = null;
        public ArrayList<Dependency> tmpDependencies = null;

        // 该依赖该节点的节点集合
        public ArrayList<Node> nodeDependencies = null;

        // 该节点所依赖的节点集合
        public ArrayList<Node> nodeDependents = null;
        public boolean done = false;

        public Node(Animator10 animation) {
            this.animation = animation;
        }

        public void addDependency(Dependency dependency) {
            // 如果依赖信息集合不存在，重新初始化
            if (dependencies == null) {
                dependencies = new ArrayList<>();
                nodeDependencies = new ArrayList<>();
            }
            // 将依赖信息添加到集合里
            dependencies.add(dependency);

            /*
             * 判断依赖节点集合是否存在要添加的依赖节点
             * 不存在就添加
             */
            if (!nodeDependencies.contains(dependency.node)) {
                nodeDependencies.add(dependency.node);
            }

            // 拿到要添加的依赖节点
            Node dependencyNode = dependency.node;

            // 判断要 添加的依赖节点的所依赖的节点集合 是否为空
            if (dependencyNode.nodeDependents == null) {
                dependencyNode.nodeDependents = new ArrayList<>();
            }
            // 给 添加的依赖节点的所依赖的节点集合 添加依赖(this)
            dependencyNode.nodeDependents.add(this);
        }

        /**
         * Node 的克隆方法
         *
         * @return Node
         */
        @Override
        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.animation = animation.clone();
                return node;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    /**
     * <pre>
     *     AnimatorSet s = new AnimatorSet();
     *     s.play(anim1).with(anim2);
     *     s.play(anim2).before(anim3);
     *     s.play(anim4).after(anim3);
     * </pre>
     * <pre>
     *   AnimatorSet s = new AnimatorSet();
     *   s.play(anim1).before(anim2).before(anim3);
     * </pre>
     * <pre>
     *   AnimatorSet s = new AnimatorSet();
     *   s.play(anim1).before(anim2);
     *   s.play(anim2).before(anim3);
     * </pre>
     */
    public class Builder {

        private Node mCurrentNode;

        Builder(Animator10 anim) {
            /*
             * 根据 Animator10 去拿到Map数据里 Node 的缓存数据
             * 然后放入 mCurrentNode 里
             */
            mCurrentNode = mNodeMap.get(anim);
            // 如果 Node 缓存数据 不存在
            if (mCurrentNode == null) {
                // 根据 Animator10 初始化一个新的 Node 数据
                mCurrentNode = new Node(anim);
                // Node 节点存入缓存数据里 HashMap<Animator10, Node>
                mNodeMap.put(anim, mCurrentNode);
                // Node 节点存入缓存数据里 ArrayList<Node>
                mNodes.add(mCurrentNode);
            }
        }

        public Builder with(Animator10 anim) {
            // 根据 Animator10 去拿到Map数据里 Node 的缓存数据
            Node node = mNodeMap.get(anim);
            // 如果 Node 缓存数据存在
            if (node == null) {
                // 根据 Animator10 初始化一个新的 Node 数据
                node = new Node(anim);
                // Node 节点存入缓存数据里 HashMap<Animator10, Node>
                mNodeMap.put(anim, node);
                // Node 节点存入缓存数据里 ArrayList<Node>
                mNodes.add(node);
            }
            /*
             * 由于是 with 方法
             * 所以 Dependency.rule ＝ Dependency.WITH
             * 初始化一个 新的 Dependency 包装该 Node 节点
             */
            Dependency dependency = new Dependency(mCurrentNode, Dependency.WITH);
            // 添加 Dependency 到缓存数据中
            node.addDependency(dependency);
            return this;
        }

        public Builder before(Animator10 anim) {
            // 根据 Animator10 去拿到Map数据里 Node 的缓存数据
            Node node = mNodeMap.get(anim);
            // 如果 Node 缓存数据存在
            if (node == null) {
                // 根据 Animator10 初始化一个新的 Node 数据
                node = new Node(anim);
                // Node 节点存入缓存数据里 HashMap<Animator10, Node>
                mNodeMap.put(anim, node);
                // Node 节点存入缓存数据里 ArrayList<Node>
                mNodes.add(node);
            }
            /*
             * before 方法
             * 所以 Dependency.rule ＝ Dependency.AFTER
             * 初始化一个 新的 Dependency 包装该 Node 节点
             */
            Dependency dependency = new Dependency(mCurrentNode, Dependency.AFTER);
            // 添加 Dependency 到缓存数据中
            node.addDependency(dependency);
            return this;
        }

        public Builder after(Animator10 anim) {
            // 根据 Animator10 去拿到Map数据里 Node 的缓存数据
            Node node = mNodeMap.get(anim);
            // 如果 Node 缓存数据存在
            if (node == null) {
                // 根据 Animator10 初始化一个新的 Node 数据
                node = new Node(anim);
                // Node 节点存入缓存数据里 HashMap<Animator10, Node>
                mNodeMap.put(anim, node);
                // Node 节点存入缓存数据里 ArrayList<Node>
                mNodes.add(node);
            }
            /*
             * after 方法
             * 所以 Dependency.rule ＝ Dependency.AFTER
             * 初始化一个 新的 Dependency 包装该 Node 节点
             */
            Dependency dependency = new Dependency(node, Dependency.AFTER);
            // 添加 Dependency 到缓存数据中
            mCurrentNode.addDependency(dependency);
            return this;
        }

        public Builder after(long delay) {
            // 初始化一个 ValueAnimator
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            // 设置上延迟
            anim.setDuration(delay);
            // 调用 after 方法
            after(anim);
            return this;
        }
    }
}
