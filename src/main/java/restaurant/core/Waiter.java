package restaurant.core;


import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Log4j
public class Waiter {

    private Thread thread;
    private WaiterTasksQueue waiterTasksQueue;
    private CookTasksQueue cookTasksQueue;

    @Getter
    private WaiterState state;
    @Getter
    private String currentTask;

    public void assignTaskQueue(WaiterTasksQueue waiterTasksQueue, CookTasksQueue cookTasksQueue) {
        this.waiterTasksQueue = waiterTasksQueue;
        this.cookTasksQueue = cookTasksQueue;
    }


    public void start() {
        thread = new Thread(() -> {
            while (true) {
                doWaiterJob();
            }
        });

        thread.start();
    }

    public void doWaiterJob() {
        currentTask = null;
        state = WaiterState.WAITING_FOR_TASK;

        RestaurantTask task = waiterTasksQueue.take();
        log.info("Waiter take task " + task.getClass().getSimpleName());

        currentTask = task.getClass().getSimpleName();

        state = WaiterState.DOING_TASK;
        task.startDoing();
        RestaurantTask resultTask = task.finish();
        log.info("Waiter finish task " + task.getClass().toString());
        if (resultTask instanceof PrepareDish) {
            cookTasksQueue.add(resultTask);
        }
    }

    public void stop() {
        thread.interrupt();
    }
}
