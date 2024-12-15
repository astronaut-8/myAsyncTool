package com.sjc.async.callback;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/29
 * {@code @msg} reserved
 */

import com.sjc.async.worker.WorkResult;

/**
 * 每个worker执行完毕后会调用这个接口
 * 需要监听执行结果的，实现这个接口就可以了
 */
public interface ICallback <T , V> {

    void begin ();


    /**
     * 耗时操作执行完成后，给value注入值
     */
    void result (boolean success , T param , WorkResult<V> workResult);
}
