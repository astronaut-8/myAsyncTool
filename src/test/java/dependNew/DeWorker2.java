package dependNew;

import com.sjc.async.callback.ICallback;
import com.sjc.async.callback.IWorker;
import com.sjc.async.worker.WorkResult;
import com.sjc.async.wrapper.WorkerWrapper;

import java.util.Map;


/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/12/26
 * {@code @msg} reserved
 */
public class DeWorker2 implements IWorker<User , String> , ICallback<User , String> {
    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "-- start --" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, User param, WorkResult<String> workResult) {
        System.out.println("worker2 result - " + workResult.getResult());
    }

    @Override
    public String action(User object, Map<String, WorkerWrapper> allWrappers) {
        System.out.println("--------------");
        System.out.println("获取par1的执行结果 - " + allWrappers.get("second").getWorkResult());
        System.out.println("取par1的结果作为自己的入参，并给他增加点东西");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        User user0 = (User)allWrappers.get("second").getWorkResult().getResult();
        return user0.getName() + "worker2 added";
    }

    @Override
    public String defaultValue() {
        return "default";
    }
}
