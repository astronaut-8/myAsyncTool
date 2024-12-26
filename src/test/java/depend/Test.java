package depend;

import com.sjc.async.executor.Async;
import com.sjc.async.wrapper.WorkerWrapper;
import com.sjc.async.worker.WorkResult;

import java.util.concurrent.ExecutionException;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/13
 * {@code @msg} reserved
 */
public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        DeWorker w = new DeWorker();
        DeWorker1 w1 = new DeWorker1();
        DeWorker2 w2 = new DeWorker2();

        WorkerWrapper<WorkResult<User> , String> workerWrapper2 = new WorkerWrapper.Builder<WorkResult<User> , String>()
                .worker(w2)
                        .callback(w2)
                                .build();

        WorkerWrapper<WorkResult<User> , User> workerWrapper1 = new WorkerWrapper.Builder<WorkResult<User> , User>()
                .worker(w1)
                .callback(w1)
                .next(workerWrapper2)
                .build();
        WorkerWrapper<String , User> workerWrapper = new WorkerWrapper.Builder<String , User>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1)
                .build();

        WorkResult<User> result = workerWrapper.getWorkResult();
        WorkResult<User> result1 = workerWrapper1.getWorkResult();

        workerWrapper1.setParam(result);
        workerWrapper2.setParam(result1);

        Async.beginWork(3500 , workerWrapper);
        System.out.println(workerWrapper2.getWorkResult());
        Async.shutDown();
    }
}
