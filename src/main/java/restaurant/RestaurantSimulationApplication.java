package restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestaurantSimulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantSimulationApplication.class, args);
    }

}
