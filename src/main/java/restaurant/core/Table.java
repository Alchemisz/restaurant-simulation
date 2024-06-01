package restaurant.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Table {
    private final int number;
    private final int seats;

    private boolean available;
    private Client client = null;

    public static Table availableEmpty(Integer number, Integer seats) {
        return new Table(number, seats, true, null);
    }

    public void free() {
        this.available = true;
        this.client = null;
    }

    public void seat(Client client) {
        if (!this.available) {
            throw new IllegalStateException("Table is not available");
        }

        this.available = false;
        this.client = client;
    }

    public void letClientsStartEating() {
        if (client == null) {
            throw new IllegalStateException("There is no client for table " + this.number);
        }

        try {
            Thread.sleep(client.getServingTime());

            RestaurantConfig.getInstance().publishClientWaitingForReceiptEvent(number);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
