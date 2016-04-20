package z3

import com.microsoft.z3._

class NQueens extends Z3JarSuite {

  test("NQueens") {

    val numCols = 8

    val zero = z3.mkInt(0)

    /* Declaring column variables */
    val columns = (0 until numCols).map{ i => z3.mkIntConst("col" + i) }

    /* All queens are on different columns */
    val diffCnstr = z3.mkDistinct(columns: _*)

    /* Columns are within the bounds */
    val boundsCnstr = for (c <- columns) yield z3.mkAnd(z3.mkGe(c, zero), z3.mkLt(c, z3.mkInt(numCols)))

    /* No two queens are on same diagonal */
    val diagonalsCnstr =
      for (i <- 0 until numCols; j <- 0 until i) yield z3.mkAnd(
        z3.mkDistinct(z3.mkSub(columns(i), columns(j)), z3.mkSub(z3.mkInt(i), z3.mkInt(j))),
        z3.mkDistinct(z3.mkSub(columns(i), columns(j)), z3.mkSub(z3.mkInt(j), z3.mkInt(i)))
      )

    /* We assert all of the above */
    val solver = z3.mkSolver
    solver.add(diffCnstr)
    boundsCnstr foreach (solver.add(_))
    diagonalsCnstr foreach (solver.add(_))

    var sat = false
    var counter = 0
    do {
      solver.check match {
        case Status.SATISFIABLE =>
          sat = true
          counter += 1
          val m = solver.getModel
          val cnstr = z3.mkNot(z3.mkAnd(m.getConstDecls.toList.map(fd => z3.mkEq(fd(), m.getConstInterp(fd))) : _*))
          solver.add(cnstr)

        case Status.UNSATISFIABLE =>
          sat = false

        case Status.UNKNOWN =>
          sat = false
      }
    } while (sat)

    //println("Total number of models: " + nbModels)
    counter should equal (92)
  }
}
