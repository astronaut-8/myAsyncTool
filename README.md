# myAsyncTool
async tool

它的主要目的是方便开发者更高效地处理多个异步任务，比如并行执行任务、控制任务的超时、处理任务的回调等操作，从而提升系统的性能和响应速度。

解决任意的多线程并行、串行、阻塞、依赖、回调的并行框架，可以任意组合各线程的执行顺序，带全链路执行结果回调。多线程编排一站式解决方案。来自于京东主App后台。（搬运https://gitee.com/jd-platform-opensource/asyncTool）

当你组合了多个执行单元时，每一步的执行，都在掌控之内，可以详细指定action，在callback中去设置begin逻辑，以及结束后的result逻辑。失败了，还会有自定义的默认值。这是CompleteableFuture无法做到的。功能更加全面

# branch - V1.0

## 组件

worker - 任务执行单元

callBack - 每个worker的回调 有 begin 和 result 函数

一个wrapper 组合 worker 和 callBack 是最小的调度单元，编排wrapper的关系 达到组合各个worker的效果

一个 wrapper依赖的wrapper 会被包装成 DependWrapper(普通的一个wrapper + 是否必须要执行的must boolean 值)





## **ATTENTION**

在高并发环境下System.currentTimeMills 有卡顿问题

常用System.currentTimeMills 来记录任务开始结束时间，评估性能 在优化并行任务组的时候，用这个去做测试，优化任务执行顺序调整资源分配

对于超时时间检测机制的实现，对时间要求比较高，需要准确，并发环境下压力大



System.currentTimeMills 本身是一个轻量级操作，获取时间戳，对性能有影响，多次在业务中去调用这个，会导致系统在高并发下的响应变慢

高并发环境下，多个线程同时调用会产生资源竞争，虽然JVM对这个有缓存机制，但性能还是会有影响

解决措施 是 减少时间戳的调用获取 但是 本项目针对这个问题有一个 SystemClock的解决

**细说**

AsyncTool 中 有一个SystemClock 维护了一个静态原子变量now

当第一次调用这个类的获取时间戳的方法 开启一个循环线程

 对 now 做 now 被赋值 System.currentTimeMills的间隔为period的操作（周期设置为 1毫秒）

维护一个静态内部类 中去 创建这个工具类的静态实例 这样子 每次不用调用System.currentTimeMills

直接调用这个静态内部类的静态成员变量的获取 now的方法(封装在SystemClock中类) 返回now 

这样子不管怎样 每时每刻只会有一个线程做System.currentTimeMills的调用 减少了jvm资源冲突

原子(变量)操作确保了在**多线程环境下获取和更新时间戳的正确性和线程安全性。**



为什么每次重新赋值成System.currentTimeMills 而不是以周期1每次对now+1

- 保证时间可以和系统同步 符合时间的流逝和变化。。。。 这就是原来System.currentTimeMills的作用(保持)
- +1 操作还有很多细节， 如时间戳到最大后如何循环 直接赋值不用考虑时间戳的循环，复杂的递增逻辑在现代jvm的优化下，这种次数级别的调用不成问题
- 其实也就是维护了System.currentTimeMills原来的作用，别的组件也使用这个时间，防止和别的组件发生冲突



##  **工作流程**

定义好多个任务的 worker 和 callback 统一封装到 WrokerWrapper中去

任务之间 调用next 函数 可以把一个或者多个wrapper 作为自己的 next 并对这个next的depend添加当前wrapper

作为father wrapper 对它的next 有 是否为 must的标识

如果是must 那么 这个father wrapper 一定要执行完才能执行 next 也就是 next wrapper 要等待自己的所有 must wrapper 执行完毕 才能 执行 

不是must的father wrapper 执行完毕 beginNext(执行下一个 也就是 唤醒自己next列表中的 wrapper 进行work)

