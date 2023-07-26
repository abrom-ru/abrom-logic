package watering.models;

import models.SwitchDevice;
import models.Topic;

public class Pump {


    private final SwitchDevice pumpControl;

    Pump(Topic pumpTopic) {
        pumpControl = new SwitchDevice(pumpTopic);
    }

    public boolean getState() {
        return pumpControl.getValue();
    }

    public void launch() throws InterruptedException {
        pumpControl.setValue(true);

        Logger.log("Насос запущен.");
        Thread.sleep(WateringConstants.PUMP_START_DELAY_SEC);
    }

    public void stop() {
        pumpControl.setValue(false);
        Logger.log("Насос остановлен.");
    }
}
