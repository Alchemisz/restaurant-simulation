package restaurant.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
public class WaiterView {
    String state;
    String currentTask;
}
