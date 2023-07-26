package watering.models;

import models.Topic;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Класс, представляющий бак с одним концевиком
 */
public class OneTrailerTank extends Tank {
    /**
     * Максимальное суммарное количества минут полива после достижения уровня концевика
     */
    private static final int WATERING_SEC_ALLOWED = 15 * 60; //увеличил задержку, были разногласия между секундами и минутами

    private final ReadWriteLock wateringLock = new ReentrantReadWriteLock();

    /**
     * Концевик бака
     */
    private final Trailer trailer;

    /**
     * Количество минут, которое осталось для полива
     */
    private int wateringSecLeft;


    /* 1)добавил топиков для инициализации устройств в конструктор
       2)убрал непонятные уровни воды, видимо они нужны были для инициализации
     */
    public OneTrailerTank(Topic tapTopic, Topic trailerTopic, Topic stateTopic) {
        super(tapTopic, stateTopic);
        this.trailer = new Trailer(trailerTopic);
        this.wateringSecLeft = 0;
    }

    /**
     * Метод, возвращающий флаг, возможно ли начать полив
     *
     * @return true, если полив может быть включён
     */
    @Override
    protected boolean wateringAllowed() {
        try {
            // возвращаем true, если количество минут для полива больше 0
            wateringLock.readLock().lock();
            //здесь оптимизировал условие
            return wateringSecLeft > 0;
        } finally {
            wateringLock.readLock().unlock();
        }
    }

    /**
     * Метод возвращающий флаг, определяющий полон бак или нет
     *
     * @return true, если баг полон
     */
    @Override
    protected boolean isFull() {
        // возвращаем true, если уровень воды достиг концевика
        boolean result = trailer.isLevelReached();
        if (result) {
            try {
                wateringLock.writeLock().lock();
                // если бак полный, обновляем количество оставшихся минут
                wateringSecLeft = Math.max(WATERING_SEC_ALLOWED, wateringSecLeft);
            } finally {
                wateringLock.writeLock().unlock();
            }
        }
        return result;
    }

    /**
     * Метод, обновляющий состояние бака во время полива
     */
    @Override
    public void makeWatering() {
        super.makeWatering();
        try {
            wateringLock.writeLock().lock();
            // уменьшаем количество оставшихся минут полива
            wateringSecLeft--;
        } finally {
            wateringLock.writeLock().unlock();
        }
    }
}
