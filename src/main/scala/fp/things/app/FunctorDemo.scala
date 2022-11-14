package fp.things.app

object FunctorDemo {
  /*

  def main(args: Array[String]): Unit = {
    println("Hi, Functor.")

    val tree = Tree.branch(
      4,
      Tree.branch(2, Tree.leaf(1), Tree.leaf(3)),
      Tree.branch(6, Tree.leaf(5), Tree.leaf(7))
    )

    Tree.travel(tree)

    // 直接调用
    val mt = treeFunctor.map(tree)(v => v * 100)
    Tree.travel(mt)

    // 统一接口调用
    Tree.travel( do10x(tree)(treeFunctor) )
  }

  sealed trait Tree[+T]

  object Tree {

    def leaf[T](value: T): Tree[T] = Leaf(value)

    def branch[T](value: T, left: Tree[T], right: Tree[T]) = Branch(value, left, right)

    def travel[T](tree: Tree[T]): Unit = {
      tree match {
        case Leaf(v) => println(s"Leaf(${v})")
        case Branch(value, left, right) => {
          travel(left)
          println(s"Branch(${value})")
          travel(right)
        }
      }
    }
  }

  case class Leaf[+T](value: T) extends Tree[T]

  case class Branch[+T](value: T, left: Tree[T], right: Tree[T]) extends Tree[T]

  trait Functor[+C[_]] {
    def map[A, B](container: C[A])(f: A => B): C[B]
  }

  def do10x[C[_]](container: C[Int])(implicit functor: Functor[C]) = functor.map(container)(_ * 10)


  implicit val treeFunctor = new Functor[Tree] {
    override def map[A, B](container: Tree[A])(f: A => B): Tree[B] = {
      container match {
        case Leaf(v) => Leaf(f(v))
        case Branch(v, l, r) => Branch(f(v), map(l)(f), map(r)(f))
      }
    }
  }
  */
}