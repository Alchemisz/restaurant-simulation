package restaurant;

import restaurant.core.Restaurant;
import restaurant.rest.SimulationOptions;

public class Main {

    public static void main(String[] args) {
        SimulationOptions options = SimulationOptions.builder()
                .cookersNumber(5)
                .cooksNumber(5)
                .waitersNumber(5)
                .build();

        Restaurant restaurant = new Restaurant(options);
        restaurant.start();
    }
}
