package com.sjc.async.callback;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/29
 * {@code @msg} reserved
 */

/**
 * 最小执行单元需要实现的接口
 * @param <T> 传入参数类型
 * @param <V> 返回值类型
 */
public interface IWorker<T , V> {

    /**
     * 任务操作 - 如 IO rpc
     */
    V action (T object);

    /**
     * 发生超时等异常的默认返回值
     */
    V defaultValue ();
}
