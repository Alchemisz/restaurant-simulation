package restaurant.rest;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SimulationOptions {
    private int cooksNumber;
    private int cookersNumber;
    private int waitersNumber;

    private int timeScale;

    private Map<Integer, Integer> seatsToNumberOfTablesMap;
}