但是有些work由于不满足条件(有些must的father wrapper 没有执行完毕 每个 wrapper 自己工作的时候，会先判断自己的father列表中 如果有father 已经报错了--> 结果状态(ResutlState) 为exception 那么这个wrapper就不用执行了 ， 如果有father 还在运行中 那么这次被唤醒就无效 wrapper 不会真正去work)



wrapper work之后填充workerResult



一个 WorkerWrapper 中 有一个原子的状态值(AtomicInteger) 对一个int的State 只用volitail修饰 无法保证毫秒级别的多线程的值的修改和拉取

状态有

```java
private static final int INIT = 0;
private static final int FINISHED = 1;
private static final int ERROR = 2;
private static final int WORKING = 3;
```

在不同阶段会使用 原子操作的cas 去 修改这个状态值

有一个fastFail机制 执行到某些时段，发现这个work没有执行的必要了（时间超时了，发生异常了，father wrapper 出现异常了） 就直接快速失败 尝试去 把状态从传入的一个值改为ERROR cas是原子操作 防止多线程的并发问题 并为 workerResult 做default的赋值 再调用callback的回调函数



线程状态的转换分别在

callback调用前 也就是 wrapper的work正式开始后 从INIT -> WORKING

执行完 耗时操作 work真正内容后 从WORKING -> FINISHED

发生错误 fastFail 从上述三个状态改为ERROR



在最开始 会自动维护一个线程池 任务最开始 是执行最顶级的father wrapper 每个 father wrapper 如果只有一个next 直接用自己的这个线程继续工作

如果有很多个next 则会从线程池中去分配线程给自己的所有next 并行去执行这些任务



addDepend默认是自动must的，也就是添加的所有Depend 都要执行完 才能执行自己



提供一个setDependNotMust 方法 把一些wrapper设置成不是必须的



当控制台(Async 中的线程池 设置的总时间) 时间耗尽了

使用递归 把所有任务(包括它的next 任务) 全部加入一个set

对set中的每一个任务(INIT 或者 WORKING) 的fastFail 成 ERROR状态





如何控制 控制台的超时时间呢

普通任务使用CompletableFuture.runAsync 异步执行

使用CompletableFuture来进行异步任务的调度

使用allof 将所有的调度任务作为一个新的CompletableFuture的参数

这个CompletableFuture 去 get 等待timeout的时间

本身没有返回值 如果future任务都在timeout内执行完毕 返回null

否则直接报错

```java
CompletableFuture.allOf(futures).get(timeout , TimeUnit.MILLISECONDS);
```











------------------------

father wrapper 和 next wrapper 之间 数据传递的原理(next wrapper 的 入参  为 father wrapper的返回值)

更改了原来WorkerRsult的逻辑 在一个wrapper 初始化的时候就对这个reult进行赋值(状态设置为DEFAULT)

后期使用这个DEFAULT 状态值 来判断result是否被跟新过

在 wrapper 初期 对result赋值而不是 = null 的原因就在于 可以在一个wrapper 赋值完毕后，就去获得到它result的引用(wrapper的内部方法调用 只是在填充这个result 而不会重新new了)

拿到引用意味着 可以 提前把引用注入到 nextWrapper的入参中 而不用等到 一个 Wrapper执行完毕后才能获取result 再开启新的wrapper逻辑 (同步调用)

这个思想逻辑 其实很简单 也比较类似于 Spring 框架中对于 循环依赖的解决思路(不考虑proxy的时候) 







最后设置一个异步的执行

原来的版本中 调用Async.beginWork 这个操作是同步的 增加一个 beginWorkAsync 异步得去开始这个任务组

并且设置GroupCallback 群组的统一 对结果进行处理

```java
/**
 *  如果是异步执行整组的话，可以用这个组回调 (不推荐使用)
 */
public interface IGroupCallback {
    // 成功和失败都可以从wrapper里去getWrapper

    void success (List<WorkerWrapper> workerWrappers);

    void failure (List<WorkerWrapper> workerWrappers , Exception e);
}
```

思路清晰 实现简单



别的小东西 不是很重要 源码的V1.0 就结束了
