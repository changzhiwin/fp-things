package fp.things.free

object FreeMonad {

  trait Monad[M[_]] {
    def pure[A](a: A): M[A]
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
  }

  object Monad {
    def apply[M[_]](implicit monad: Monad[M]): Monad[M] = monad
  }

  trait ~>[F[_], G[_]] {
    def apply[A](fa: F[A]): G[A]
  }

  trait Free[M[_], A] {
    import Free._

    def flatMap[B](f: A => Free[M, B]): Free[M, B] = FlatMap(this, f)

    def map[B](f: A => B): Free[M, B] = flatMap(a => pure(f(a)))

    // 解释器: 完成逻辑脚本 到 可执行的代码
    def foldMap[G[_]](natTrans: M ~> G)(implicit monad: Monad[G]): G[A] = this match {
      case Pure(a)        => monad.pure(a)
      case Suspend(ma)    => natTrans(ma)
      case FlatMap(fa, f) => monad.flatMap(fa.foldMap(natTrans))(a => f(a).foldMap(natTrans))
    }
  }

  object Free {
    def pure[M[_], A](a: A): Free[M, A] = Pure(a)
    def liftM[M[_], A](ma: M[A]): Free[M, A] = Suspend(ma)

    case class Pure[M[_], A](a: A) extends Free[M, A]
    case class FlatMap[M[_], A, B](fa: Free[M, A], f: A => Free[M, B]) extends Free[M, B]
    case class Suspend[M[_], A](ma: M[A]) extends Free[M, A]
  }
}

