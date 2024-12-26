package com.sjc.async.wrapper;

/*
  @author abstractMoonAstronaut
 * {@code @date} 2024/11/30
 * {@code @msg} reserved
 */

import com.sjc.async.callback.DefaultCallback;
import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.exception.SkippedException;
import com.sjc.async.executor.timer.SystemClock;
import com.sjc.async.worker.DependWrapper;
import com.sjc.async.worker.ResultState;
import com.sjc.async.worker.WorkResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一对一
 * 对每个 worker 和 wrapper 进行包装
 */
public class WorkerWrapper<T, V> {
    private static final int INIT = 0;
    private static final int FINISHED = 1;
    private static final int ERROR = 2;
    private static final int WORKING = 3;

    private T param;

    private IWorker<T, V> worker;

    private ICallback<T, V> callback;

    // 在自己后面的wrappers (很多的话要开多线程)
    private List<WorkerWrapper<?, ?>> nextWrappers;

    // 自己依赖的wrappers，全部依赖执行完成后才能执行自己
    private List<DependWrapper> dependWrappers;

    /**
     * 1
     *  -------3
     * 2
     * 如果2执行前发现3执行完毕了(被1触发) 则 2没必要再执行
     * 2的nextWrapper只有一个才可以
     */
    private volatile boolean needCheckNextWrapperResult = true;

    /**
     * 标识此事件是否被执行过了
     * volatile 不能保证毫秒级别的 多线程的 值的修改和拉取
     * 1-finished 2-error 3-working
     */
    private AtomicInteger state = new AtomicInteger(0);

    /**
     * 钩子变量
     * 存放临时结果
     */
    private volatile WorkResult<V> workResult = WorkResult.defaultResult();


    private WorkerWrapper(IWorker<T, V> worker, ICallback<T, V> callback, T param) {
        if (worker == null) {
            throw new NullPointerException("async.worker is null");
        }
        this.worker = worker;
        this.param = param;
        if (callback == null) {
            callback = new DefaultCallback<>(); // 默认的回调方法
        }
        this.callback = callback;
    }

    // 整体的工作流程
    // fromWrapper 代表这个work是由上游哪一个wrapper发起的
    private void work(ThreadPoolExecutor poolExecutor, WorkerWrapper fromWrapper, long remainTime) {
        long now = SystemClock.now();

        // 总时间已经超时了，快速失败，进行下一个
        if (remainTime <= 0) {
            System.err.println("remainTime is empty stop work -- threadName --- " + Thread.currentThread().getName());
            fastFail(INIT, null);
            beginNext(poolExecutor, now, remainTime);
            return;
        }
        // 如果自己执行过了，不要反复执行(有多个依赖，别多个依赖唤醒？)
        if (getState() == FINISHED || getState() == ERROR) {
            beginNext(poolExecutor, now, remainTime);
            return;
        }
        if (needCheckNextWrapperResult) {
            // 如果自己唯一的一个nextWrapper已经出现结果或者开始执行新的任务，自己就不用继续了
            if (!checkNextWrapperResult()) {
                fastFail(INIT , new SkippedException());
                beginNext(poolExecutor, now, remainTime);
                return;
            }
        }
        // 如果没有任何依赖，说明自己就是第一批要执行的
        if (dependWrappers == null || dependWrappers.isEmpty()) {
            fire();
            beginNext(poolExecutor, now, remainTime);
            return;
        }
        // 前方只有一个依赖
        if (dependWrappers.size() == 1) {
            doDependsOneJob(fromWrapper);
            beginNext(poolExecutor, now, remainTime);
        } else{
            //多个依赖的情况，会被前方的依赖任务多次唤醒，需要判断是否全部执行完毕
            doDependsJobs(poolExecutor, dependWrappers, fromWrapper, now, remainTime);
        }

    }

    private boolean checkNextWrapperResult() {

        // 如果自己是最后一个 或者后面有多个并行任务，则需要执行此任务
        if (nextWrappers == null || nextWrappers.size() != 1) {

            return getState() == INIT;
        }

        WorkerWrapper nextWrapper = nextWrappers.get(0);
        boolean state = nextWrapper.getState() == INIT;

        // 继续校验自己的next的状态
        return state && nextWrapper.checkNextWrapperResult();
    }


    public void work(ThreadPoolExecutor poolExecutor , long remainTime) {
        work(poolExecutor , null , remainTime);
    }


