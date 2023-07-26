package watering.models;

import models.SwitchDevice;
import models.Topic;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Класс, представляющий клапан элемента полива
 */
public class Valve implements Runnable {
    private final ReadWriteLock openLock = new ReentrantReadWriteLock();

    /**
     * Открыт ли клапан
     */

    private final SwitchDevice open; // поменял на физическое устройство

    /**
     * Номер клапана
     */
    private final int index;

    private Thread openThread;

    /**
     * Конструктор, создающий закрытый клапан
     */
    public Valve(int index, Topic topic) {
        this.index = index;
        this.open = new SwitchDevice(topic);
        this.open.setValue(false);
        openThread = null;
    }
    /* исправил ошибку связанную с тем, что если мы останавливаем поток через
       thread.interrupt(), то он не останавливает поток, когда он не находился во сне(thread.sleep()) и нужно самим в цикле это проверять
     */

    /**
     * Перегрузка метода run - выполняющая процедуру открытия клапана
     */
    @Override
    public void run() {
        try {
            openLock.writeLock().lock();
            Logger.log("Клапан #" + index + " открывается...");
            this.open.setValue(true);
            Thread.sleep(WateringConstants.VALVE_OPEN_DELAY_SEC * WateringConstants.SIMULATION_MILLIS_PER_SECOND);
            Logger.log("Клапан #" + index + " открыт");
            if (openThread.isInterrupted()) {
                this.open.setValue(false);
                Logger.log("открытие клапана прервалось");
            }
        } catch (InterruptedException ignored) {
            this.open.setValue(false);
            Logger.log("открытие клапана прервалось");
        } finally {
            openLock.writeLock().unlock();
        }
    }

    /**
     * Метод для открытия клапана. Вызывает метод run в отдельном потоке
     */
    public void open() {
        if (isOpen()) {
            Logger.log("Клапан #" + index + " уже открыт");
            return;
        }
        if (openThread != null && (openThread.getState() == Thread.State.RUNNABLE || openThread.getState() == Thread.State.BLOCKED)) {
            Logger.log("Клапан #" + index + " уже открывается");
            return;
        }
        openThread = new Thread(this);
        openThread.start();
    }

    public void waitForOpening() {
        try {
            if (openThread == null || openThread.getState() == Thread.State.NEW) {
                throw new IllegalStateException();
            }
            openThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    public void stop() {
        if (openThread == null || openThread.getState() == Thread.State.NEW || openThread.getState() == Thread.State.TERMINATED) {
            close();
            return;
        }
        openThread.interrupt();
    }

    /**
     * Метод для закрытия клапана. Закрытие происходит мгновенно
     */
    public void close() {
        try {
            openLock.writeLock().lock();
            if (openThread != null && openThread.getState() != Thread.State.TERMINATED) {
                openThread.interrupt();
            }
            openThread = null;
            this.open.setValue(false);
            Logger.log("Клапан #" + index + " закрыт");
        } finally {
            openLock.writeLock().unlock();
        }
    }

    /**
     * Метод для определения состояния клапана
     *
     * @return true, если открыт
     */
    public boolean isOpen() {
        try {
            openLock.readLock().lock();
            return open.getValue();
        } finally {
            openLock.readLock().unlock();
        }
    }
}
