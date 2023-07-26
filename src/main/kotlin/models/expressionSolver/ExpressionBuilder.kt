package models.expressionSolver

import models.expressionSolver.OperatorProperty.ADDITION
import models.expressionSolver.OperatorProperty.AND
import models.expressionSolver.OperatorProperty.CHANGED
import models.expressionSolver.OperatorProperty.DIVINE
import models.expressionSolver.OperatorProperty.EQUALS
import models.expressionSolver.OperatorProperty.LESS
import models.expressionSolver.OperatorProperty.LESS_EQUALS
import models.expressionSolver.OperatorProperty.MORE
import models.expressionSolver.OperatorProperty.MORE_EQUALS
import models.expressionSolver.OperatorProperty.MULTIPLY
import models.expressionSolver.OperatorProperty.NOT
import models.expressionSolver.OperatorProperty.NOT_EQUALS
import models.expressionSolver.OperatorProperty.OR
import models.expressionSolver.OperatorProperty.SUBTRACTION
import models.expressionSolver.OperatorProperty.TOPIC_DEREFERENCE
import models.expressionSolver.OperatorProperty.values
import java.util.Stack

/**
 * Билдер выражений
 *
 * @property infixExpression выражение в формате строки
 */
class ExpressionBuilder(private val infixExpression: String) {

    private fun isOperator(pos: Int): Boolean {
        val lexemes = values().map { it.lexeme }
        var result = false
        lexemes.forEach {
            if (infixExpression.substring(pos).startsWith(it)) {
                result = true
            }
        }
        return result
    }

    /**
     * Создаёт выражение
     *
     * @throws ExpressionException
     */
    fun getExpression(): Expression {
        val result: MutableList<ExpressionLexeme> = mutableListOf()
        val operatorStack: Stack<Lexeme> = Stack()
        var lastLex: Lexeme? = null
        var pos = 0
        while (pos < infixExpression.length) {
            val cur = infixExpression[pos]
            when {
                cur.isWhitespace() -> {}

                cur.isDigit() -> {
                    pos += addNextNumber(result, pos) - 1
                    lastLex = result.last()
                }

                isOperator(pos) -> {
                    var isUnary = false
                    if (pos == 0 || (pos > 1 && operatorStack.contains(lastLex)) && lastLex !is ValueOperator) {
                        isUnary = true
                    }
                    pos += addNextOperator(result, operatorStack, pos, isUnary) - 1
                    lastLex = operatorStack.peek()
                }

                cur == '(' -> {
                    operatorStack.add(OpenBracket())
                    lastLex = operatorStack.peek()
                }

                cur == ')' -> {
                    while (operatorStack.peek() !is OpenBracket) {
                        result.add(operatorStack.pop() as ExpressionLexeme)
                    }
                    operatorStack.pop()
                    lastLex = null
                }

                else -> throw ExpressionException("error lexeme: $cur")
            }
            pos++
        }
        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek() !is ExpressionLexeme) {
                throw ExpressionException("error count of brackets")
            }
            result.add(operatorStack.pop() as ExpressionLexeme)
        }
        return Expression(result)
    }

    private fun addNextOperator(
        postExpr: MutableList<ExpressionLexeme>,
        operatorStack: Stack<Lexeme>,
        pos: Int,
        canBeUnary: Boolean,
    ): Int {
        val postfix = infixExpression.substring(pos)
        var result = 1
        val operator: Operator = when {
            postfix.startsWith(EQUALS.lexeme) -> {
                result++
                Equals()
            }

            postfix.startsWith(NOT_EQUALS.lexeme) -> {
                result++
                NotEquals()
            }

            postfix.startsWith(NOT.lexeme) -> Not()
            postfix.startsWith(MORE_EQUALS.lexeme) -> {
                result++
                MoreEquals()
            }

            postfix.startsWith(MORE.lexeme) -> More()
            postfix.startsWith(LESS_EQUALS.lexeme) -> {
                result++
                LessEquals()
            }

            postfix.startsWith(CHANGED.lexeme) -> {
                result = CHANGED.lexeme.length
                Changed()
            }

            postfix.startsWith(LESS.lexeme) -> Less()
            postfix.startsWith(AND.lexeme) -> {
                result++
                And()
            }

            postfix.startsWith(OR.lexeme) -> {
                result++
                Or()
            }

            postfix.startsWith(ADDITION.lexeme) -> AdditionOp()
            postfix.startsWith(SUBTRACTION.lexeme) -> {
                if (canBeUnary) {
                    UnaryMinusOp()
                } else {
                    SubtractionOp()
                }
            }

            postfix.startsWith(MULTIPLY.lexeme) -> MultiplyOp()
            postfix.startsWith(DIVINE.lexeme) -> DivineOp()
            postfix.startsWith(TOPIC_DEREFERENCE.lexeme) -> {
                result = postfix.substringBefore("}").length + 1
                TopicDereferenceOp(postfix.substringAfter("{").substringBefore("}"))
            }

            else -> throw ExpressionException("error operator ${postfix[0]}")
        }
        while (!operatorStack.isEmpty() && operatorStack.peek() is Operator &&
            (operatorStack.peek() as Operator).getPriority() >= operator.getPriority()
        ) {
            postExpr.add(operatorStack.pop() as ExpressionLexeme)
        }
        operatorStack.add(operator)
        return result
    }

    private fun addNextNumber(list: MutableList<ExpressionLexeme>, position: Int): Int {
        var strNumber = ""
        for (i in position until infixExpression.length) {
            if (infixExpression[i].isDigit() || infixExpression[i] == '.') {
                strNumber += infixExpression[i]
            } else {
                break
            }
        }
        val number: Number = when {
            strNumber.toIntOrNull() != null -> strNumber.toInt()
            strNumber.toDoubleOrNull() != null -> strNumber.toDouble()
            else -> throw ExpressionException("not a number")
        }
        list.add(ExNumber(number))
        return strNumber.length
    }
}

class OpenBracket : Lexeme()
