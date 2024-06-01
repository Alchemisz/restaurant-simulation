package restaurant.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Infrastructure {

    public static List<Table> generateTables(Map<Integer, Integer> numOfSeatsToNumOfTablesMAP) {
        List<Table> results = new LinkedList<>();
        AtomicInteger counter = new AtomicInteger(1);


        numOfSeatsToNumOfTablesMAP.forEach((numberOfSeats, numberOfTables) -> {
            IntStream.range(0, numberOfTables).forEach(v -> {

                results.add(Table.builder()
                        .number(counter.getAndIncrement())
                        .seats(numberOfSeats)
                        .available(true)
                        .client(null)
                        .build());
            });

        });

        return results;
    }
}
