package z3

import com.microsoft.z3._

class Quantifiers extends Z3JarSuite {

  /*
   * (declare-sort Type)
   * (declare-fun subtype (Type Type) Bool)
   * (declare-fun array-of (Type) Type)
   * (assert (forall ((x Type)) (subtype x x)))
   * (assert (forall ((x Type) (y Type) (z Type))
   *             (=> (and (subtype x y) (subtype y z)) 
   *                             (subtype x z)))) 
   * (assert (forall ((x Type) (y Type))
   *             (=> (and (subtype x y) (subtype y x)) 
   *                             (= x y))))
   * (assert (forall ((x Type) (y Type) (z Type))
   *             (=> (and (subtype x y) (subtype x z)) 
   *                             (or (subtype y z) (subtype z y))))) 
   * (assert (forall ((x Type) (y Type))
   *             (=> (subtype x y) 
   *                             (subtype (array-of x) (array-of y)))))
   * (declare-const root-type Type)
   * (assert (forall ((x Type)) (subtype x root-type)))
   * (check-sat)
   */

  test("Quantifiers") {
    val solver = z3.mkSolver

    /*
     * (declare-sort Type)
     * (declare-fun subtype (Type Type) Bool)
     * (declare-fun array-of (Type) Type)
     */
    val typeSort = z3.mkUninterpretedSort("Type")
    val subtypeFun = z3.mkFuncDecl("subtype", List[Sort](typeSort, typeSort).toArray, z3.mkBoolSort)
    val arrayOfFun = z3.mkFuncDecl("array-of", List[Sort](typeSort).toArray, typeSort)

    val syms @ List(xSym, ySym, zSym) = List("x", "y", "z").map(z3.mkSymbol(_))
    val consts @ List(x, y, z) = syms.map(sym => z3.mkConst(sym, typeSort))

    def subtype(e1: Expr, e2: Expr) = subtypeFun(e1, e2).asInstanceOf[BoolExpr]
    def arrayOf(e: Expr) = arrayOfFun(e)

    /* (assert (forall ((x Type)) (subtype x x))) */
    solver.add(z3.mkForall(
      List[Sort](typeSort).toArray,
      List[Symbol](xSym).toArray,
      subtype(x, x),
      0, new Array[Pattern](0), new Array[Expr](0), null, null))

    /* (assert (forall ((x Type) (y Type) (z Type))
                   (=> (and (subtype x y) (subtype y z)) 
                                  (subtype x z)))) */
    solver.add(z3.mkForall(
      List[Sort](typeSort, typeSort, typeSort).toArray,
      List[Symbol](xSym, ySym, zSym).toArray,
      z3.mkImplies(z3.mkAnd(subtype(x, y), subtype(y, z)), subtype(x, z)),
      0, new Array[Pattern](0), new Array[Expr](0), null, null))

    /* (assert (forall ((x Type) (y Type))
                   (=> (and (subtype x y) (subtype y x)) 
                                  (= x y)))) */
    solver.add(z3.mkForall(
      List[Sort](typeSort, typeSort).toArray,
      List[Symbol](xSym, ySym).toArray,
      z3.mkImplies(z3.mkAnd(subtype(x, y), subtype(y, x)), z3.mkEq(x, y)),
      0, new Array[Pattern](0), new Array[Expr](0), null, null))

    /* (assert (forall ((x Type) (y Type) (z Type))
                   (=> (and (subtype x y) (subtype x z)) 
                                  (or (subtype y z) (subtype z y))))) */
    solver.add(z3.mkForall(
      List[Sort](typeSort, typeSort, typeSort).toArray,
      List[Symbol](xSym, ySym, zSym).toArray,
      z3.mkImplies(z3.mkAnd(subtype(x, y), subtype(x, z)), z3.mkOr(subtype(y, z), subtype(z, y))),
      0, new Array[Pattern](0), new Array[Expr](0), null, null))

    /* (assert (forall ((x Type) (y Type))
                  (=> (subtype x y) 
                                  (subtype (array-of x) (array-of y))))) */
    solver.add(z3.mkForall(
      List[Sort](typeSort, typeSort).toArray,
      List[Symbol](xSym, ySym).toArray,
      z3.mkImplies(subtype(x, y), subtype(arrayOf(x), arrayOf(y))),
      0, new Array[Pattern](0), new Array[Expr](0), null, null))

    /* (declare-const root-type Type) */
    val rootType = z3.mkConst("root-type", typeSort)

    /* (assert (forall ((x Type)) (subtype x root-type))) */
    solver.add(z3.mkForall(
      List[Sort](typeSort).toArray,
      List[Symbol](xSym).toArray,
      subtype(x, rootType),
      0, new Array[Pattern](0), new Array[Expr](0), null, null))

    solver.check


  }
}
