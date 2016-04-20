package z3

import com.microsoft.z3._

class Core extends Z3JarSuite {

  test("Core") {

    val x = z3.mkIntConst("x")
    val y = z3.mkIntConst("y")
    val p1 = z3.mkBoolConst("p1")
    val p2 = z3.mkBoolConst("p2")
    val p3 = z3.mkBoolConst("p3")
  
    val zero = z3.mkInt(0)

    val solver = z3.mkSolver
    solver.add(z3.mkImplies(p1, z3.mkNot(z3.mkNot(z3.mkEq(x, zero)))))
    solver.add(z3.mkImplies(p2, z3.mkNot(z3.mkEq(y, zero))))
    solver.add(z3.mkImplies(p3, z3.mkNot(z3.mkEq(x, zero))))

    val (result, model, core) = solver.check(p1, p2, p3) match {
      case Status.SATISFIABLE => (Some(true), solver.getModel, null)
      case Status.UNSATISFIABLE => (Some(false), null, solver.getUnsatCore.toSet)
      case Status.UNKNOWN => (None, null, null)
    }

    result should equal (Some(false))
    core.toSet should equal (Set(p1, p3))
  }
}

