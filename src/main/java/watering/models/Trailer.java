package watering.models;

import models.SwitchDevice;
import models.Topic;

/**
 * Класс, представляющий концевик бака
 */
public class Trailer {


    /**
     * концевик
     */
    private final SwitchDevice trailer;

    /**
     *
     * @param trailer_topic  топик концевика
     */
    public Trailer(Topic trailer_topic) {
        this.trailer = new SwitchDevice(trailer_topic, true);
    }

    /**
     * Метод для определения, достигла вода в баке концевика или нет
     *
     * @return true, если вода в баке достигла концевика
     */
    public boolean isLevelReached() {
        return trailer.getValue();
    } //поменял симуляцию на реальное устройство
}
