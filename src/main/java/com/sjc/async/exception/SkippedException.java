package com.sjc.async.exception;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/25
 * {@code @msg} reserved
 */

/**
 *  如果任务在执行之前，自己后面的任务已经执行完毕或者正在执行，抛出这个exception
 */
public class SkippedException extends RuntimeException {
    public SkippedException( ) {
        super();
    }
    public SkippedException( String message ) {
        super(message);
    }
}
