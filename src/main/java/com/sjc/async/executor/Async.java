package com.sjc.async.executor;

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
// 启动类
@SuppressWarnings("ALL")
public class Async {
    public static final ThreadPoolExecutor COMMON_POOL =
            new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2 , 1024
            ,15L , TimeUnit.SECONDS , new LinkedBlockingDeque<>( ) , (ThreadFactory) Thread::new);

    public static void beginWork(long timeout , ThreadPoolExecutor pool , WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException {
        if (workerWrapper == null || workerWrapper.length == 0) {
            return;
        }
        List<WorkerWrapper> workerWrappers = Arrays.stream(workerWrapper).collect(Collectors.toList());

        CompletableFuture[] futures = new CompletableFuture[workerWrappers.size()];
        for (int i = 0 ; i < workerWrappers.size() ; i++) {
            WorkerWrapper wrapper = workerWrappers.get(i);
            futures[i] = CompletableFuture.runAsync(() -> wrapper.work(COMMON_POOL , timeout) , COMMON_POOL);
        }
        try {
            CompletableFuture.allOf(futures).get(timeout , TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            Set<WorkerWrapper> set = new HashSet<>();
            totalWorkers(workerWrappers , set);
            for (WorkerWrapper wrapper : set) {
                wrapper.stopNow();
            }
        }
    }
    public static void beginWork (long timeout , WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException {
        beginWork(timeout , COMMON_POOL , workerWrapper);
    }

    // 所有的执行单元
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
