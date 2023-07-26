package watering.models;

import models.SwitchDevice;
import models.Topic;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Абстракный класс, представляющий бак для полива
 */
public abstract class Tank extends Thread {
    private final ReadWriteLock levelLock = new ReentrantReadWriteLock();
    private final ReadWriteLock tapLock = new ReentrantReadWriteLock();


    /**
     * устройство для вывода в интерфейс состояния крана бака
     */
    private final SwitchDevice state;
    /**
     * устройство отвечающее за кран
     */
    private final SwitchDevice tapOpen;


    /*
        добавил в конструктор топики отвечающие за инециализацию крана и устройства,
        отвечающего за вывод в интерфейс
        убрал значение для симуляции
    */
    public Tank(Topic tapTopic, Topic stateTopic) {
        tapOpen = new SwitchDevice(tapTopic);
        state = new SwitchDevice(stateTopic);
    }

    /**
     * Перегузка метода run - бак является потоком, статус которого непрерывно обновляется
     */
    @Override
    public void run() {
        try {
            while (true) {
                try {
                    tapLock.writeLock().lock();
                    //SwitchDevice обладает методом getValue() возращающим тип boolean
                    if (tapOpen.getValue()) {
                        if (isFull()) {
                            Logger.log("Бак заполнен. Кран закрывается");
                            closeTap();
                            state.setValue(false);
                        }
                    }
                } finally {
                    tapLock.writeLock().unlock();
                }
                Thread.sleep(WateringConstants.SIMULATION_MILLIS_PER_SECOND);
            }
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Метод, возвращающий флаг, возможно ли начать полив
     *
     * @return true, если полив может быть включён
     */
    protected abstract boolean wateringAllowed();

    /**
     * Метод возвращающий флаг, определяющий полон бак или нет
     *
     * @return true, если баг полон
     */
    protected abstract boolean isFull();


    // добавил физическое устройство для крана

    /**
     * Метод для открытия крана для наполнения бака
     */
    public void openTap() {
        try {
            tapLock.writeLock().lock();
            if (tapOpen.getValue()) {
                Logger.log("Кран уже открыт");
                return;
            }
            if (isFull()) {
                Logger.log("Бак заполнен.");
                state.setValue(false);
                return;
            }
            tapOpen.setValue(true);
            Logger.log("Кран открывается....");
        } finally {
            tapLock.writeLock().unlock();
        }
    }

    /**
     * Метод, обновляющий состояние бака во время полива
     */
    public void makeWatering() {
        try {
            levelLock.writeLock().lock();
            Logger.log("Идёт полив...");
        } finally {
            levelLock.writeLock().unlock();
        }
    }


    // добавил метод для закрытия крана
    public void closeTap() {
        try {
            tapLock.writeLock().lock();
            if (!tapOpen.getValue()) {
                Logger.log("Кран уже закрыт");
                return;
            }
            tapOpen.setValue(false);
            state.setValue(false);
            Logger.log("Кран закрывается....");
        } finally {
            tapLock.writeLock().unlock();
        }
    }
}
