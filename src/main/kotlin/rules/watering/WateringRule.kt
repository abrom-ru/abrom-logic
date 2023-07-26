package rules.watering

import models.SwitchDevice
import models.Topic
import models.js.Cell
import models.js.CellType
import models.js.VirtualDevice
import rules.Rule
import storage.TopicBase
import watering.models.OneTrailerTank
import watering.models.TwoTrailerTank
import watering.models.WateringElement

class WateringRule(
    val name: String,
    tap: Topic,
    topTrailer: Topic,
    lowerTrailer: Topic?,
    valves: List<Topic>,
    pump: Topic,
) : Rule {

    companion object {
        private const val TOPIC_PREFIX = "Watering_"
        private const val TITLE_PREFIX = "ПОЛИВ "
        private const val TAP_TOPIC = "tank tap"
        private const val WATERING_TOPIC = "Watering"
    }

    private val pageTopic = "$TOPIC_PREFIX$name"
    private val tankValveSwitch = SwitchDevice(pageTopic, TAP_TOPIC)
    private val wateringSwitch = SwitchDevice(pageTopic, WATERING_TOPIC)

    private var tankValveState = false
    private var wateringState = false

    private val tank = if (lowerTrailer != null) {
        TwoTrailerTank(tap, lowerTrailer, topTrailer, Topic(pageTopic, TAP_TOPIC))
    } else {
        OneTrailerTank(tap, topTrailer, Topic(pageTopic, TAP_TOPIC))
    }

    private val wateringElement =
        WateringElement(tank, valves.toTypedArray(), pump, Topic(pageTopic, WATERING_TOPIC))

    init {
        val topics = listOfNotNull(tap, pump, topTrailer, lowerTrailer) + valves
        TopicBase.addCallback(topics, ::checkRule)
        addPage()
        tank.start()
    }

    private fun addPage() {
        Rule.builder.add(
            VirtualDevice(
                pageTopic,
                "$TITLE_PREFIX$name",
                Cell(TAP_TOPIC, CellType.SWITCH),
                Cell(WATERING_TOPIC, CellType.SWITCH),
            ),
        )
    }

    private fun checkRule() {
        if (wateringSwitch.value && !wateringState) {
            wateringElement.start()
            wateringState = true
        } else if (!wateringSwitch.value && wateringState) {
            wateringElement.stop()
            wateringState = false
        }
        if (tankValveSwitch.value && !tankValveState) {
            tank.openTap()
            tankValveState = true
        } else if (!tankValveSwitch.value && tankValveState) {
            tank.closeTap()
            tankValveState = false
        }
    }

    override fun deleteRule() {
        TopicBase.removeCallback(::checkRule)
        Rule.builder.delete(pageTopic)
    }
}
