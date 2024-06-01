package restaurant.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Log4j
public class Cook {

    private WaiterTasksQueue waiterTasksQueue;
    private CookTasksQueue cookTasksQueue;
    private CookerManager cookerManager;
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Getter
    private CookState state;
    private Thread cookThread;

    public void assignTaskQueue(CookerManager cookerManager, WaiterTasksQueue waiterTasksQueue, CookTasksQueue cookTasksQueue) {
        this.cookerManager = cookerManager;
        this.waiterTasksQueue = waiterTasksQueue;
        this.cookTasksQueue = cookTasksQueue;
    }

    public void start() {
        cookThread = new Thread(() -> {
            while (true) {
                try {
                    doCookJob();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        });

        cookThread.start();
    }

    private void doCookJob() throws InterruptedException {
        state = CookState.WAIT_FOR_TASK;

        RestaurantTask task = cookTasksQueue.take();

        if (task instanceof PrepareDish) {
            waiterTasksQueue.add(prepareDish((PrepareDish) task));
        }
    }

    private DishIsReady prepareDish(PrepareDish task) throws InterruptedException {
        state = CookState.PREPARING_MEAL;

        Order order = task.getOrder();
        log.info("Cook start preparing meal " + order.getDishes() + " for table " + order.getTableNumber());

        Map<DishName, Dish> menu = RestaurantConfig.getInstance().getDishesMap();
        List<Dish> allDishesInOrder = order.getDishes().stream().map(menu::get).filter(Objects::nonNull).collect(Collectors.toList());

        List<Future<?>> cookingMeals = new ArrayList<>();

        for (Dish dish : allDishesInOrder) {
            log.info("Preparing meal " + dish);

            Thread.sleep(dish.getPreparingTime());
            Future<?> result = cookMeal(dish);

            cookingMeals.add(result);
        }

        state = CookState.WAITING_FOR_COOKED_MEAL;
        cookingMeals.stream().map((future) -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }).collect(Collectors.toList());

        log.info("Dish is ready");
        return new DishIsReady(order);
    }

    private Future<?> cookMeal(Dish dish) {
        return executor.submit(() -> {
            try {
                this.cookerManager.waitForCookerAndCook(dish);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    public void stop() {
        executor.shutdown();
        cookThread.interrupt();
    }
}
