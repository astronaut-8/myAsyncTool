package com.sjc.async.executor;

import com.sjc.async.callback.DefaultGroupCallback;
import com.sjc.async.callback.IGroupCallback;
import com.sjc.async.group.WorkerWrapper;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/11
 * {@code @msg} reserved
 */
// 类入口 可以根据自己情况调整core线程的数量
@SuppressWarnings("ALL")
public class Async {
    public static final ThreadPoolExecutor COMMON_POOL =
            new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2 , 1024
            ,15L , TimeUnit.SECONDS , new LinkedBlockingDeque<>( ) , (ThreadFactory) Thread::new);

    public static boolean beginWork(long timeout , ThreadPoolExecutor pool , WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException {
        if (workerWrapper == null || workerWrapper.length == 0) {
            return false;
        }
        List<WorkerWrapper> workerWrappers = Arrays.stream(workerWrapper).collect(Collectors.toList());

        CompletableFuture[] futures = new CompletableFuture[workerWrappers.size()];
        for (int i = 0 ; i < workerWrappers.size() ; i++) {
            WorkerWrapper wrapper = workerWrappers.get(i);
            futures[i] = CompletableFuture.runAsync(() -> wrapper.work(pool , timeout) , pool);
        }
        try {
            CompletableFuture.allOf(futures).get(timeout , TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            Set<WorkerWrapper> set = new HashSet<>();
            totalWorkers(workerWrappers , set);
            for (WorkerWrapper wrapper : set) {
                wrapper.stopNow();
            }
            return false;
        }
    }
    public static boolean beginWork (long timeout , WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException {
        return beginWork(timeout , COMMON_POOL , workerWrapper);
    }

    /**
     *  异步执行，直到所有的都完成，或者失败后，发起回调
     */
    public static void beginWorkAsync (long timeout , IGroupCallback groupCallback , WorkerWrapper... workerWrapper) {
        if (groupCallback == null) {
            groupCallback = new DefaultGroupCallback();
        }
        IGroupCallback finalGroupCallback = groupCallback;
        CompletableFuture.runAsync(() -> {
            try {
                boolean success = beginWork(timeout, COMMON_POOL, workerWrapper);
                if (success) {
                    finalGroupCallback.success(Arrays.asList(workerWrapper));
                } else {
                    finalGroupCallback.failure(Arrays.asList(workerWrapper) , new TimeoutException());
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                finalGroupCallback.failure(Arrays.asList(workerWrapper) , e);
            }
        });
    }

    // 所有的执行单元
    @SuppressWarnings("unchecked") // 忽略 未经检查的类型转换的错误
    private static void totalWorkers(List<WorkerWrapper> workerWrappers, Set<WorkerWrapper> set) {
        set.addAll(workerWrappers);
        for (WorkerWrapper wrapper : workerWrappers) {
            if (wrapper.getNextWrappers() == null) {
                continue;
            }
            totalWorkers(wrapper.getNextWrappers() , set);
        }
    }

    public static void shutDown() {
        COMMON_POOL.shutdown();
    }


    // 池子的信息
    public static String getThreadCount () {
        return "activeCount - " + COMMON_POOL.getActiveCount() +
                " completedCount - " + COMMON_POOL.getCompletedTaskCount()
                + " largestCount - " + COMMON_POOL.getLargestPoolSize();
    }
}
