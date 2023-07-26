package watering.models;

import models.Topic;

/**
 * Класс, представляющий бак с двумя концевиками
 */
public class TwoTrailerTank  extends Tank {
    /**
     * Нижний концевик
     */
    private final Trailer bottomTrailer;

    /**
     * Верхний концевик
     */
    private final Trailer topTrailer;


    // добавил топиков для инициализации концевиков и крана
    public TwoTrailerTank(Topic tapTopic, Topic bottomTrailerTopic, Topic topTrailerTopic, Topic stateTopic) {
        super(tapTopic, stateTopic);
        this.bottomTrailer = new Trailer(bottomTrailerTopic);
        this.topTrailer = new Trailer(topTrailerTopic);
    }

    /**
     * Метод, возвращающий флаг, возможно ли начать полив
     * @return true, если полив может быть включён
     */
    @Override
    protected boolean wateringAllowed() {
        return bottomTrailer.isLevelReached();
    }

    /**
     * Метод возвращающий флаг, определяющий полон бак или нет
     * @return true, если баг полон
     */
    @Override
    protected boolean isFull() {
        return topTrailer.isLevelReached();
    }
}
