package depend;

import com.sjc.async.executor.Async;
import com.sjc.async.group.WorkerWrapper;
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

        WorkerWrapper<String , User> workerWrapper = new WorkerWrapper<>(w , w,"0");
        WorkResult<User> workResult = workerWrapper.getWorkResult(); // 经过上一个commit的优化 这里可以获取到result 了 result在初始化阶段就有了

        WorkerWrapper<WorkResult<User> , User> workerWrapper1 = new WorkerWrapper<>(w1 , w1 , workResult);
        WorkResult<User> workResult1 = workerWrapper1.getWorkResult();

        WorkerWrapper<WorkResult<User> , String> workerWrapper2 = new WorkerWrapper<>(w2 , w2 , workResult1);

        workerWrapper.addNext(workerWrapper1);
        workerWrapper1.addNext(workerWrapper2);

        Async.beginWork(3500 , workerWrapper);

        System.out.println(workerWrapper2.getWorkResult());

        Async.shutDown();
    }
}
