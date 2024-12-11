package com.sjc.async.group;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/30
 * {@code @msg} reserved
 */

import com.sjc.async.callback.DefaultCallback;
import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.worker.DependWrapper;
import com.sjc.async.worker.WorkResult;

import java.awt.*;
import java.util.List;
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

    private ICallback<T ,V> callback;

    // 在自己后面的wrappers (很多的话要开多线程)
    private List<WorkerWrapper<? ,?>> nextWrappers;

    // 自己依赖的wrappers，全部依赖执行完成后才能执行自己
    private List<DependWrapper> dependWrappers;

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
    private volatile WorkResult<V> workResult;


    public WorkerWrapper(IWorker<T, V> worker, ICallback<T, V> callback, T param) {
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
    private void work(ThreadPoolExecutor poolExecutor , WorkerWrapper fromWrapper , long remainTime) {


    }
}
