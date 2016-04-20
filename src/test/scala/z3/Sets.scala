package z3

import com.microsoft.z3._

class Sets extends Z3JarSuite {

  test("Sets") {

    val is = z3.mkIntSort
    val iss = z3.mkSetSort(is)
    val s1 = z3.mkFreshConst("s1", iss)
    val s2 = z3.mkFreshConst("s2", iss)

    val solver = z3.mkSolver
    solver.add(z3.mkDistinct(s1, s2))

    val (result, model) = solver.check match {
      case Status.SATISFIABLE => (Some(true), solver.getModel)
      case Status.UNSATISFIABLE => (Some(false), null)
      case Status.UNKNOWN => (None, null)
    }

    result should equal(Some(true))

    val s1eval = Option(model.eval(s1, false))
    val s2eval = Option(model.eval(s2, false))
    s1eval should be ('defined)
    s2eval should be ('defined)
    (s1eval,s2eval) match {
      case (Some(se1), Some(se2)) =>
        se1 should not equal (se2)
        //println("Set values :" + s1val + ", " + s2val)
      case _ =>
    }
  }
}

