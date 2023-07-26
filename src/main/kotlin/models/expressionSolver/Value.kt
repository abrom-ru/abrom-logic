package models.expressionSolver

import kotlin.math.abs
import kotlin.math.sign

abstract class Value

class NumberValue(var value: Number) : Value(), Comparable<NumberValue> {
    override fun compareTo(other: NumberValue): Int {
        return if (value is Double || other.value is Double) {
            if (abs(value.toDouble() - other.value.toDouble()) <= EPS) {
                0
            } else {
                (value.toDouble() - other.value.toDouble()).sign.toInt()
            }
        } else {
            value.toInt().compareTo(other.value.toInt())
        }
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is NumberValue) {
            if (other.value is Double || value is Double) {
                abs(other.value.toDouble() - value.toDouble()) <= EPS
            } else {
                value == other.value
            }
        } else {
            false
        }
    }

    operator fun plus(other: NumberValue): NumberValue {
        val result = if (value is Double || other.value is Double) {
            value.toDouble() + other.value.toDouble()
        } else {
            value.toInt() + other.value.toInt()
        }
        return NumberValue(result)
    }

    operator fun minus(other: NumberValue): NumberValue {
        val result = if (value is Double || other.value is Double) {
            value.toDouble() - other.value.toDouble()
        } else {
            value.toInt() - other.value.toInt()
        }
        return NumberValue(result)
    }

    operator fun times(other: NumberValue): NumberValue {
        val result = if (value is Double || other.value is Double) {
            value.toDouble() * other.value.toDouble()
        } else {
            value.toInt() * other.value.toInt()
        }
        return NumberValue(result)
    }

    operator fun div(other: NumberValue): NumberValue {
        val result = if (value is Double || other.value is Double) {
            value.toDouble() / other.value.toDouble()
        } else {
            value.toInt() / other.value.toInt()
        }
        return NumberValue(result)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    operator fun unaryMinus(): NumberValue {
        val result: Number = if (value is Double) {
            -value.toDouble()
        } else {
            -value.toInt()
        }
        return NumberValue(result)
    }

    companion object {
        private const val EPS = 1e-6
    }
}

class LogicValue(val value: Boolean) : Value() {
    override fun equals(other: Any?): Boolean {
        return if (other is LogicValue) {
            value == other.value
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value.toString()
    }
}
