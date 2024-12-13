package com.sjc.async.callback;

import com.sjc.async.worker.WorkResult;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/30
 * {@code @msg} reserved
 */

/**
 * 默认回调类，如果不设置的话，会默认给这个回调
 */
public class DefaultCallback <T ,V> implements ICallback<T , V> {
    @Override
    public void begin() {

    }

    @Override
    public void result(boolean success, T param, WorkResult<V> workResult) {

    }
}
