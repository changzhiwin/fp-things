package fp.things.functor

trait Functor[C[_]] {
  def map[A, B](container: C[A])(f: A => B): C[B]
}

object Functor {

  implicit val listFunctor = new Functor[List] {
    def map[A, B](container: List[A])(f: A => B): List[B] = {
      container.map(f)
    }
  }

  implicit val treeFunctor = new Functor[Tree] {
    def map[A, B](container: Tree[A])(f: A => B): Tree[B] = {
      container match {
        case Tree.Leaf(v) => Tree.Leaf(f(v))
        case Tree.Branch(v, l, r) => Tree.Branch(f(v), map(l)(f), map(r)(f))
      }
    }
  }
}