package com.sjc.async.callback;

import com.sjc.async.group.WorkerWrapper;

import java.util.List;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/14
 * {@code @msg} reserved
 */
public class DefaultGroupCallback implements IGroupCallback{
    @Override
    public void success(List<WorkerWrapper> workerWrappers) {

    }

    @Override
    public void failure(List<WorkerWrapper> workerWrappers, Exception e) {

    }
}
