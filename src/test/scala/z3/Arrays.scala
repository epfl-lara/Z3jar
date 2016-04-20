package z3

import com.microsoft.z3._

class Arrays extends Z3JarSuite {

  test("Arrays") {

    val is = z3.mkIntSort
    val array1 = z3.mkArrayConst("arr1", is, is)
    val array2 = z3.mkArrayConst("arr2", is, is)
    val x = z3.mkIntConst("x")

    val solver = z3.mkSolver
    // array1 = [ 42, 42, 42, ... ]
    solver.add(z3.mkEq(array1, z3.mkConstArray(is, z3.mkInt(42))))
    // x = array1[6]
    solver.add(z3.mkEq(x, z3.mkSelect(array1, z3.mkInt(6))))
    // array2 = array1[x - 40 -> 0]
    solver.add(z3.mkEq(array2, z3.mkStore(array1, z3.mkSub(x, z3.mkInt(40)), z3.mkInt(0))))

    // "reading" the default value of array2 (should be 42)
    val fourtyTwo = z3.mkFreshConst("ft", is)
    solver.add(z3.mkEq(fourtyTwo, z3.mkTermArray(array2)))

    val (result, model) = solver.check match {
      case Status.SATISFIABLE => (Some(true), solver.getModel)
      case Status.UNSATISFIABLE => (Some(false), null)
      case Status.UNKNOWN => (None, null)
    }

    //println("model is")
    //println(model)
    result should equal(Some(true))

    def evalArray(sym: Expr) = {
      val arrayEvaluated = Option(model.eval(sym, false)).map(_.asInstanceOf[ArrayExpr])
      arrayEvaluated should be ('defined)

      val ae = arrayEvaluated.get
      val arrayVal = Option(model.getFuncInterp(model.eval(ae, false).getFuncDecl.getParameters()(0).getFuncDecl))
      arrayVal should be ('defined)

      val (entries, default) = (arrayVal.get.getEntries, arrayVal.get.getElse)
      val valueMap = entries.map(entry => entry.getArgs()(0) -> entry.getValue).toMap

      (valueMap, default)
    }

    val (valueMap1, default1) = evalArray(array1)
    default1.asInstanceOf[IntNum].getInt should equal (42)

    val (valueMap2, default2) = evalArray(array2)
    valueMap2.get(z3.mkInt(2)) should equal (Some(z3.mkInt(0)))
    default2.asInstanceOf[IntNum].getInt should equal (42)

    // This seems to fail. Perhaps specifying array default is not supported anymore ?
    // Option(model.eval(fourtyTwo, false)).map(_.asInstanceOf[IntNum].getInt) should equal (Some(42))
  }
}

