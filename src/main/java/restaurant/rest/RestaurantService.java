package restaurant.rest;

import org.springframework.stereotype.Service;
import restaurant.core.Restaurant;
import restaurant.view.RestaurantView;

@Service
public class RestaurantService {
    private Restaurant restaurant = null;


    public void start(SimulationOptions options) {
        if (restaurant != null && restaurant.isRunning()) {
            throw new IllegalStateException("Simulation is still running");
        }

        restaurant = new Restaurant(options);
        restaurant.start();
    }

    public RestaurantView readView() {
        if (restaurant == null) {
            return null;
        }

        if (restaurant.isRunning()){
            return restaurant.read();
        }else {
            return restaurant.finalView();
        }
    }

    public SimulationOptions getOptions() {
        if (restaurant == null) {
            return null;
        }

        return restaurant.getOptions();
    }
}
