package fp.things.app

import fp.things.free.{FreeMonad, IO}

sealed trait DatabaseOps[A]
case class Create[A](key: String, value: A) extends DatabaseOps[Unit]
case class Read[A](key: String) extends DatabaseOps[A]
case class Update[A](key: String, value: A) extends DatabaseOps[A]
case class Delete(Key: String) extends DatabaseOps[Unit]

object DatabaseOps {
  // type DBMonad[A] = FreeMonad.Free[DatabaseOps, A]

  import FreeMonad.{Free}

  def create[A](key: String, value: A): Free[DatabaseOps, Unit] = Free.liftM[DatabaseOps, Unit](Create[A](key, value))

  def get[A](key: String): Free[DatabaseOps, A]                 = Free.liftM[DatabaseOps, A](Read[A](key))

  def update[A](key: String, value: A): Free[DatabaseOps, A]    = Free.liftM[DatabaseOps, A](Update[A](key, value))

  def delete(key: String): Free[DatabaseOps, Unit]              = Free.liftM[DatabaseOps, Unit](Delete(key))

  def myProgram: Free[DatabaseOps, Unit] = for {
    t    <- Free.pure("uncle sam")
    _    <- create[String]("123-456", t)                   // Suspend(Create("123-456", "Uncle Sam")).flatMap(f) == FlatMap(this, f)
    name <- get[String]("123-456")                         // Suspend(Read("123-456"))
    _    <- update[String]("123-456", name.toUpperCase())  // Suspend(Update("123-456", name.toLowerCase()))
    _    <- delete("123-456")                              // Suspend(Delete("123-456"))
  } yield()
}

object FreeApp {

  val memDB: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map()
  def serialize[A](a: A): String = a.toString
  def deserialize[A](value: String): A = value.asInstanceOf[A]

  import FreeMonad.{~>}
  val dbOps2IO: ~>[DatabaseOps, IO] = new ~>[DatabaseOps, IO] {
    def apply[A](fa: DatabaseOps[A]): IO[A] = fa match {
      case Create(key, value) => IO.create {
        println(s"insert into person(id, name) values ($key, $value)")
        memDB += (key -> serialize(value))
        ()
      }
      case Read(key) => IO.create {
        println(s"select * from person where id=$key limit 1")
        deserialize[A](memDB(key))
      }
      case Update(key, value) => IO.create {
        println(s"update person(name=$value) where id=$key")
        val old = memDB(key)
        memDB += (key -> serialize(value))
        deserialize[A](old)
      }
      case Delete(key) => IO.create {
        println(s"delete from person where id=$key")
        ()
      }
    }
  }

  implicit val iomonad = IO.ioMonad
  val ioProgram: IO[Unit] = DatabaseOps.myProgram.foldMap(dbOps2IO)

  def main(args: Array[String]): Unit = {
    ioProgram.unsafeRun()
  }
}


