package restaurant.core;

import lombok.Getter;

import java.util.Map;

public class RestaurantConfig {
    private static final RestaurantConfig instance = new RestaurantConfig();

    public static RestaurantConfig getInstance() {
        return instance;
    }

    @Getter
    private Map<DishName, Dish> dishesMap;
    @Getter
    private Map<Integer, Table> tablesMap;

    private Restaurant restaurant;

    void fill(Restaurant restaurant) {
        this.dishesMap = restaurant.getDishByDishName();
        this.tablesMap = restaurant.getTableByTableNumber();
        this.restaurant = restaurant;
    }

    public void publishClientWaitingForReceiptEvent(Integer tableNumber) {
        restaurant.publishClientWaitingForReceiptEvent(tableNumber);
    }

    public void finishServingClient(Integer tableNumber) {
        restaurant.finishServingClient(tableNumber);
    }
}
