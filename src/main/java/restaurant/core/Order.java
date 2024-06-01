package restaurant.core;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Order {
    List<DishName> dishes;
    int tableNumber;
}
