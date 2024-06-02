package restaurant.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Cooker {
    private volatile boolean available;
    private DishName currentDish;

    public static Cooker availableFree() {
        return new Cooker(true, null);
    }

    public void free() {
        this.available = true;
        this.currentDish = null;
    }

    public void placeDish(Dish dish) {
        this.currentDish = dish.getDishName();
        this.available = false;
    }

}
