package com.sjc.async.executor.timer;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/11
 * {@code @msg} reserved
 */

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  解决高并发下System.currentTimeMills的卡顿问题
 */
public class SystemClock {
    private final int period;

    private final AtomicLong now;

    private static class InstanceHolder {
        private static final SystemClock INSTANCE = new SystemClock(1);
    }
    public SystemClock(int period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    //执行定时任务去跟新now -- 代表currentTimeMills
    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r , "System Clock");
                thread.setDaemon(true);
                return thread;
            }
        });
        // 执行的任务 第一次的开始时间 以后的间隔时间 时间的单位
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()) , period , period , TimeUnit.MILLISECONDS);
    }
    private long currentTimeMillis() {
        return now.get();
    }
    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    // 统一使用这个获取时间
    // 代替System.currentTimeMills
    public static long now () {
        return instance().currentTimeMillis();
    }
}
