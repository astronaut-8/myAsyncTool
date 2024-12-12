package parallel;

import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.executor.timer.SystemClock;
import com.sjc.async.worker.WorkResult;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/12
 * {@code @msg} reserved
 */
public class ParWorker4 implements IWorker<String, String>, ICallback<String, String> {
    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "---start---" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, String param, WorkResult<String> workResult) {
        if (success) {
            System.out.println("callback worker4 success--" + SystemClock.now() + "----" + workResult.getResult()
                    + "-threadName: " + Thread.currentThread().getName());
        } else {
            System.out.println("callback worker4 failure--" + SystemClock.now() + "----" + workResult.getResult()
                    + "-threadName: " + Thread.currentThread().getName());
        }
    }

    @Override
    public String action(String object) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "result = " + SystemClock.now() + "---param" + object + "from 4";
    }

    @Override
    public String defaultValue() {
        return "worker4 == default";
    }
}
