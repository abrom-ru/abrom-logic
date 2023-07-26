package polishNotation

import models.expressionSolver.Expression
import models.expressionSolver.ExpressionBuilder
import models.expressionSolver.ExpressionException
import models.expressionSolver.Value
import org.junit.jupiter.api.Test

internal class ExpressionBuilderTest {
    val s = "2 / 0"
    val builder = ExpressionBuilder(s)

    @Test
    fun getExpression() {
        val expression: Expression? = try {
            builder.getExpression()
        } catch (ex: ExpressionException) {
            println("eror is Expression")
            null
        }

        val solve: Value? = try {
            expression?.solve()
        } catch (ex: ExpressionException) {
            println("expression solve error is ${ex.message}")
            null
        }
        println(solve)
    }
}
