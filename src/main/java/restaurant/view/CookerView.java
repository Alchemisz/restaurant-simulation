package restaurant.view;

import lombok.Builder;
import lombok.Value;
import restaurant.core.DishName;

@Value
@Builder
public class CookerView {
    boolean available;
    DishName currentDish;
}
