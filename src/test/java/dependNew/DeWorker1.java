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
public class DeWorker1 implements IWorker<String , User> , ICallback<String , User> {
    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "-- start --" + System.currentTimeMillis());
    }

    @Override
    public void result(boolean success, String param, WorkResult<User> workResult) {
        System.out.println("worker1 result - " + workResult.getResult());
    }

    @Override
    public User action(String object, Map<String, WorkerWrapper> allWrappers) {
        System.out.println("--------------");
        System.out.println("获取par0的执行结果 - " + allWrappers.get("first").getWorkResult());
        System.out.println("取par0的结果作为自己的入参，并给他增加点东西");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        User user0 = (User)allWrappers.get("first").getWorkResult().getResult();
        return new User(user0.getName() + "worker1 added");
    }

    @Override
    public User defaultValue() {
        return new User("default - user");
    }
}
