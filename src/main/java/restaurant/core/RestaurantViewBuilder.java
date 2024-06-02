package restaurant.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import restaurant.view.CookView;
import restaurant.view.CookerView;
import restaurant.view.TableView;
import restaurant.view.WaiterView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantViewBuilder {

    private static Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

    static CookerView toFinalView(Cooker cooker) {
        return CookerView.builder()
            .available(true)
            .currentDish(null)
            .build();
    }

    static TableView toFinalView(Table table) {
        return TableView.builder()
            .available(true)
            .number(table.getNumber())
            .seats(table.getSeats())
            .numberOfSittingPeople(0)
            .build();
    }

    static CookView toFinalView(Cook cook) {
        return new CookView(
            translate(CookState.WAIT_FOR_TASK)
        );
    }

    static WaiterView toFinalView(Waiter waiter) {
        return new WaiterView(
            translate(WaiterState.WAITING_FOR_TASK),
            waiter.getCurrentTask()
        );
    }

    static CookView toView(Cook cook) {
        return CookView.builder()
            .state(translate(cook.getState()))
            .build();
    }

    static TableView toView(Table value) {
        return TableView.builder()
            .available(value.isAvailable())
            .number(value.getNumber())
            .seats(value.getSeats())
            .numberOfSittingPeople(value.getClient() != null ? value.getClient().getClientNumbers() : 0)
            .build();
    }

    static WaiterView toView(Waiter value) {
        return WaiterView.builder()
            .state(translate(value.getState()))
            .currentTask(value.getCurrentTask())
            .build();
    }

    static CookerView toView(Cooker value) {
        return CookerView.builder()
            .available(value.isAvailable())
            .currentDish(value.getCurrentDish())
            .build();
    }

    static String toDateFormat(long time) {
        return format.format(new Date(time));
    }

    private static String translate(WaiterState waiterState) {
        switch (waiterState) {
            case DOING_TASK:
                return "Wykonuje zadanie";
            case WAITING_FOR_TASK:
                return "Oczekuje na zadanie";
        }
        throw new IllegalStateException(String.format("Unhandled waiter state: %s", waiterState));
    }

    private static String translate(CookState cookState) {
        switch (cookState) {
            case WAIT_FOR_TASK:
                return "Oczekuje na zadanie";
            case PREPARING_MEAL:
                return "Przygotowuje posiłek";
            case WAITING_FOR_COOKED_MEAL:
                return "Oczekuje na ugotowanie posiłku";
        }
        throw new IllegalStateException(String.format("Unhandled cook state: %s", cookState));
    }
}
