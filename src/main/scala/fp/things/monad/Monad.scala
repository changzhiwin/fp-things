package fp.things.monad

// 要理解所谓集合论，那当然操作都是在集合之上的，所以这里类型参数是M[_]，M是对某种类型的包裹（容器）
trait Monad[M[_]] {
  def pure[A](a: A): M[A]
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
  def map[A, B](ma: M[A])(f: A => B): M[B] = flatMap(ma)(a => pure(f(a)))
}

object Monad {

  implicit val listMonad = new Monad[List] {
    override def pure[A](a: A): List[A] = List(a)
    override def flatMap[A, B](ma: List[A])(f: A => List[B]): List[B] = ma.flatMap(f)
  }

  implicit val optionMonad = new Monad[Option] {
    override def pure[A](a: A): Option[A] = Option(a)
    override def flatMap[A, B](ma: Option[A])(f: A => Option[B]): Option[B] = ma.flatMap(f)
  }

  // other manads...

  implicit class Ma2Monad[M[_], A](ma: M[A]) {

    def flatMap[B](f: A => M[B])(implicit monad: Monad[M]): M[B] = monad.flatMap(ma)(f)

    def map[B](f: A => B)(implicit monad: Monad[M]): M[B] = monad.map(ma)(f)
  }

}