package com.sjc.async.callback;

import com.sjc.async.group.WorkerWrapper;

import java.util.List;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/30
 * {@code @msg} reserved
 */

/**
 *  如果是异步执行整组的话，可以用这个组回调 (不推荐使用)
 */
public interface IGroupCallback {
    // 成功和失败都可以从wrapper里去getWrapper

    void success (List<WorkerWrapper> workerWrappers);

    void failure (List<WorkerWrapper> workerWrappers , Exception e);
}
