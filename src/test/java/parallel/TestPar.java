package parallel;

import com.sjc.async.executor.Async;
import com.sjc.async.executor.timer.SystemClock;
import com.sjc.async.wrapper.WorkerWrapper;

import java.util.concurrent.ExecutionException;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/12
 * {@code @msg} reserved
 */
public class TestPar {
    public static void main(String[] args) throws Exception {
        //testNormal();
        //testMulti();
        //testMulti3();
        //testMulti4();
        //testMulti5();
        //testMulti6();
        //testMulti7();
        //testMulti8();
        //testMulti9();
        //testMultiReverse();
        //testMulti3Reserve();
        //testMulti9Reverse();
        //testMulti4Reserve();
        testMulti5Reserve();
    }

    /**
     * 测试三个并行执行
     * 通过
     */
    public static void testNormal() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();

        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .build();
        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        long now = SystemClock.now();

        Async.beginWork(1500, workerWrapper, workerWrapper1, workerWrapper2);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        System.out.println("work0 - result -  " + workerWrapper.getWorkResult());
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    /**
     * 0，2同时开始 1在0后面
     * 0 --- 1
     * 2
     * 时间不够串型执行 被Async终止 所以这个终止操作的线程为main
     * 测试通过
     */
    private static void testMulti() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();

        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .build();
        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1)
                .build();


        long now = SystemClock.now();
        System.out.println("begin - " + now);
        Async.beginWork(3000, workerWrapper, workerWrapper2); // 控制台时间设置为1500 测试 work1无法完成的情况 由控制台去stop 给那些work做fastFail

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }
    /**
     * 0，2同时开始 1在0后面
     * 0 --- 1
     * 2
     * 测试通过
     */
    private static void testMultiReverse () throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();

        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(workerWrapper)
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();

        long now = SystemClock.now();
        System.out.println("begin - " + now);
        Async.beginWork(3000, workerWrapper, workerWrapper2); // 控制台时间设置为1500 测试 work1无法完成的情况 由控制台去stop 给那些work做fastFail

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    /**
     * 1
     * 0       3
     * 2
     * 3 等待 1 2
     * 测试通过
     */
    private static void testMulti3() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        ParWorker3 w3 = new ParWorker3();

        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1 , workerWrapper2)
                .build();


        long now = SystemClock.now();
        System.out.println("begin - " + now);
        //Async.beginWork(2100 , workerWrapper);
        Async.beginWork(3100, workerWrapper);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    private static void testMulti3Reserve() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        ParWorker3 w3 = new ParWorker3();

        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .depend(workerWrapper)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(workerWrapper)
                .build();
        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .depend(workerWrapper1 , workerWrapper2)
                .build();





        long now = SystemClock.now();
        System.out.println("begin - " + now);
        //Async.beginWork(2100 , workerWrapper);
        Async.beginWork(3100, workerWrapper);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    /**
     * 1
     * 0       3
     * 2
     * 3 等待 1 2
     * 1 2 执行时间不一样的情况 3 等待 1 2 全都执行完毕
     * 测试通过
     */
    private static void testMulti4() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(2000);
        ParWorker3 w3 = new ParWorker3();

        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1 , workerWrapper2)
                .build();




        long now = SystemClock.now();
        System.out.println("begin - " + now);
        // 正常完毕
        //Async.beginWork(4100 , workerWrapper);
        // 3超时
        //Async.beginWork(3100 , workerWrapper);
        // 2 ，3 超时
        Async.beginWork(2900, workerWrapper); // 这里都没有执行到 3 控制台就没时间了 2会唤醒3 3进行一次failFast 所以 3会有两次 failFast 一次是主动的 一次是控制台对其被动的

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }
    private static void testMulti4Reserve() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(2000);
        ParWorker3 w3 = new ParWorker3();

        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();
        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .depend(workerWrapper)
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(workerWrapper)
                .next(workerWrapper3)
                .build();




        long now = SystemClock.now();
        System.out.println("begin - " + now);
        // 正常完毕
        Async.beginWork(4100 , workerWrapper);
        // 3超时
        //Async.beginWork(3100 , workerWrapper);
        // 2 ，3 超时
        //Async.beginWork(2900, workerWrapper); // 这里都没有执行到 3 控制台就没时间了 2会唤醒3 3进行一次failFast 所以 3会有两次 failFast 一次是主动的 一次是控制台对其被动的

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    /**
     * 1
     * 0       3
     * 2
     * 3 等待 1 2 任意一个执行完就可以了
     * 测试通过
     */
    private static void testMulti5() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(500);
        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(400);

        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(workerWrapper3 , false)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(workerWrapper3 , false)
                .build();
        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1 , workerWrapper2)
                .build();




        long now = SystemClock.now();
        System.out.println("begin - " + now);
        // 正常完毕
        Async.beginWork(4100, workerWrapper);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }
    private static void testMulti5Reserve() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(500);
        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(400);

        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();
        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .depend(workerWrapper , true)
                .next(workerWrapper3 , false)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(workerWrapper , true)
                .next(workerWrapper3 , false)
                .build();




        long now = SystemClock.now();
        System.out.println("begin - " + now);
        // 正常完毕
        Async.beginWork(4100, workerWrapper);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    /**
     *      1
     * 0       3
     *      2
     * 3 等待 1 2
     * 1 是必须的 所以 不管2 快不快 一定要等1
     * 测试通过
     */
    private static void testMulti6() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(500);
        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(400);

        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(workerWrapper3 , false)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(workerWrapper3 )
                .build();
        WorkerWrapper<String ,String> workerWrapper = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1 , workerWrapper2)
                .build();


        long now = SystemClock.now();
        System.out.println("begin - " + now);
        // 正常完毕
        Async.beginWork(4100, workerWrapper);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));
        System.out.println("threadCount - " + Async.getThreadCount());

        Async.shutDown();
    }

    /**
     * 两个0并行，上面0执行完,同时1和2, 下面0执行完开始1，上面的 必须1、2执行完毕后，才能执行3. 最后必须2、3都完成，才能4
     *      1
     * 0        3
     *      2        4
     * ---------
     * 0   1    2
     */
    private static void testMulti7() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        ParWorker3 w3 = new ParWorker3();
        ParWorker4 w4 = new ParWorker4();

        WorkerWrapper<String ,String> workerWrapper4 = new WorkerWrapper.Builder<String ,String>()
                .worker(w4)
                .callback(w4)
                .param("4")
                .build();
        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .next(workerWrapper4)
                .build();
        WorkerWrapper<String ,String> workerWrapper22 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("22")
                .next(workerWrapper4)
                .build();
        WorkerWrapper<String ,String> workerWrapper11 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("11")
                .next(workerWrapper22)
                .build();

        WorkerWrapper<String ,String> workerWrapper00 = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("00")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapper0 = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(workerWrapper1, workerWrapper2)
                .build();


        long now = SystemClock.now();
        System.out.println("begin - " + now);

        // 正常完毕
        Async.beginWork(4100 , workerWrapper00 , workerWrapper0);

        System.out.println("end - " + SystemClock.now());
        System.out.println("cost - " + (SystemClock.now() - now));

        System.out.println("threadCount - " + Async.getThreadCount());
        Async.shutDown();
    }

    /**
     * a1 -> b -> c
     * a2 -> b -> c
     * 高并发环境下 不能同时进入doDependsJob
     */
    private static void testMulti8() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        w1.setSleepTime(1005);
        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(3000);
        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(1000);

        WorkerWrapper<String ,String> workerWrapper3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("c")
                .build();
        WorkerWrapper<String ,String> workerWrapper2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("b")
                .next(workerWrapper3)
                .build();
        WorkerWrapper<String ,String> workerWrapperA1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("a1")
                .next(workerWrapper2)
                .build();
        WorkerWrapper<String ,String> workerWrapperA2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("a2")
                .next(workerWrapper2)
                .build();


        Async.beginWork(6000 , workerWrapperA1 , workerWrapperA2);
        Async.shutDown();
    }

    /**
     *  w1 - w2 - w3
     *               last
     *     w
     *   w1 w 并行执行，w执行完毕执行last bc不用开始了
     */
    private static void testMulti9 () throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        ParWorker3 w3 = new ParWorker3();
        ParWorker4 w4 = new ParWorker4();

        WorkerWrapper<String ,String> last = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("last")
                .build();
        WorkerWrapper<String ,String> wrapperW = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("w")
                .next(last , false)
                .build();
        WorkerWrapper<String ,String> wrapperW3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("w3")
                .next(last , false)
                .build();
        WorkerWrapper<String ,String> wrapperW2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("w2")
                .next(wrapperW3)
                .build();
        WorkerWrapper<String ,String> wrapperW1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("w1")
                .next(wrapperW2)
                .build();




        Async.beginWork(6000 , wrapperW , wrapperW1);
        Async.shutDown();

    }
    private static void testMulti9Reverse () throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        ParWorker3 w3 = new ParWorker3();
        ParWorker4 w4 = new ParWorker4();

        WorkerWrapper<String ,String> wrapperW1 = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("w1")
                .build();

        WorkerWrapper<String ,String> wrapperW = new WorkerWrapper.Builder<String ,String>()
                .worker(w)
                .callback(w)
                .param("w")
                .build();

        WorkerWrapper<String ,String> last = new WorkerWrapper.Builder<String ,String>()
                .worker(w1)
                .callback(w1)
                .param("last")
                .depend(wrapperW)
                .build();

        WorkerWrapper<String ,String> wrapperW2 = new WorkerWrapper.Builder<String ,String>()
                .worker(w2)
                .callback(w2)
                .param("w2")
                .depend(wrapperW1)
                .build();
        WorkerWrapper<String ,String> wrapperW3 = new WorkerWrapper.Builder<String ,String>()
                .worker(w3)
                .callback(w3)
                .param("w3")
                .depend(wrapperW2)
                .next(last , false)
                .build();

        Async.beginWork(6000 , wrapperW , wrapperW1);
        Async.shutDown();

    }
}
