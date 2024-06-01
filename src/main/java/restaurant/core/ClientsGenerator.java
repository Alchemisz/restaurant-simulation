package restaurant.core;

import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor
public class ClientsGenerator {
    private final Random random = new Random();

    Client generateNewClient() {
        int clientNumbers = generateClientNumbersNormallyDistributed();
        int servingTimePerOne = random.nextInt(3) + 1;
        int eatingTimePerOne = random.nextInt(2) + 1;

        List<DishName> dishes = IntStream.range(0, clientNumbers)
            .mapToObj(id -> randomDish())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return Client.builder()
            .servingTime(Utils.convertToUnit(clientNumbers * servingTimePerOne))
            .eatingTime(Utils.convertToUnit(clientNumbers * eatingTimePerOne))
            .clientNumbers(clientNumbers)
            .dishes(dishes)
            .build();
    }

    private int generateClientNumbersNormallyDistributed() {
        double mean = 2.5;
        double stdDeviation = 1;
        int clientNumber;
        do {
            clientNumber = (int) Math.round(random.nextGaussian() * stdDeviation + mean);
        } while (clientNumber < 1 || clientNumber > 4);
        return clientNumber;
    }


    private List<DishName> randomDish() {
        int idx = random.nextInt(DishName.values().length);

        DishName[] vals = DishName.values();
        DishName dish = vals[idx];

        int tmp = random.nextInt(3);

        if (tmp == 2) {
            int idx2 = random.nextInt(DishName.values().length);
            return Arrays.asList(dish, vals[idx2]);
        }

        return Arrays.asList(dish);
    }
}
