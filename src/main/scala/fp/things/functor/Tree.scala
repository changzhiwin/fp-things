package fp.things.functor

sealed trait Tree[+T]

object Tree {

  case class Leaf[+T](value: T) extends Tree[T]

  case class Branch[+T](value: T, left: Tree[T], right: Tree[T]) extends Tree[T]

  def leaf[T](value: T): Tree[T] = Leaf(value)

  def branch[T](value: T, left: Tree[T], right: Tree[T]) = Branch(value, left, right)

  def travel[T](tree: Tree[T])(f: T => Unit): Unit = {
    tree match {
      case Leaf(v)                    => f(v)
      case Branch(value, left, right) => {
        travel(left)(f)
        f(value)
        travel(right)(f)
      }
    }
  }
}