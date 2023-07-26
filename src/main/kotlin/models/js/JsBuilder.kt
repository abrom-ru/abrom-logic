package models.js

import LaunchParams
import Logger
import storage.TopicBase
import java.io.File
import java.io.IOException
import java.lang.Thread.sleep

class JsBuilder(private val configFile: String, val pages: MutableMap<String, VirtualDevice> = mutableMapOf()) {

    init {
        save()
    }

    fun save() {
        if (!LaunchParams.LOCAL_LAUNCH) {
            TopicBase.timer.updateLast()
            try {
                val file = File(configFile).outputStream()
                try {
                    val writer = file.bufferedWriter()
                    writer.flush()
                    writer.write(pages.values.joinToString("\n") { it.toString() })
                    sleep(1000)
                    writer.close()
                } catch (ex: IOException) {
                    Logger.error {
                        "File writing error"
                    }
                } finally {
                    file.close()
                }
            } catch (ex: IOException) {
                Logger.error {
                    ex.toString()
                }
            }
        }
    }

    fun add(device: VirtualDevice, name: String = device.deviceName) {
        pages[name] = device
        save()
    }

    fun deletePage(name: String) {
        pages.remove(name)
    }

    fun add(devices: List<VirtualDevice>) {
        devices.forEach { pages[it.deviceName] = it }
        save()
    }

    fun addCell(deviceName: String, aCell: Cell) {
        pages[deviceName]?.cells?.add(aCell) ?: Logger.error { "not found page with name: $deviceName" }
    }

    fun set(devices: List<VirtualDevice>) {
        pages.clear()
        devices.forEach { pages[it.deviceName] = it }
        save()
    }

    fun clear() {
        pages.clear()
        save()
    }

    fun delete(name: String) {
        pages.remove(name)
        save()
    }
}
