package z3

import com.microsoft.z3._

class Calendar extends Z3JarSuite {

  test("Calendar") {
    val totalDays = 10593
    val originYear = 1980

    val year = z3.mkIntConst("year")
    val day = z3.mkIntConst("day")

    def leapDaysUntil(y: IntExpr) = z3.mkAdd(
      z3.mkSub(z3.mkDiv(z3.mkSub(y, z3.mkInt(1)), z3.mkInt(4)),
        z3.mkDiv(z3.mkSub(y, z3.mkInt(1)), z3.mkInt(100))),
      z3.mkDiv(z3.mkSub(y, z3.mkInt(1)), z3.mkInt(400)))

    val solver = z3.mkSolver
    solver.add(z3.mkAnd(
      z3.mkEq(
        z3.mkInt(totalDays),
        z3.mkAdd(
          z3.mkSub(
            z3.mkAdd(
              z3.mkMul(z3.mkSub(year, z3.mkInt(originYear)), z3.mkInt(365)),
              leapDaysUntil(year)),
            leapDaysUntil(z3.mkInt(originYear))),
          day)),
      z3.mkGt(day, z3.mkInt(0)),
      z3.mkLe(day, z3.mkInt(366)))
    )

    val (sol, model) = solver.check match {
      case Status.SATISFIABLE => (Some(true), solver.getModel)
      case _ => (None, null)
    }

    sol should equal(Some(true))
    Option(model.eval(year, false)) should equal(Some(z3.mkInt("2008")))
    Option(model.eval(day, false)) should equal(Some(z3.mkInt("366")))
  }
}

