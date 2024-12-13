package depend;

import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.worker.WorkResult;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/12
 * {@code @msg} reserved
 */
public class DeWorker1 implements IWorker<WorkResult<User> , User> , ICallback<WorkResult<User> , User> {
    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "---start---" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, WorkResult<User> param, WorkResult<User> workResult) {
        System.out.println("worker1 - result - " + workResult.getResult());
    }

    @Override
    public User action(WorkResult<User> result) {
        System.out.println("par1的参数来源自par0 " + result.getResult());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new User("user1");
    }

    @Override
    public User defaultValue() {
        return new User("default - user");
    }
}
