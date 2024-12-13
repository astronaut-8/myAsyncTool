package com.sjc.async.worker;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/30
 * {@code @msg} reserved
 */
// 执行结果
public class WorkResult<V> {

    private V result; // 结果

    private ResultState resultState; // 状态

    private Exception ex; // 异常

    public WorkResult(V result, ResultState resultState, Exception ex) {
        this.result = result;
        this.resultState = resultState;
        this.ex = ex;
    }
    public WorkResult(V result, ResultState resultState) {
       this(result, resultState, null);
    }

    public static <V> WorkResult<V> defaultResult () {
        return new WorkResult(null , ResultState.Default);
    }

    @Override
    public String toString() {
        return "WorkResult{" +
                "result=" + result +
                ", resultState=" + resultState +
                ", ex=" + ex +
                '}';
    }

    public V getResult() {
        return result;
    }

    public void setResult(V result) {
        this.result = result;
    }

    public ResultState getResultState() {
        return resultState;
    }

    public void setResultState(ResultState resultState) {
        this.resultState = resultState;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }
}
