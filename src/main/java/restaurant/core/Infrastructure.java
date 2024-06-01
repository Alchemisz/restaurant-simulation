package restaurant.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Infrastructure {

    static List<Table> generateTables(Map<Integer, Integer> numberOfSeatsByNumberOfTables) {
        List<Table> tables = new LinkedList<>();
        int counter = 1;

        for (Map.Entry<Integer, Integer> entry : numberOfSeatsByNumberOfTables.entrySet()) {
            int numberOfSeats = entry.getKey();
            int numberOfTables = entry.getValue();

            for (int i = 0; i < numberOfTables; i++) {
                tables.add(Table.availableEmpty(counter++, numberOfSeats));
            }
        }

        return tables;
    }
}
