package restaurant.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import restaurant.rest.SimulationOptions;
import restaurant.view.*;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j
public class Restaurant {
    @Getter
    private final Map<DishName, Dish> dishesMap;
    @Getter
    private final Map<Integer, Table> tablesMap;
    private final Map<Integer, List<Integer>> tablesNumberBySeatsNumberMAP;

    private long startTime;
    private long closeTime;

    private Thread arrivingClientThread;
    private Thread managerThread;
    private final ClientsGenerator clientsGenerator = new ClientsGenerator();


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

    private Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

    public Restaurant(SimulationOptions options) {
        this.options = options;
        this.dishesMap = prepareDishes();
        this.tablesMap = prepareTables(options);

        this.tablesNumberBySeatsNumberMAP = tablesMap.values().stream()
            .collect(Collectors.groupingBy(Table::getSeats, Collectors.mapping(Table::getNumber, Collectors.toList())));

        Integer maxKey = this.tablesNumberBySeatsNumberMAP.keySet().stream()
            .max(Integer::compareTo)
            .orElseThrow(() -> new IllegalStateException("There is no max numberOfSets - probably empty"));

        IntStream.rangeClosed(1, maxKey).forEach(key -> {
            if (!tablesNumberBySeatsNumberMAP.containsKey(key)) {
                tablesNumberBySeatsNumberMAP.put(key, Collections.emptyList());
            }
        });

        cookerManager = new CookerManager(options.getCookersNumber());

        this.allCooks = prepareCooks(options);
        this.allWaiters = prepareWaiters(options);

        this.arrivingClientThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(Utils.convertToUnit(1));
                    Client client = clientsGenerator.generateNewClient();
                    log.info("New client arrived" + client);
                    waitingClients.add(client);

                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }

        });


        this.managerThread = new Thread(() -> {
            while (true) {
                try {
                    log.info("Room manager wait for client");
                    Client client = waitingClients.take();
                    log.info("Room manager assigning table for client " + client);
                    RestaurantTask task = assignTable(client);

                    log.info("Room manager add task for waiter " + client);

                    waiterTasksQueue.add(task);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        });


        this.allWaiters.forEach(w -> w.assignTaskQueue(waiterTasksQueue, cookTasksQueue));
        this.allCooks.forEach(w -> w.assignTaskQueue(cookerManager, waiterTasksQueue, cookTasksQueue));
    }

    private List<Cook> prepareCooks(SimulationOptions options) {
        return IntStream.range(0, options.getCooksNumber()).mapToObj(v -> new Cook()).collect(Collectors.toList());
    }

    private List<Waiter> prepareWaiters(SimulationOptions options) {
        return IntStream.range(0, options.getWaitersNumber()).mapToObj(v -> new Waiter()).collect(Collectors.toList());
    }

    private RestaurantTask assignTable(Client client) throws InterruptedException {
        Integer tableNumber = tryToFindTableOrBlock(client);

        Table table = tablesMap.get(tableNumber);
        table.seat(client);

        return new GetOrder(client, table);
    }

    private Integer tryToFindTableOrBlock(Client client) throws InterruptedException {
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
            Optional<Integer> avaliableTableNumber = tables.stream().filter(tableNumber -> {
                return tablesMap.get(tableNumber).isAvailable();
            }).findFirst();

            if (avaliableTableNumber.isPresent()) {
                return avaliableTableNumber.get();
            }
        }

        return null;
    }

    private void start(SimulationOptions options) {
        start();
    }

    public void start() {
        Utils.changeScale(options.getTimeScale());

        log.info("Start restaurant");
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

        table.getClient().getDishes().stream().map(this.dishesMap::get).filter(Objects::nonNull).map(Dish::getPrice).forEach(price -> {
                this.profit += price;

            }
        );


        table.free();
        notifyRoomManager();
    }


    public RestaurantView read() {

        return RestaurantView.builder()
            .startTime(toDateFormat(startTime))
            .stopTime(toDateFormat(closeTime))
            .profit(profit)
            .cooks(allCooks.stream().map(this::toView).collect(Collectors.toList()))
            .waiters(allWaiters.stream().map(this::toView).collect(Collectors.toList()))
            .tables(tablesMap.values().stream().map(this::toView).collect(Collectors.toList()))
            .cookers(cookerManager.getAllCookers().stream().map(this::toView).collect(Collectors.toList()))
            .nextStepAvailable(true)
            .build();
    }

    public RestaurantView finalView() {
        return RestaurantView.builder()
            .startTime(toDateFormat(startTime))
            .stopTime(toDateFormat(closeTime))
            .profit(profit)
            .cooks(allCooks.stream().map(this::toFinalView).collect(Collectors.toList()))
            .waiters(allWaiters.stream().map(this::toFinalView).collect(Collectors.toList()))
            .tables(tablesMap.values().stream().map(this::toFinalView).collect(Collectors.toList()))
            .cookers(cookerManager.getAllCookers().stream().map(this::toFinalView).collect(Collectors.toList()))
            .nextStepAvailable(false)
            .build();
    }

    private CookerView toFinalView(Cooker cooker) {
        return CookerView.builder()
            .available(true)
            .currentDish(null)
            .build();
    }

    private TableView toFinalView(Table table) {
        return TableView.builder()
            .available(true)
            .number(table.getNumber())
            .seats(table.getSeats())
            .numberOfSittingPeople(0)
            .build();
    }

    private CookView toFinalView(Cook cook) {
        return new CookView(
            translate(CookState.WAIT_FOR_TASK)
        );
    }

    private WaiterView toFinalView(Waiter waiter) {
        return new WaiterView(
            translate(WaiterState.WAITING_FOR_TASK),
            waiter.getCurrentTask()
        );
    }

    private CookView toView(Cook cook) {
        return CookView.builder()
            .state(translate(cook.getState()))
            .build();
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

    private TableView toView(Table value) {
        return TableView.builder()
            .available(value.isAvailable())
            .number(value.getNumber())
            .seats(value.getSeats())
            .numberOfSittingPeople(value.getClient() != null ? value.getClient().getClientNumbers() : 0)
            .build();
    }

    private WaiterView toView(Waiter value) {
        return WaiterView.builder()
            .state(translate(value.getState()))
            .currentTask(value.getCurrentTask())
            .build();
    }

    private String translate(WaiterState waiterState) {
        switch (waiterState) {
            case DOING_TASK:
                return "Wykonuje zadanie";
            case WAITING_FOR_TASK:
                return "Oczekuje na zadanie";
        }
        throw new IllegalStateException(String.format("Unhandled waiter state: %s", waiterState));
    }

    private CookerView toView(Cooker value) {
        return CookerView.builder()
            .available(value.isAvailable())
            .currentDish(value.getCurrentDish())
            .build();
    }

    private LocalDateTime toLocalDateTime(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(time),
            TimeZone.getDefault().toZoneId());
    }

    private String toDateFormat(long time) {
        return format.format(new Date(time));
    }

    public boolean isRunning() {
        return running.get();
    }
}
