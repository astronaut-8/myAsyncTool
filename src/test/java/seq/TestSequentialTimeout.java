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
@SuppressWarnings("Duplicates")
public class TestSequentialTimeout {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        testFirstTimeout();
    }
    public static void testFirstTimeout () throws ExecutionException, InterruptedException {
        SeqWorker1 w1 = new SeqWorker1();
        SeqWorker2 w2 = new SeqWorker2();
        SeqTimeoutWorker t = new SeqTimeoutWorker();

        WorkerWrapper<String, String> workerWrapperT = new WorkerWrapper<>(t, t, "t");
        WorkerWrapper<String, String> workerWrapper1 = new WorkerWrapper<>(w1, w1, "1");
        WorkerWrapper<String, String> workerWrapper2 = new WorkerWrapper<>(w2, w2, "2");

        workerWrapper1.addNext(workerWrapper2);
        workerWrapperT.addNext(workerWrapper1);

        long now = SystemClock.now();
        System.out.println("begin - " + now);

        Async.beginWork(5000 , workerWrapperT);
        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));

        Async.shutDown();
    }
    public static void testSecondTimeout () throws ExecutionException, InterruptedException {
        SeqWorker1 w1 = new SeqWorker1();
        SeqWorker2 w2 = new SeqWorker2();
        SeqTimeoutWorker t = new SeqTimeoutWorker();

        WorkerWrapper<String, String> workerWrapperT = new WorkerWrapper<>(t, t, "t");
        WorkerWrapper<String, String> workerWrapper1 = new WorkerWrapper<>(w1, w1, "1");
        WorkerWrapper<String, String> workerWrapper2 = new WorkerWrapper<>(w2, w2, "2");

        workerWrapper1.addNext(workerWrapper2);
        workerWrapperT.addNext(workerWrapper1);

        long now = SystemClock.now();
        System.out.println("begin - " + now);

        Async.beginWork(5000 , workerWrapperT);
        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));

        Async.shutDown();
    }
}
