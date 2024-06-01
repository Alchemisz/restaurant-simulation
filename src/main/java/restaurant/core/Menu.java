package restaurant.core;

import java.util.Arrays;
import java.util.List;

public class Menu {
    public static List<Dish> getAllDishes() {
        return ALL_DISHES;
    }

    private static final List<Dish> ALL_DISHES = Arrays.asList(
        Dish.builder()
            .dishName(DishName.PIZZA)
            .preparingTime(Utils.convertToUnit(20))
            .cookingTime(Utils.convertToUnit(40))
            .price(2500)
            .build(),
        Dish.builder()
            .dishName(DishName.FISH)
            .preparingTime(Utils.convertToUnit(40))
            .cookingTime(Utils.convertToUnit(20))
            .price(2500)
            .build(),
        Dish.builder()
            .dishName(DishName.BURGER)
            .preparingTime(Utils.convertToUnit(20))
            .cookingTime(Utils.convertToUnit(30))
            .price(2500)
            .build(),
        Dish.builder()
            .dishName(DishName.CAKE)
            .preparingTime(Utils.convertToUnit(20))
            .cookingTime(Utils.convertToUnit(30))
            .price(2500)
            .build(),
        Dish.builder()
            .dishName(DishName.COLA)
            .preparingTime(Utils.convertToUnit(1))
            .cookingTime(Utils.convertToUnit(0))
            .price(2500)
            .build(),
        Dish.builder()
            .dishName(DishName.SALAD)
            .preparingTime(Utils.convertToUnit(5))
            .cookingTime(Utils.convertToUnit(0))
            .price(2500)
            .build(),
        Dish.builder()
            .dishName(DishName.PIEROGI)
            .preparingTime(Utils.convertToUnit(20))
            .cookingTime(Utils.convertToUnit(20))
            .price(2500)
            .build()
    );
}