    /**
     * 总控制台超时，停止所有任务，在Async中被调用
     */
    public void stopNow() {
        if (getState() == INIT || getState() == WORKING) {
            System.err.println("threadName - " + Thread.currentThread().getName() + " stop for executor timeout");
            fastFail(getState() , null);
        }
    }

    /**
     * 运行下一个任务
     */
    private void beginNext(ThreadPoolExecutor poolExecutor, long now, long remainTime) {
        // 花费的时间
        long costTime = SystemClock.now() - now;
        if (nextWrappers == null) {
            return;
        }
        if (nextWrappers.size() == 1) {
            nextWrappers.get(0).work(poolExecutor , WorkerWrapper.this , remainTime - costTime);
            return;
        }
        CompletableFuture[] futures = new CompletableFuture[nextWrappers.size()];
        for (int i = 0 ; i < nextWrappers.size() ; i++) {
            int finalI = i; // 是的i在lambda中可见
            futures[i] = CompletableFuture.runAsync(() ->
                    nextWrappers.get(finalI).work(poolExecutor , WorkerWrapper.this,remainTime - costTime ) ,
                    poolExecutor);
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    private void doDependsOneJob(WorkerWrapper dependWrapper) {

        if (ResultState.TIMEOUT == dependWrapper.getWorkResult().getResultState()) {
            workResult = defaultResult();
            fastFail(INIT , null);
        } else if (ResultState.EXCEPTION == dependWrapper.getWorkResult().getResultState()) {
            workResult = defaultExResult(dependWrapper.getWorkResult().getEx());
            fastFail(INIT , null);
        } else {
            //前面任务正常执行完毕，轮到自己了
            fire();
        }
    }

    private synchronized void doDependsJobs(ThreadPoolExecutor poolExecutor, List<DependWrapper> dependWrappers, WorkerWrapper fromWrapper, long now, long remainTime) {
        // 上游father 任务是否为must的
        boolean nowDependIsMust = false;
        // 必须要完成的上游wrapper的集合
        Set<DependWrapper> mustWrapper = new HashSet<>();
        for (DependWrapper dependWrapper : dependWrappers) {
            if (dependWrapper.isMust()) {
                mustWrapper.add(dependWrapper);
            }
            if (dependWrapper.getDependWrapper().equals(fromWrapper)) {
                nowDependIsMust = dependWrapper.isMust();
            }
        }

        // 如果全都是不必须的条件 ，到这里 判断完 调用方的上游father wrapper 就可以继续执行了
        if (mustWrapper.isEmpty()) {
            if (ResultState.TIMEOUT == fromWrapper.getWorkResult().getResultState()) {
                fastFail(INIT , null);
            } else {
                fire();
            }
            beginNext(poolExecutor , now , remainTime);
            return;
        }

        // 如果存在必须的 ，并且 father wrapper 不是必须的，相当于啥都不用干
        if (!nowDependIsMust) {
            return;
        }

        //father wrapper 必须

        // 是否存在正在运行中的
        boolean existNotFinish = false;
        // 是否存在出错的
        boolean hashError = false;

        // 遍历判断 依赖列表中的执行结果 任何一个失败 就直接failFast
        for (DependWrapper dependWrapper : mustWrapper) {
            WorkerWrapper workerWrapper = dependWrapper.getDependWrapper();
            WorkResult tempWorkResult = workerWrapper.getWorkResult();

            // 状态为INIT或者状态为Working 代表没有执行完
            if (workerWrapper.getState() == INIT || workerWrapper.getState() == WORKING) {
                existNotFinish = true;
                break;
            }
            if (ResultState.TIMEOUT == tempWorkResult.getResultState()) {
                workResult = defaultResult();
                hashError = true;
                break;
            }
            if (ResultState.EXCEPTION == tempWorkResult.getResultState()) {
                workResult = defaultExResult(workerWrapper.getWorkResult().getEx());
                hashError = true;
                break;
            }
        }
        // 依赖链中只要有失败的，直接结束
        if (hashError) {
            fastFail(INIT , null);
            beginNext(poolExecutor , now , remainTime);
            return;
        }

        System.out.println(Thread.currentThread().getName() + " if exist----" + existNotFinish);
        // 如果依赖的wrapper 都执行结束了 就到自己了
        if (!existNotFinish) {
            fire();
            beginNext(poolExecutor , now , remainTime);
            return;
        }
    }

    /**
     * 执行自己的job，具体执行在另一个线程，判断阻塞超时在这个work线程(？？？没看懂)
     */
    private void fire() {
        // 阻塞获得结果
        workResult = workerDoJob();
    }
    private WorkResult<V> workerDoJob() {
        if (!checkIsNullResult()) {
            return workResult;
        }
        try {
            // 如果不是init状态了 - 说明正在被执行或已经执行完毕 防止被重复执行
            if (!compareAndSetState(INIT , WORKING)) {
                return workResult;
            }

            callback.begin();

            //耗时操作
            V resultValue = worker.action(param);

            // 如果状态不是working ， 说明别的地方修改了
            if (!compareAndSetState(WORKING , FINISHED)) {
                return workResult;
            }

            workResult.setResultState(ResultState.SUCCESS);
            workResult.setResult(resultValue);
            // 回调成功
            callback.result(true , param , workResult);

            return workResult;
        } catch (Exception e) {

            // 避免重复回调
            if (checkIsNullResult()) {
                return workResult;
            }
            System.err.println("执行自己的job失败喽");
            fastFail(WORKING , e);
            return workResult;
        }
    }


    /**
     * 快速失败
     */
    private boolean fastFail(int expect , Exception e) {
        System.err.println("fastFail: " + Thread.currentThread().getName() + " time " + System.currentTimeMillis());

        // 试图从except状态 改为 Error
        if (!compareAndSetState(expect , ERROR)) {
            System.out.println("compareAndSetState to Error failed");
            return false;
        }

        if (checkIsNullResult()) {
            if (e == null) {
                workResult = defaultResult();
            } else {
                workResult = defaultExResult(e);
            }
        }

        // 回调函数
        callback.result(false , param , workResult);
        return false;
    }



    private void addDepend(WorkerWrapper<? , ?> workerWrapper ,boolean must){
        addDepend(new DependWrapper(workerWrapper , must));
    }
    private void addDepend(DependWrapper dependWrapper ){
        if (dependWrappers == null) {
            dependWrappers = new ArrayList<>();
        }
        for (DependWrapper wrapper : dependWrappers) {
            if (wrapper.equals(dependWrapper)) {
                return;
            }
        }
        dependWrappers.add(dependWrapper);
    }

    private void addNext (WorkerWrapper<? ,?> workerWrapper) {
        if (nextWrappers == null) {
            nextWrappers = new ArrayList<>();
        }
        nextWrappers.stream().forEach(wrapper -> {
            if (wrapper.equals(workerWrapper)) {
                return;
            }
        });
        nextWrappers.add(workerWrapper);
    }
    private void addNextWrappers (List<WorkerWrapper<? ,?>> wrappers) {
        if (wrappers == null) {
            return;
        }
        wrappers.forEach(this::addNext);
    }
    private void addDependWrappers (List<DependWrapper> dependWrappers) {
        if (dependWrappers == null) {
            return;
        }
        dependWrappers.forEach(this::addDepend);
    }

    private boolean compareAndSetState(int expect , int update) {
        return this.state.compareAndSet(expect , update);
    }

    private boolean checkIsNullResult () {
        // result 是否处于初始化状态
        return ResultState.Default == workResult.getResultState();
    }
    private WorkResult<V> defaultResult() {
        workResult.setResultState(ResultState.TIMEOUT);
        workResult.setResult(worker.defaultValue());
        return workResult;
    }
    private WorkResult<V> defaultExResult(Exception ex) {
        workResult.setResultState(ResultState.EXCEPTION);
        workResult.setResult(worker.defaultValue());
        workResult.setEx(ex);
        return workResult;
    }




    public List<WorkerWrapper<?, ?>> getNextWrappers() {
        return nextWrappers;
    }

    public List<DependWrapper> getDependWrappers() {
        return dependWrappers;
    }

    public WorkResult<V> getWorkResult() {
        return workResult;
    }

    private int getState() {
        return state.get();
    }

    public void setWorkResult(WorkResult<V> workResult) {
        this.workResult = workResult;
    }

    public void setDependWrappers(List<DependWrapper> dependWrappers) {
        this.dependWrappers = dependWrappers;
    }



    public boolean isNeedCheckNextWrapperResult() {
        return needCheckNextWrapperResult;
    }

    private void setNeedCheckNextWrapperResult(boolean needCheckNextWrapperResult) {
        this.needCheckNextWrapperResult = needCheckNextWrapperResult;
    }

    public void setNextWrappers(List<WorkerWrapper<?, ?>> nextWrappers) {
        this.nextWrappers = nextWrappers;
    }

    public void setParam(T param) {
        this.param = param;
    }

    public static class Builder<W , C> {
        private W param;
        private IWorker<W , C> worker;
        private ICallback<W ,C> callback;

        private List<WorkerWrapper<? ,?>> nextWrappers; // 自己后面所有的wrapper
        private Set<WorkerWrapper<? ,?>> selfIsMustSet; // 强依赖于自己的wrapper
        private List<DependWrapper> dependWrappers;
        private boolean needCheckNextWrapperResult = true;


        public Builder<W ,C> worker(IWorker<W ,C> worker){
            this.worker = worker;
            return this;
        }
        public Builder<W ,C> param(W w){
            this.param = w;
            return this;
        }
        public Builder<W ,C> callback(ICallback<W ,C> callback){
            this.callback = callback;
            return this;
        }
        public Builder<W ,C> needCheckNextWrapperResult(boolean needCheckNextWrapperResult){
            this.needCheckNextWrapperResult = needCheckNextWrapperResult;
            return this;
        }
        public Builder<W ,C> depend(WorkerWrapper<? ,?>... wrappers) {
            if (wrappers == null) {
                return this;
            }
            for (WorkerWrapper<?, ?> wrapper : wrappers) {
                depend(wrapper);
            }
            return this;
        }
        public Builder<W ,C> depend(WorkerWrapper<? ,?> wrapper) {
            return depend(wrapper ,true);
        }
        public Builder<W ,C> depend (WorkerWrapper<? ,?> wrapper , boolean isMust) {
            if (wrapper == null) {
                return this;
            }
            DependWrapper dependWrapper = new DependWrapper(wrapper, isMust);
            if (dependWrappers == null) {
                dependWrappers = new ArrayList<>();
            }
            dependWrappers.add(dependWrapper);
            return this;
        }
        public Builder<W ,C> next(WorkerWrapper<? ,?> wrapper , boolean selfIsMust) {
            if (nextWrappers == null) {
                nextWrappers = new ArrayList<>();
            }
            nextWrappers.add(wrapper);

            if (selfIsMust) {
                if (selfIsMustSet == null) {
                    selfIsMustSet = new HashSet<>();
                }
                selfIsMustSet.add(wrapper);
            }
            return this;
        }
        public Builder<W ,C> next (WorkerWrapper<? ,?> wrapper) {
            return next(wrapper , true);
        }
        public Builder<W ,C> next(WorkerWrapper<? ,?> ... wrappers) {
            if (wrappers == null) {
                return this;
            }
            for (WorkerWrapper<? ,?> wrapper : wrappers) {
                next(wrapper);
            }
            return this;
        }
        public WorkerWrapper<W ,C> build() {
            WorkerWrapper<W ,C> wrapper = new WorkerWrapper<>(worker , callback , param);
            wrapper.setNeedCheckNextWrapperResult(needCheckNextWrapperResult);
            if (dependWrappers != null) {
                for (DependWrapper workerWrapper : dependWrappers) {
                    workerWrapper.getDependWrapper().addNext(wrapper);
                    wrapper.addDepend(workerWrapper);
                }
            }

            if (nextWrappers != null && !nextWrappers.isEmpty()) {
                for (WorkerWrapper<? ,?> workerWrapper : nextWrappers) {
                    boolean must = false;
                    if (selfIsMustSet != null && !selfIsMustSet.contains(workerWrapper)) {
                        must = true;
                    }
                    workerWrapper.addDepend(wrapper , must);
                    wrapper.addNext(workerWrapper);
                }
            }
            return wrapper;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkerWrapper<?, ?> that = (WorkerWrapper<?, ?>) o;
        return needCheckNextWrapperResult == that.needCheckNextWrapperResult && Objects.equals(param, that.param) && Objects.equals(worker, that.worker) && Objects.equals(callback, that.callback) && Objects.equals(nextWrappers, that.nextWrappers) && Objects.equals(dependWrappers, that.dependWrappers) && Objects.equals(state, that.state) && Objects.equals(workResult, that.workResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(param, worker, callback, nextWrappers, dependWrappers, needCheckNextWrapperResult, state, workResult);
    }
}
