package com.sjc.async.group;

/*
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/12
 * {@code @msg} reserved
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 暂时用不到
 */
public class WorkerGroup {

    private List<WorkerWrapper<?, ?>> workerWrapperList;
    // begin 时候的 wrappers
    private List<WorkerWrapper<?, ?>> beginList;

    public WorkerGroup() {
        workerWrapperList = new ArrayList<>();
    }

    // 起始任务
    public WorkerGroup begin(WorkerWrapper<?, ?>... workerWrappers) {
        if (workerWrappers == null) {
            throw new NullPointerException("workerWrappers cannot be null");
        }
        beginList = Arrays.asList(workerWrappers);
        return this;
    }

    public WorkerGroup then(WorkerWrapper<?, ?>... workerWrappers) {
        if (workerWrappers == null) {
            throw new NullPointerException("workerWrappers cannot be null");
        }
        beginList = Arrays.asList(workerWrappers);
        return this;
    }
    public WorkerGroup addWrappers(List<WorkerWrapper<?, ?>> workerWrappers) {
        if (workerWrappers == null) {
            throw new NullPointerException("workerWrappers cannot be null");
        }
        this.workerWrapperList.addAll(workerWrappers);
        return this;
    }
    public WorkerGroup addWrappers(WorkerWrapper<?, ?>... workerWrappers) {
        if (workerWrappers == null) {
            throw new NullPointerException("workerWrappers cannot be null");
        }
        return addWrappers(Arrays.asList(workerWrappers));
    }

    // 返回当前worker数量，用于决定启用的线程数量
    public int size() {
        synchronized (this) {
            return workerWrapperList.size();
        }
    }

    public List<WorkerWrapper<?, ?>> getWorkerWrapperList() {
        return workerWrapperList;
    }
}
