package watering.models;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Вспомогательный класс для ведения логирования
 */
public class Logger {
    /**
     * Метод для вывода сообщения в консоль. С указанием времени сообщения
     * @param message сообщения для вывода
     */
    public static void log(String message) {
        Date date = new Date();
        System.out.println("[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date) + "]  " + message);
    }
}
