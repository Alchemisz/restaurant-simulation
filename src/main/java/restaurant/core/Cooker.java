package restaurant.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Cooker {
    private volatile boolean available;
    private DishName currentDish;

    public void free() {
        this.available = true;
        this.currentDish = null;
    }

    public void placeDish(Dish dish) {
        this.currentDish = dish.getDishName();
        this.available = false;
    }

}
