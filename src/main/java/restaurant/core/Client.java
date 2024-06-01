package restaurant.core;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Builder
@Value
@ToString
public class Client {
    long servingTime;
    long eatingTime;
    int clientNumbers;
    List<DishName> dishes;
}
