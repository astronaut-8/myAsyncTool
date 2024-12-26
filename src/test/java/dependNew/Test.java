package dependNew;

import com.sjc.async.executor.Async;
import com.sjc.async.wrapper.WorkerWrapper;

import java.util.concurrent.ExecutionException;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/26
 * {@code @msg} reserved
 */
public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        DeWorker w = new DeWorker();
        DeWorker1 w1 = new DeWorker1();
        DeWorker2 w2 = new DeWorker2();

        WorkerWrapper<User , String> workerWrapper2 = new WorkerWrapper.Builder<User , String>()
                .worker(w2)
                .callback(w2)
                .id("third")
                .build();

        WorkerWrapper<String , User> workerWrapper1 = new WorkerWrapper.Builder<String , User>()
                .worker(w1)
                .callback(w1)
                .id("second")
                .next(workerWrapper2)
                .build();
        WorkerWrapper<String , User> workerWrapper = new WorkerWrapper.Builder<String , User>()
                .worker(w)
                .callback(w)
                .id("first")
                .next(workerWrapper1)
                .param("0")
                .build();

        Async.beginWork(3500 , workerWrapper); // v1.3之后 不用给wrapper setParam 直接在worker的action里面根据id去获取
        System.out.println(workerWrapper2.getWorkResult());
        Async.shutDown();
    }
}
