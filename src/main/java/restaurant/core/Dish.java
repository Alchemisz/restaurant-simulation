package restaurant.core;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@ToString
public class Dish {
    DishName dishName;
    long preparingTime;
    long cookingTime;
    long price; // GR
}
