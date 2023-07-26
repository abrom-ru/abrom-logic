package models.expressionSolver

import models.SensorDevice
import models.Topic
import models.toTopic
import java.util.Stack
import kotlin.math.abs

class Expression(val list: Collection<ExpressionLexeme>) {
    fun solve(): Value {
        val stack: Stack<Value> = Stack()
        list.forEach {
            if (it is ExNumber) {
                stack.add(NumberValue(it.number))
            }
            if (it is Operator) {
                it.apply(stack)
            }
        }
        if (stack.size != 1) {
            throw ExpressionException("error operator count(stack length is ${stack.size})")
        }
        return (stack.pop())
    }

    fun getTopics(): List<Topic> {
        return list.filterIsInstance<TopicDereferenceOp>().map { it.topic.toTopic() }
    }
}

abstract class Lexeme

abstract class ExpressionLexeme : Lexeme()

interface ValueOperator

class ExNumber(var number: Number) : ExpressionLexeme() {
    override fun toString(): String {
        return number.toString()
    }
}

abstract class Operator : ExpressionLexeme() {
    abstract fun getPriority(): Int
    abstract fun apply(stack: Stack<Value>)
}

class AdditionOp : Operator() {
    override fun getPriority(): Int = OperatorProperty.ADDITION.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        stack.add(stack.pop() as NumberValue + stack.pop() as NumberValue)
    }

    override fun toString(): String {
        return "+"
    }
}

class SubtractionOp : Operator() {
    override fun getPriority(): Int = OperatorProperty.SUBTRACTION.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val second = stack.pop() as NumberValue
        val first = stack.pop() as NumberValue
        stack.add(first - second)
    }

    override fun toString(): String {
        return "-"
    }
}

class MultiplyOp : Operator() {
    override fun getPriority(): Int = OperatorProperty.MULTIPLY.priority
    override fun toString(): String {
        return "*"
    }

    override fun apply(stack: Stack<Value>) {
        val second = stack.pop() as NumberValue
        val first = stack.pop() as NumberValue
        stack.add(first * second)
    }
}

class Changed : Operator(), ValueOperator {

    private var lastState: Value? = null
    override fun getPriority(): Int = OperatorProperty.CHANGED.priority

    override fun apply(stack: Stack<Value>) {
        if (stack.size < 1) throw ExpressionException("error operators count")
        val value = stack.pop()
        stack.add(
            if (lastState == null) {
                LogicValue(false)
            } else {
                LogicValue(lastState != value)
            },
        )
        lastState = value
    }
}

class DivineOp : Operator() {
    override fun getPriority(): Int = OperatorProperty.DIVINE.priority
    override fun toString(): String {
        return "/"
    }

    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val second = stack.pop() as NumberValue
        val first = stack.pop() as NumberValue
        if (second == NumberValue(0)) {
            if (stack.size < 2) throw ExpressionException("division to zero")
        }
        stack.add(first / second)
    }
}

class UnaryMinusOp : Operator() {
    override fun getPriority(): Int = OperatorProperty.UNARY_MINUS.priority
    override fun toString(): String {
        return "-"
    }

    override fun apply(stack: Stack<Value>) {
        if (stack.size < 1) throw ExpressionException("error operators count")
        stack.add(-(stack.pop() as NumberValue))
    }
}

class TopicDereferenceOp(val topic: String) : Operator(), ValueOperator {
    val value: Double by SensorDevice(topic)
    override fun getPriority(): Int = OperatorProperty.TOPIC_DEREFERENCE.priority
    override fun apply(stack: Stack<Value>) {
        val eps = 1e-6
        val remainder = value - value.toInt()
        stack.add(NumberValue(if (abs(remainder) < eps) value.toInt() else value))
    }
}

class Equals : Operator() {
    override fun getPriority(): Int = OperatorProperty.EQUALS.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        stack.add(LogicValue((stack.pop() as NumberValue) == (stack.pop() as NumberValue)))
    }
}

class NotEquals : Operator() {
    override fun getPriority(): Int = OperatorProperty.NOT_EQUALS.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        stack.add(LogicValue((stack.pop() as NumberValue) != (stack.pop() as NumberValue)))
    }
}

class More : Operator() {
    override fun getPriority(): Int = OperatorProperty.MORE.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val second = (stack.pop() as NumberValue)
        val first = (stack.pop() as NumberValue)
        stack.add(LogicValue(first > second))
    }
}

class Less : Operator() {
    override fun getPriority(): Int = OperatorProperty.LESS.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val second = (stack.pop() as NumberValue)
        val first = (stack.pop() as NumberValue)
        stack.add(LogicValue(first < second))
    }
}

class MoreEquals : Operator() {
    override fun getPriority(): Int = OperatorProperty.MORE_EQUALS.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val second = (stack.pop() as NumberValue)
        val first = (stack.pop() as NumberValue)
        stack.add(LogicValue(first >= second))
    }
}

class LessEquals : Operator() {
    override fun getPriority(): Int = OperatorProperty.LESS_EQUALS.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val second = (stack.pop() as NumberValue)
        val first = (stack.pop() as NumberValue)
        stack.add(LogicValue(first <= second))
    }
}

class And : Operator() {
    override fun getPriority(): Int = OperatorProperty.AND.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val first = (stack.pop() as LogicValue).value
        val second = (stack.pop() as LogicValue).value
        stack.add(LogicValue(first && second))
    }
}

class Or : Operator() {
    override fun getPriority(): Int = OperatorProperty.OR.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 2) throw ExpressionException("error operators count")
        val first = (stack.pop() as LogicValue).value
        val second = (stack.pop() as LogicValue).value
        stack.add(LogicValue(first || second))
    }
}

class Not : Operator() {
    override fun getPriority(): Int = OperatorProperty.NOT.priority
    override fun apply(stack: Stack<Value>) {
        if (stack.size < 1) throw ExpressionException("error operators count")
        stack.add(LogicValue(!(stack.pop() as LogicValue).value))
    }
}

enum class OperatorProperty(val priority: Int, val lexeme: String) {
    ADDITION(4, "+"),
    SUBTRACTION(4, "-"),
    MULTIPLY(5, "*"),
    DIVINE(5, "/"),
    UNARY_MINUS(6, "-"),
    CHANGED(6, "changed"),
    TOPIC_DEREFERENCE(7, "{"),
    AND(1, "&&"),
    EQUALS(2, "=="),
    NOT_EQUALS(2, "!="),
    NOT(6, "!"),
    OR(0, "||"),
    MORE(3, ">"),
    LESS(3, "<"),
    MORE_EQUALS(3, ">="),
    LESS_EQUALS(3, "<="),
}
