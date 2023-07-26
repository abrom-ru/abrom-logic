package watering.models;
/**
 * Набор констант для симуляции полива
 */
public class WateringConstants {
    /**
     * Число миллисекунд, для симуляции одной секунды времени
     */
    public static final long SIMULATION_MILLIS_PER_SECOND = 1000;

    /**
     * Задержка в секундах для запуска насоса
     */
    public static final int PUMP_START_DELAY_SEC = 10;

    /**
     * Задержка в секундах для открытия клапана
     */
    public static final int VALVE_OPEN_DELAY_SEC = 2;

    /**
     * Число клапанов в элементе полива
     */
    public static final int VALVE_NUMBER = 4;

    /**
     * Лимит работы клапана в секундах
     */
    public static final int VALVE_WORKING_LIMIT_SEC = 60;
}
