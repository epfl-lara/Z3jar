package z3

import com.microsoft.z3._

class IntArith extends Z3JarSuite {

  test("Comfusy-like") {
    val h = z3.mkIntConst(z3.mkSymbol("h"))
    val m = z3.mkIntConst(z3.mkSymbol("m"))
    val s = z3.mkIntConst(z3.mkSymbol("s"))
    // builds a constant integer value from the CL arg.
    val t = z3.mkInt(1234)
    // more integer constants
    val z = z3.mkInt(0)
    val sx = z3.mkInt(60)
    // builds the constraint h*3600 + m * 60 + s == totSecs
    val cs1 = z3.mkEq(
    z3.mkAdd(
      z3.mkMul(z3.mkInt(3600), h),
      z3.mkMul(sx,             m),
      s),
    t)
    // more constraints
    val cs2 = z3.mkAnd(z3.mkGe(h, z), z3.mkLt(h, z3.mkInt(24)))
    val cs3 = z3.mkAnd(z3.mkGe(m, z), z3.mkLt(m, sx))
    val cs4 = z3.mkAnd(z3.mkGe(s, z), z3.mkLt(s, sx))

    val solver = z3.mkSolver
    solver.add(z3.mkAnd(cs1, cs2, cs3, cs4))

    // attempting to solve the constraints
    val (sol, model) = solver.check match {
      case Status.SATISFIABLE => (Some(true), solver.getModel)
      case Status.UNSATISFIABLE => (Some(false), null)
      case Status.UNKNOWN => (None, null)
    }

    sol should equal(Some(true))
    Option(model.eval(h, false)).map(_.asInstanceOf[IntNum].getInt) should equal(Some(0))
    Option(model.eval(m, false)).map(_.asInstanceOf[IntNum].getInt) should equal(Some(20))
    Option(model.eval(s, false)).map(_.asInstanceOf[IntNum].getInt) should equal(Some(34))
  }
}

