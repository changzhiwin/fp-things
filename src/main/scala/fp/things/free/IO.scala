package fp.things.free

import FreeMonad.Monad

case class IO[A](unsafeRun: () => A)

object IO {
  def create[A](a: => A): IO[A] = IO( () => a )

  val ioMonad = new Monad[IO] {
    override def pure[A](a: A): IO[A] = IO( () => a )

    // IO( () => f(ma.unsafeRun()).unsafeRun() )
    override def flatMap[A, B](ma: IO[A])(f: A => IO[B]): IO[B] = f(ma.unsafeRun())
  }
}