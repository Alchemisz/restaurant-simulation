package restaurant.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientArrivingTimeGenerator {

    private static final double LAMBDA = 2.0; // lambda dla średniego czasu 500 milisekund

    public static int getExponentialTime() {
        double exponentialTime = Math.log(1 - ThreadLocalRandom.current().nextDouble()) / (-LAMBDA);
        return (int) Math.round(exponentialTime * 1000);
    }

}
