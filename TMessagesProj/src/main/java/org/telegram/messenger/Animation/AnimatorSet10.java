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

    // 动画加速器
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
     * 给 AnimatorSet10 设置上 加速器
     *
     * @param interpolator interpolator
     */
    @Override
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * 拿到 该 AnimatorSet10 的加速器
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
     * 取消
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
                 * 逐个调用其cancel回调
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
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public void end() {
        mTerminated = true;
        if (isStarted()) {
            if (mSortedNodes.size() != mNodes.size()) {
                // hasn't been started yet - sort the nodes now, then end them
                sortNodes();
                for (Node node : mSortedNodes) {
                    if (mSetListener == null) {
                        mSetListener = new AnimatorSetListener(this);
                    }
                    node.animation.addListener(mSetListener);
                }
            }
            if (mDelayAnim != null) {
                mDelayAnim.cancel();
            }
            if (mSortedNodes.size() > 0) {
                for (Node node : mSortedNodes) {
                    node.animation.end();
                }
            }
            if (mListeners != null) {
                ArrayList<AnimatorListener> tmpListeners = (ArrayList<AnimatorListener>) mListeners.clone();
                for (AnimatorListener listener : tmpListeners) {
                    listener.onAnimationEnd(this);
                }
            }
            mStarted = false;
        }
    }

    /**
     * @return boolean
     */
    @Override
    public boolean isRunning() {
        for (Node node : mNodes) {
            if (node.animation.isRunning()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStarted() {
        return mStarted;
    }

    @Override
    public long getStartDelay() {
        return mStartDelay;
    }

    @Override
    public void setStartDelay(long startDelay) {
        mStartDelay = startDelay;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public AnimatorSet10 setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be a value of zero or greater");
        }
        mDuration = duration;
        return this;
    }

    @Override
    public void setupStartValues() {
        for (Node node : mNodes) {
            node.animation.setupStartValues();
        }
    }

    @Override
    public void setupEndValues() {
        for (Node node : mNodes) {
            node.animation.setupEndValues();
        }
    }

    @Override
    public void pause() {
        boolean previouslyPaused = mPaused;
        super.pause();
        if (!previouslyPaused && mPaused) {
            if (mDelayAnim != null) {
                mDelayAnim.pause();
            } else {
                for (Node node : mNodes) {
                    node.animation.pause();
                }
            }
        }
    }

    @Override
    public void resume() {
        boolean previouslyPaused = mPaused;
        super.resume();
        if (previouslyPaused && !mPaused) {
            if (mDelayAnim != null) {
                mDelayAnim.resume();
            } else {
                for (Node node : mNodes) {
                    node.animation.resume();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        mTerminated = false;
        mStarted = true;
        mPaused = false;

        if (mDuration >= 0) {
            for (Node node : mNodes) {
                node.animation.setDuration(mDuration);
            }
        }
        if (mInterpolator != null) {
            for (Node node : mNodes) {
                node.animation.setInterpolator(mInterpolator);
            }
        }

        sortNodes();

        int numSortedNodes = mSortedNodes.size();
        for (Node node : mSortedNodes) {
            ArrayList<AnimatorListener> oldListeners = node.animation.getListeners();
            if (oldListeners != null && oldListeners.size() > 0) {
                final ArrayList<AnimatorListener> clonedListeners = new
                        ArrayList<>(oldListeners);

                for (AnimatorListener listener : clonedListeners) {
                    if (listener instanceof DependencyListener ||
                            listener instanceof AnimatorSetListener) {
                        node.animation.removeListener(listener);
                    }
                }
            }
        }

        final ArrayList<Node> nodesToStart = new ArrayList<>();
        for (Node node : mSortedNodes) {
            if (mSetListener == null) {
                mSetListener = new AnimatorSetListener(this);
            }
            if (node.dependencies == null || node.dependencies.size() == 0) {
                nodesToStart.add(node);
            } else {
                int numDependencies = node.dependencies.size();
                for (int j = 0; j < numDependencies; ++j) {
                    Dependency dependency = node.dependencies.get(j);
                    dependency.node.animation.addListener(
                            new DependencyListener(this, node, dependency.rule));
                }
                node.tmpDependencies = (ArrayList<Dependency>) node.dependencies.clone();
            }
            node.animation.addListener(mSetListener);
        }

        if (mStartDelay <= 0) {
            for (Node node : nodesToStart) {
                node.animation.start();
                mPlayingSet.add(node.animation);
            }
        } else {
            mDelayAnim = ValueAnimator.ofFloat(0f, 1f);
            mDelayAnim.setDuration(mStartDelay);
            mDelayAnim.addListener(new AnimatorListenerAdapter10() {
                boolean canceled = false;

                public void onAnimationCancel(Animator10 anim) {
                    canceled = true;
                }

                public void onAnimationEnd(Animator10 anim) {
                    if (!canceled) {
                        int numNodes = nodesToStart.size();
                        for (Node node : nodesToStart) {
                            node.animation.start();
                            mPlayingSet.add(node.animation);
                        }
                    }
                    mDelayAnim = null;
                }
            });
            mDelayAnim.start();
        }
        if (mListeners != null) {
            ArrayList<AnimatorListener> tmpListeners =
                    (ArrayList<AnimatorListener>) mListeners.clone();
            int numListeners = tmpListeners.size();
            for (AnimatorListener tmpListener : tmpListeners) {
                tmpListener.onAnimationStart(this);
            }
        }
        if (mNodes.size() == 0 && mStartDelay == 0) {
            mStarted = false;
            if (mListeners != null) {
                ArrayList<AnimatorListener> tmpListeners =
                        (ArrayList<AnimatorListener>) mListeners.clone();
                int numListeners = tmpListeners.size();
                for (AnimatorListener tmpListener : tmpListeners) {
                    tmpListener.onAnimationEnd(this);
                }
            }
        }
    }

    @Override
    public AnimatorSet10 clone() {
        final AnimatorSet10 anim = (AnimatorSet10) super.clone();

        anim.mNeedsSort = true;
        anim.mTerminated = false;
        anim.mStarted = false;
        anim.mPlayingSet = new ArrayList<>();
        anim.mNodeMap = new HashMap<>();
        anim.mNodes = new ArrayList<>();
        anim.mSortedNodes = new ArrayList<>();

        HashMap<Node, Node> nodeCloneMap = new HashMap<>();
        for (Node node : mNodes) {
            Node nodeClone = node.clone();
            nodeCloneMap.put(node, nodeClone);
            anim.mNodes.add(nodeClone);
            anim.mNodeMap.put(nodeClone.animation, nodeClone);
            nodeClone.dependencies = null;
            nodeClone.tmpDependencies = null;
            nodeClone.nodeDependents = null;
            nodeClone.nodeDependencies = null;
            ArrayList<AnimatorListener> cloneListeners = nodeClone.animation.getListeners();
            if (cloneListeners != null) {
                ArrayList<AnimatorListener> listenersToRemove = null;
                for (AnimatorListener listener : cloneListeners) {
                    if (listener instanceof AnimatorSetListener) {
                        if (listenersToRemove == null) {
                            listenersToRemove = new ArrayList<>();
                        }
                        listenersToRemove.add(listener);
                    }
                }
                if (listenersToRemove != null) {
                    for (AnimatorListener listener : listenersToRemove) {
                        cloneListeners.remove(listener);
                    }
                }
            }
        }
        for (Node node : mNodes) {
            Node nodeClone = nodeCloneMap.get(node);
            if (node.dependencies != null) {
                for (Dependency dependency : node.dependencies) {
                    Node clonedDependencyNode = nodeCloneMap.get(dependency.node);
                    Dependency cloneDependency = new Dependency(clonedDependencyNode, dependency.rule);
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
            if (mRule == Dependency.AFTER) {
                startIfReady(animation);
            }
        }

        public void onAnimationRepeat(Animator10 animation) {

        }

        public void onAnimationStart(Animator10 animation) {
            if (mRule == Dependency.WITH) {
                startIfReady(animation);
            }
        }

        private void startIfReady(Animator10 dependencyAnimation) {
            if (mAnimatorSet.mTerminated) {
                return;
            }
            Dependency dependencyToRemove = null;
            int numDependencies = mNode.tmpDependencies.size();
            for (int i = 0; i < numDependencies; ++i) {
                Dependency dependency = mNode.tmpDependencies.get(i);
                if (dependency.rule == mRule && dependency.node.animation == dependencyAnimation) {
                    dependencyToRemove = dependency;
                    dependencyAnimation.removeListener(this);
                    break;
                }
            }
            mNode.tmpDependencies.remove(dependencyToRemove);
            if (mNode.tmpDependencies.size() == 0) {
                mNode.animation.start();
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
            if (!mTerminated) {
                if (mPlayingSet.size() == 0) {
                    if (mListeners != null) {
                        int numListeners = mListeners.size();
                        for (AnimatorListener mListener : mListeners) {
                            mListener.onAnimationCancel(mAnimatorSet);
                        }
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void onAnimationEnd(Animator10 animation) {
            animation.removeListener(this);
            mPlayingSet.remove(animation);
            Node animNode = mAnimatorSet.mNodeMap.get(animation);
            animNode.done = true;
            if (!mTerminated) {
                ArrayList<Node> sortedNodes = mAnimatorSet.mSortedNodes;
                boolean allDone = true;
                int numSortedNodes = sortedNodes.size();
                for (Node sortedNode : sortedNodes) {
                    if (!sortedNode.done) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    if (mListeners != null) {
                        ArrayList<AnimatorListener> tmpListeners =
                                (ArrayList<AnimatorListener>) mListeners.clone();
                        int numListeners = tmpListeners.size();
                        for (AnimatorListener tmpListener : tmpListeners) {
                            tmpListener.onAnimationEnd(mAnimatorSet);
                        }
                    }
                    mAnimatorSet.mStarted = false;
                    mAnimatorSet.mPaused = false;
                }
            }
        }

        public void onAnimationRepeat(Animator10 animation) {

        }

        public void onAnimationStart(Animator10 animation) {

        }
    }

    private void sortNodes() {
        if (mNeedsSort) {
            mSortedNodes.clear();
            ArrayList<Node> roots = new ArrayList<>();
            int numNodes = mNodes.size();
            for (Node node : mNodes) {
                if (node.dependencies == null || node.dependencies.size() == 0) {
                    roots.add(node);
                }
            }
            ArrayList<Node> tmpRoots = new ArrayList<>();
            while (roots.size() > 0) {
                int numRoots = roots.size();
                for (Node root : roots) {
                    mSortedNodes.add(root);
                    if (root.nodeDependents != null) {
                        int numDependents = root.nodeDependents.size();
                        for (int j = 0; j < numDependents; ++j) {
                            Node node = root.nodeDependents.get(j);
                            node.nodeDependencies.remove(root);
                            if (node.nodeDependencies.size() == 0) {
                                tmpRoots.add(node);
                            }
                        }
                    }
                }
                roots.clear();
                roots.addAll(tmpRoots);
                tmpRoots.clear();
            }
            mNeedsSort = false;
            if (mSortedNodes.size() != mNodes.size()) {
                throw new IllegalStateException("Circular dependencies cannot exist in AnimatorSet");
            }
        } else {
            int numNodes = mNodes.size();
            for (Node node : mNodes) {
                if (node.dependencies != null && node.dependencies.size() > 0) {
                    int numDependencies = node.dependencies.size();
                    for (int j = 0; j < numDependencies; ++j) {
                        Dependency dependency = node.dependencies.get(j);
                        if (node.nodeDependencies == null) {
                            node.nodeDependencies = new ArrayList<>();
                        }
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

        // 该节点的依赖子集
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
