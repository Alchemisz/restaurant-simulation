package restaurant.core.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restaurant.core.Client;
import restaurant.core.ClientsGenerator;

import java.util.concurrent.BlockingQueue;

import static restaurant.shared.AppConstants.CLIENT_ARRIVING_TIME_IN_MILLISECONDS;

@Slf4j
@RequiredArgsConstructor
public class ArrivingClientThread extends Thread {

    private final BlockingQueue<Client> waitingClientsQueue;

    @Override
    public void run() {
        while (true) {
            sleep(CLIENT_ARRIVING_TIME_IN_MILLISECONDS);
            Client client = ClientsGenerator.generateNewClient();
            log.info(String.format("New client arrived: %s", client));
            waitingClientsQueue.add(client);
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
