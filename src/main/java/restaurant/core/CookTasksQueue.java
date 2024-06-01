package restaurant.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CookTasksQueue {
    private final BlockingQueue<RestaurantTask> tasks = new LinkedBlockingQueue<>();

    public void add(RestaurantTask task) {
        tasks.add(task);
    }

    public RestaurantTask take() {
        try {
            return tasks.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
