package restaurant.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import restaurant.core.threads.ArrivingClientThread;
import restaurant.rest.SimulationOptions;
import restaurant.view.RestaurantView;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static restaurant.core.RestaurantViewBuilder.toDateFormat;

@Log4j
public class Restaurant {
    @Getter
    private final Map<DishName, Dish> dishByDishName;
    @Getter
    private final Map<Integer, Table> tableByTableNumber;
    private final Map<Integer, List<Integer>> tablesNumberBySeatsNumberMAP;

    private long startTime;
    private long closeTime;

    private ArrivingClientThread arrivingClientThread;
    private Thread managerThread;


    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private final BlockingQueue<Client> waitingClients = new LinkedBlockingQueue<>();

    private final WaiterTasksQueue waiterTasksQueue = new WaiterTasksQueue();
    private final CookTasksQueue cookTasksQueue = new CookTasksQueue();
    private final CookerManager cookerManager;

    private final AtomicBoolean restaurantClosed = new AtomicBoolean(false);

    private final List<Cook> allCooks;
    private final List<Waiter> allWaiters;

    private long profit;

    private Timer timeToCloseRestaurantChecker = new Timer();

    private final AtomicBoolean running = new AtomicBoolean(false);
    @Getter
    private SimulationOptions options;


    public Restaurant(SimulationOptions options) {
        this.options = options;
        this.dishByDishName = prepareDishes();
        this.tableByTableNumber = prepareTables(options);

        this.tablesNumberBySeatsNumberMAP = tableByTableNumber.values().stream()
            .collect(Collectors.groupingBy(Table::getSeats, Collectors.mapping(Table::getNumber, Collectors.toList())));

        Integer maxSeatsNumber = findMaxSeatsNumber();
        fillMissingSeatsNumber(maxSeatsNumber);

        this.cookerManager = new CookerManager(options.getCookersNumber());

        this.allCooks = prepareCooks(options);
        this.allWaiters = prepareWaiters(options);

        this.arrivingClientThread = new ArrivingClientThread(waitingClients);

        this.managerThread = new Thread(() -> {
            while (true) {
                try {
                    log.info("Room manager wait for client");
                    Client client = waitingClients.take();
                    log.info("Room manager assigning table for client " + client);
                    RestaurantTask orderTask = assignFreeTableToClient(client);
                    log.info("Room manager add task for waiter " + client);
                    waiterTasksQueue.add(orderTask);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        });

        this.allWaiters.forEach(w -> w.assignTaskQueue(waiterTasksQueue, cookTasksQueue));
        this.allCooks.forEach(w -> w.assignTaskQueue(cookerManager, waiterTasksQueue, cookTasksQueue));
    }

    private void fillMissingSeatsNumber(Integer maxSeatsNumber) {
        IntStream.rangeClosed(1, maxSeatsNumber).forEach(it -> {
            if (!tablesNumberBySeatsNumberMAP.containsKey(it)) {
                tablesNumberBySeatsNumberMAP.put(it, Collections.emptyList());
            }
        });
    }

    private Integer findMaxSeatsNumber() {
        return this.tablesNumberBySeatsNumberMAP.keySet().stream()
            .max(Integer::compareTo)
            .orElseThrow(() -> new IllegalStateException("There is no max numberOfSets - probably empty"));
    }

    private List<Cook> prepareCooks(SimulationOptions options) {
        return IntStream.range(0, options.getCooksNumber()).mapToObj(v -> new Cook()).collect(Collectors.toList());
    }

    private List<Waiter> prepareWaiters(SimulationOptions options) {
        return IntStream.range(0, options.getWaitersNumber()).mapToObj(v -> new Waiter()).collect(Collectors.toList());
    }

    public void start() {
        log.info("Start restaurant");
        Utils.changeScale(options.getTimeScale());
        running.set(true);
        RestaurantConfig.getInstance().fill(this);
        profit = 0;
        startTime = System.currentTimeMillis();

        allWaiters.forEach(Waiter::start);
        allCooks.forEach(Cook::start);
        managerThread.start();
        arrivingClientThread.start();

        closeTime = startTime + Utils.convertToUnit(1000);

        startTimerCheckingForRestaurantClosing();

        log.info("Start time: " + startTime);
        log.info("Close time: " + closeTime);
    }

    private void startTimerCheckingForRestaurantClosing() {
        restaurantClosed.set(false);

        timeToCloseRestaurantChecker.cancel();
        timeToCloseRestaurantChecker = new Timer();
        timeToCloseRestaurantChecker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (restaurantClosed.get()) {
                    return;
                }

                long currTime = System.currentTimeMillis();
                if (currTime >= closeTime) {
                    restaurantClosed.set(true);
                    timeToCloseRestaurantChecker.cancel();
                    closeRestaurant();
                }
            }
        }, 0, 200);
    }

    private void closeRestaurant() {
        allWaiters.forEach(Waiter::stop);
        allCooks.forEach(Cook::stop);
        managerThread.interrupt();
        arrivingClientThread.interrupt();
        log.info("Restaurant has been closed - profit " + (profit / 100.0));
        running.set(false);
    }

    private RestaurantTask assignFreeTableToClient(Client client) throws InterruptedException {
        Integer tableNumber = tryToFindFreeTableOrBlock(client);
        Table table = tableByTableNumber.get(tableNumber);
        table.seat(client);
        return new GetOrder(client, table);
    }

    private Integer tryToFindFreeTableOrBlock(Client client) throws InterruptedException {
        while (true) {
            Integer tableNumber = findAvailableTable(client);

            if (tableNumber == null) {
                countDownLatch = new CountDownLatch(1);
                countDownLatch.await();
            } else {
                return tableNumber;
            }
        }
    }

    private Integer findAvailableTable(Client client) {
        for (int seatsNumber = client.getClientNumbers(); tablesNumberBySeatsNumberMAP.containsKey(seatsNumber); ++seatsNumber) {

            List<Integer> tables = tablesNumberBySeatsNumberMAP.get(seatsNumber);
            Optional<Integer> avaliableTableNumber = tables.stream()
                .filter(tableNumber -> tableByTableNumber.get(tableNumber).isAvailable())
                .findFirst();

            if (avaliableTableNumber.isPresent()) {
                return avaliableTableNumber.get();
            }
        }

        return null;
    }

    private Map<DishName, Dish> prepareDishes() {
        return Menu.getAllDishes().stream()
            .collect(Collectors.toMap(Dish::getDishName, d -> d));
    }

    private Map<Integer, Table> prepareTables(SimulationOptions options) {
        return Infrastructure.generateTables(options.getSeatsToNumberOfTablesMap()).stream()
            .collect(Collectors.toMap(Table::getNumber, t -> t));
    }

    public void notifyRoomManager() {
        countDownLatch.countDown();
    }

    public void publishClientWaitingForReceiptEvent(Integer tableNumber) {
        this.waiterTasksQueue.add(new ClientWaitingForReceipt(tableNumber));
    }

    public void finishServingClient(Integer tableNumber) {
        Table table = RestaurantConfig.getInstance().getTablesMap().get(tableNumber);

        table.getClient().getDishes().stream()
            .map(this.dishByDishName::get)
            .filter(Objects::nonNull)
            .map(Dish::getPrice)
            .forEach(price -> this.profit += price);

        table.free();
        notifyRoomManager();
    }


    public RestaurantView read() {
        return RestaurantView.builder()
            .startTime(toDateFormat(startTime))
            .stopTime(toDateFormat(closeTime))
            .profit(profit)
            .cooks(allCooks.stream().map(RestaurantViewBuilder::toView).collect(Collectors.toList()))
            .waiters(allWaiters.stream().map(RestaurantViewBuilder::toView).collect(Collectors.toList()))
            .tables(tableByTableNumber.values().stream().map(RestaurantViewBuilder::toView).collect(Collectors.toList()))
            .cookers(cookerManager.getAllCookers().stream().map(RestaurantViewBuilder::toView).collect(Collectors.toList()))
            .nextStepAvailable(true)
            .build();
    }

    public RestaurantView finalView() {
        return RestaurantView.builder()
            .startTime(toDateFormat(startTime))
            .stopTime(toDateFormat(closeTime))
            .profit(profit)
            .cooks(allCooks.stream().map(RestaurantViewBuilder::toFinalView).collect(Collectors.toList()))
            .waiters(allWaiters.stream().map(RestaurantViewBuilder::toFinalView).collect(Collectors.toList()))
            .tables(tableByTableNumber.values().stream().map(RestaurantViewBuilder::toFinalView).collect(Collectors.toList()))
            .cookers(cookerManager.getAllCookers().stream().map(RestaurantViewBuilder::toFinalView).collect(Collectors.toList()))
            .nextStepAvailable(false)
            .build();
    }

    public boolean isRunning() {
        return running.get();
    }
}
