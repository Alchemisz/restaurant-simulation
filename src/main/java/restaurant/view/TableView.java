package restaurant.view;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TableView {
    int number;
    int seats;
    boolean available;
    public int numberOfSittingPeople;
}
