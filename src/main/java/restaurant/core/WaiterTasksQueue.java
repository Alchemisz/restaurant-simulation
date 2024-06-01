package restaurant.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WaiterTasksQueue {
    private final BlockingQueue<RestaurantTask> restaurantTasks = new LinkedBlockingQueue<>();

    public void add(RestaurantTask task) {
        restaurantTasks.add(task);
    }

    public RestaurantTask take() {
        try {
            return restaurantTasks.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
