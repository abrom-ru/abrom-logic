package models.js

class VirtualDevice(val deviceName: String, private val title: String, val cells: MutableCollection<Cell>) {
    constructor(topic: String, title: String, vararg cells: Cell) : this(topic, title, cells.toMutableList())

    init {
        cells.forEachIndexed { index, cell ->
            cell.order = index
        }
    }

    override fun toString(): String {
        return "defineVirtualDevice('$deviceName', {\ntitle: '$title',\ncells: {\n${cells.joinToString(separator = ",\n") { it.toString() }}\n}\n});\n".formatToJs()
    }

    fun addCell(aCell: Cell) {
        cells.add(aCell.apply { this.order = cells.size })
    }

    private fun String.formatToJs(): String {
        var spaceCount = 0
        val parts = this.split("\n")
        val builder: StringBuilder = StringBuilder()
        for (it in parts) {
            spaceCount -= it.count { char -> char == '}' }
            builder.append(spaceBuilder(spaceCount))
            builder.append(it + "\n")
            spaceCount += it.count { char -> char == '{' }
        }
        return builder.toString()
    }

    private fun spaceBuilder(count: Int): String =
        StringBuilder().apply { repeat(count) { this.append("    ") } }.toString()
}
