package seq;

import com.sjc.async.executor.Async;
import com.sjc.async.executor.timer.SystemClock;
import com.sjc.async.wrapper.WorkerWrapper;

import java.util.concurrent.ExecutionException;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/25
 * {@code @msg} reserved
 */
// 测试Wrapper的串型执行 - 测试通过
public class TestSequential {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SeqWorker w = new SeqWorker();
        SeqWorker1 w1 = new SeqWorker1();
        SeqWorker2 w2 = new SeqWorker2();

        WorkerWrapper<String,String> workerWrapper2 = new WorkerWrapper.Builder<String , String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();
        WorkerWrapper<String,String> workerWrapper1 = new WorkerWrapper.Builder<String , String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(workerWrapper2)
                .build();
        WorkerWrapper<String,String> workerWrapper = new WorkerWrapper.Builder<String , String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1)
                .build();


        //testNormal(workerWrapper);
        testGroupTimeout(workerWrapper);

    }

    private static void testNormal(WorkerWrapper<String , String> workerWrapper) throws ExecutionException, InterruptedException {
        long now = SystemClock.now();
        System.out.println("begin - " + now);

        Async.beginWork(3500 , workerWrapper);
        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));

        Async.shutDown();
    }

    private static void testGroupTimeout(WorkerWrapper<String , String> workerWrapper) throws ExecutionException, InterruptedException {
        long now = SystemClock.now();
        System.out.println("begin - " + now);

        Async.beginWork(2500 , workerWrapper);
        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));

        Async.shutDown();
    }
}
