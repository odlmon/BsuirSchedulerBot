package core;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final String PORT = System.getenv("PORT");

    public static void main(String[] args) {
        ApiContextInitializer.init();
        var botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new BsuirSchedulerBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        try (var serverSocket = new ServerSocket(Integer.parseInt(PORT))) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
