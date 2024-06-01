package restaurant.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import restaurant.view.RestaurantView;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/restaurant/")
public class RestaurantRestController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping("start")
    public void start(@RequestBody SimulationOptions options) {
        restaurantService.start(options);
    }

    @GetMapping("read")
    public RestaurantView readView() {
        return restaurantService.readView();
    }

    @GetMapping("options")
    public SimulationOptions getOptions() {
        return restaurantService.getOptions();
    }
}
