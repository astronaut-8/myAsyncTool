package depend;

import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.worker.WorkResult;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/12
 * {@code @msg} reserved
 */
public class DeWorker2 implements IWorker<WorkResult<User> , String> , ICallback<WorkResult<User> , String> {
    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "---start---" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, WorkResult<User> param, WorkResult<String> workResult) {
        System.out.println("worker2 - result - " + workResult.getResult());
    }

    @Override
    public String action(WorkResult<User> result) {
        System.out.println("par2的参数来源自par1 " + result.getResult());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result.getResult().getName();
    }

    @Override
    public String defaultValue() {
        return "defaultValue";
    }
}
