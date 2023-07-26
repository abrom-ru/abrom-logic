package watering.models;

import models.SwitchDevice;
import models.Topic;
import org.jetbrains.annotations.NotNull;

public class WateringElement implements Runnable {
    /**
     * Клапаны элемента полива
     */
    private final Valve[] valves;

    /**
     * Бак элемента полива
     */
    private final Tank tank;

    private final Pump pump;
    private final SwitchDevice state;

    /**
     * Номер последнего использованного клапана
     */
    private int lastUsedValveIndex;

    private Thread wateringThread;

    public WateringElement(Tank tank, @NotNull Topic[] topics, Topic aPump, Topic state) {
        this.valves = new Valve[topics.length];
        this.pump = new Pump(aPump);
        for (int i = 0; i < topics.length; i++) {
            this.valves[i] = new Valve(i, topics[i]);
        }
        this.tank = tank;
        this.state = new SwitchDevice(state);
        this.lastUsedValveIndex = 0;
    }


    private void openValve(int id) {
        Logger.log("открывается кран" + (id + 1));
        valves[id].open();
        valves[id].waitForOpening();
    }

    private void closeValve(int id) {
        valves[id].close();
    }

    private void changeToNextValve() {
        int nextValveIndex = (lastUsedValveIndex + 1) % valves.length;
        openValve(nextValveIndex);
        closeValve(lastUsedValveIndex);
        lastUsedValveIndex = nextValveIndex;
    }

    private void closeAllValves() {
        for (Valve i : valves) {
            i.close();
        }
    }


    //TODO("TRY TO FIX IT)
    @Override
    public void run() {
        if (!tank.wateringAllowed()) {
            state.setValue(false);
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            openValve(lastUsedValveIndex);
            pump.launch();
            do {
                if (System.currentTimeMillis() - startTime >= WateringConstants.SIMULATION_MILLIS_PER_SECOND * WateringConstants.VALVE_WORKING_LIMIT_SEC) {
                    changeToNextValve();
                    startTime = System.currentTimeMillis();
                }
                tank.makeWatering();
                Thread.sleep(WateringConstants.SIMULATION_MILLIS_PER_SECOND);
            }
            while (tank.wateringAllowed() && !wateringThread.isInterrupted());

        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            pump.stop();
            closeAllValves();
            state.setValue(false);
        }
    }

    public void start() {

        if (wateringThread != null && (wateringThread.getState() != Thread.State.TERMINATED)) {
            Logger.log("Полив уже идёт");
            return;
        }
        wateringThread = new Thread(this);
        wateringThread.start();
    }

    public void stop() {
        if (wateringThread == null || wateringThread.getState() ==
                Thread.State.TERMINATED) {
            Logger.log("Полив не идёт");
            return;
        }
        wateringThread.interrupt();
    }
}
