package z3

import com.microsoft.z3._

class ADTs extends Z3JarSuite {
  test("ADTs") {

    val intSort = z3.mkIntSort

    // this builds two (recursive) datatypes:
    //   Tree ::= Tree * Int * Tree | Leaf
    // and
    //   TreeList ::= Tree TreeList | Nil
    // ...and constructor/tester functions, as well as selectors.
    
    val (nodeSym, isNodeSym) = (z3.mkSymbol("Node"), z3.mkSymbol("isNode"))
    val (leftSym, valueSym, rightSym) = (z3.mkSymbol("left"), z3.mkSymbol("value"), z3.mkSymbol("right"))
    val nodeCons = z3.mkConstructor(nodeSym, isNodeSym,
      List[Symbol](leftSym, valueSym, rightSym).toArray,
      List[Sort]  (null   , intSort , null    ).toArray,
      List[Int]   (0      , 0       , 0       ).toArray)

    val (leafSym, isLeafSym) = (z3.mkSymbol("Leaf"), z3.mkSymbol("isLeaf"))
    val leafCons = z3.mkConstructor(leafSym, isLeafSym,
      new Array[Symbol](0), new Array[Sort](0), new Array[Int](0))

    val (consSym, isConsSym) = (z3.mkSymbol("Cons"), z3.mkSymbol("isCons"))
    val (headSym, tailSym) = (z3.mkSymbol("head"), z3.mkSymbol("tail"))
    val consCons = z3.mkConstructor(consSym, isConsSym,
      List[Symbol](headSym, tailSym).toArray,
      List[Sort]  (null   , null   ).toArray,
      List[Int]   (0      , 1      ).toArray)

    val (nilSym, isNilSym) = (z3.mkSymbol("Nil"), z3.mkSymbol("isNil"))
    val nilCons = z3.mkConstructor(nilSym, isNilSym,
      new Array[Symbol](0), new Array[Sort](0), new Array[Int](0))

    val (treeSym, listSym) = (z3.mkSymbol("Tree"), z3.mkSymbol("List"))
    val constructorMatrix = List(
      List(nodeCons, leafCons).toArray,
      List(consCons, nilCons ).toArray
    ).toArray

    val adts = z3.mkDatatypeSorts(List[Symbol](treeSym, listSym).toArray, constructorMatrix)

    // we then recover all the relevant Function Declarations...
    assert(adts.size == 2)
    val List(treeSort, listSort) = adts.toList

    val nodeConsFun = nodeCons.ConstructorDecl()
    val nodeTestFun = nodeCons.getTesterDecl
    val List(leftSelect, valueSelect, rightSelect) = nodeCons.getAccessorDecls.toList

    val leafConsFun = leafCons.ConstructorDecl()
    val leafTestFun = leafCons.getTesterDecl

    val consConsFun = consCons.ConstructorDecl()
    val consTestFun = consCons.getTesterDecl
    val List(headSelect, tailSelect) = consCons.getAccessorDecls.toList

    val nilConsFun = nilCons.ConstructorDecl()
    val nilTestFun = nilCons.getTesterDecl

    // ...and finally we can use them to build a problem.
    val x  = z3.mkIntConst("x")
    val t1 = z3.mkConst("t1", treeSort)
    val t2 = z3.mkConst("t2", treeSort)
    val t3 = z3.mkConst("t3", treeSort)

    // t1 == t2
    //ctx.assertCnstr(ctx.mkEq(t1,t2))
    //// t1 != t3
    //ctx.assertCnstr(ctx.mkDistinct(t1,t3))
    //// x > 4
    //ctx.assertCnstr(ctx.mkGT(x, ctx.mkInt(4, intSort)))

    val solver = z3.mkSolver
    solver.add(z3.mkEq(t1, leafConsFun()))
    solver.add(z3.mkEq(valueSelect(t1), z3.mkInt(4)))

    // t1 != Leaf
    //ctx.assertCnstr(ctx.mkNot(isLeaf(t1)))

    // isNode(t2) => (t2 = Node(Leaf, x, t3))
    // ctx.assertCnstr(ctx.mkImplies(isNode(t2), ctx.mkEq(t2, nodeCons(leafCons(), x, t3))))
    // replace by this and it becomes unsat..
    // ctx.assertCnstr(ctx.mkEq(t1,t3))

    //println(ctx.mkImplies(isNode(t2), nodeValueSelector(t2) === ctx.mkInt(12, intSort)))

    val (sol, model) = solver.check match {
      case Status.SATISFIABLE => (Some(true), solver.getModel)
      case Status.UNSATISFIABLE => (Some(false), null)
      case Status.UNKNOWN => (None, null)
    }

    sol should equal(Some(true))
    Option(model.eval(t1, false)) should equal(Some(leafConsFun()))
  }
}

