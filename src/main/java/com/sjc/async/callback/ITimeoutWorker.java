package com.sjc.async.callback;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/29
 * {@code @msg} reserved
 */
public interface ITimeoutWorker <T , V> extends IWorker{

    /**
     * 每个worker可以设置超时时间
     * @return 返回毫秒的超时时间
     */
    long timeOut ();

    /**
     * 是否开启单个执行单元的超时功能
     * 有时候一个group设置超时时间，而不用担心单个worker的超时时间
     * 开启单个任务超时检测后，线程池数量会多出一倍
     * @return ifTimeout - flag
     */
    boolean enableTimeout ();
}
