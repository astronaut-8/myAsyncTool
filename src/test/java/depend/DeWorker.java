package depend;

import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.executor.timer.SystemClock;
import com.sjc.async.worker.WorkResult;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/12
 * {@code @msg} reserved
 */
public class DeWorker implements IWorker<String , User> , ICallback<String , User> {
    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "---start---" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, String param, WorkResult<User> workResult) {
        System.out.println("worker0 - result - " + workResult.getResult());
    }

    @Override
    public User action(String object) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new User("user0");
    }

    @Override
    public User defaultValue() {
        return new User("default - user");
    }
}
