package restaurant.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

@Log4j
public class CookerManager {
    private final BlockingQueue<Cooker> availableCookers = new LinkedBlockingQueue<>();
    @Getter
    private final List<Cooker> allCookers = new LinkedList<>();

    public CookerManager(int cookersNumber) {
        IntStream.range(0, cookersNumber).forEach(x -> allCookers.add(Cooker.availableFree()));
        availableCookers.addAll(allCookers);
    }

    public Cooker take() {
        try {
            return availableCookers.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void waitForCookerAndCook(Dish dish) throws InterruptedException {
        log.info("Waiting for cooker");
        Cooker cooker = take();
        cooker.placeDish(dish);

        log.info("Start cooking");
        Thread.sleep(dish.getCookingTime());

        cooker.free();
        log.info("Meal has been cooked");
        availableCookers.add(cooker);
    }
}
