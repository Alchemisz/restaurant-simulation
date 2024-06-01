package restaurant.view;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class RestaurantView {

    String startTime;
    String stopTime;

    long profit;

    List<TableView> tables;
    List<CookerView> cookers;

    List<CookView> cooks;
    List<WaiterView> waiters;
}
