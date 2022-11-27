package fp.things.app

import fp.things.functor.{Functor, Tree}

object FunctorDemo {

  def do10x[C[_]](container: C[Int])(implicit functor: Functor[C]) = functor.map(container)(x => x * 10)

  def main(args: Array[String]): Unit = {

    val tree = Tree.branch(
      4,
      Tree.branch(2, Tree.leaf(1), Tree.leaf(3)),
      Tree.branch(6, Tree.leaf(5), Tree.leaf(7))
    )

    // 实现了Tree/List的Functor[_]
    import Functor._
    val treex10 = do10x[Tree](tree)
    Tree.travel( treex10 )(println(_))

    val list = List(1, 2, 3, 4, 5)
    val listx10 = do10x[List](list)
    listx10.foreach(println(_))
  }
  
}