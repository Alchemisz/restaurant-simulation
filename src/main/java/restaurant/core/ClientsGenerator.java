package restaurant.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientsGenerator {
    private static final Random random = new Random();

    public static Client generateNewClient() {
        int clientNumbers = generateClientNumbersNormallyDistributed();
        int servingTimePerOne = random.nextInt(3) + 1;
        int eatingTimePerOne = random.nextInt(2) + 1;

        return Client.builder()
            .servingTime(Utils.convertToUnit((long) clientNumbers * servingTimePerOne))
            .eatingTime(Utils.convertToUnit((long) clientNumbers * eatingTimePerOne))
            .clientNumbers(clientNumbers)
            .dishes(generateDishes(clientNumbers))
            .build();
    }

    private static int generateClientNumbersNormallyDistributed() {
        double mean = 2.5;
        double stdDeviation = 1;
        int clientNumber;
        do {
            clientNumber = (int) Math.round(random.nextGaussian() * stdDeviation + mean);
        } while (clientNumber < 1 || clientNumber > 4);
        return clientNumber;
    }

    private static List<DishName> generateDishes(int clientNumbers) {
        return Stream.generate(ClientsGenerator::randomDishes)
            .limit(clientNumbers)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private static List<DishName> randomDishes() {
        int idx = random.nextInt(DishName.values().length);

        DishName[] vals = DishName.values();
        DishName dish = vals[idx];

        int tmp = random.nextInt(3);

        if (tmp == 2) {
            int idx2 = random.nextInt(DishName.values().length);
            return Arrays.asList(dish, vals[idx2]);
        }

        return Collections.singletonList(dish);
    }
}
