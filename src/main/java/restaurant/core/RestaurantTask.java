package restaurant.core;

import lombok.Getter;

import java.util.List;

public interface RestaurantTask {
    void startDoing();

    RestaurantTask finish();

}


class DishIsReady implements RestaurantTask {

    private final Order order;

    public DishIsReady(Order order) {
        this.order = order;
    }

    @Override
    public void startDoing() {
        Table table = RestaurantConfig.getInstance().getTablesMap().get(order.getTableNumber());
        table.letClientsStartEating();
    }

    @Override
    public RestaurantTask finish() {
        return null;
    }
}

class GetOrder implements RestaurantTask {

    private final int tableNumber;
    private final List<DishName> dishes;
    long servingTime;

    public GetOrder(Client client, Table table) {
        this.servingTime = client.getServingTime();
        this.tableNumber = table.getNumber();
        this.dishes = client.getDishes();
    }

    @Override
    public void startDoing() {
        try {
            Thread.sleep(servingTime);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public RestaurantTask finish() {
        Order order = Order.builder().tableNumber(tableNumber).dishes(dishes).build();

        return new PrepareDish(order);
    }
}

@Getter
class PrepareDish implements RestaurantTask {

    private final Order order;

    public PrepareDish(Order order) {
        this.order = order;
    }

    @Override
    public void startDoing() {

    }

    @Override
    public RestaurantTask finish() {
        return null;
    }
}

class ClientWaitingForReceipt implements RestaurantTask {

    private final Integer tableNumber;

    ClientWaitingForReceipt(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    @Override
    public void startDoing() {
        try {
            Thread.sleep(Utils.convertToUnit(2));
            RestaurantConfig.getInstance().finishServingClient(tableNumber);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public RestaurantTask finish() {
        return null;
    }
}