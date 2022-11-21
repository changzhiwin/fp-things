package fp.things.app

// "HK" for higher-kind
object HKFoldLeft {

  // type class, 类型类的定义；类型参数是：带有一个类型参数的类型，也是带有一个类型参数的类型构造函数
  trait Folder[-M[_]] {
    def apply[IN, OUT](m: M[IN], seed: OUT, f: (OUT, IN) => OUT): OUT
  }

  // 类型类的实现，传入类型参数Iterable
  implicit val iterFolder = new Folder[Iterable] {
    override def apply[IN, OUT](m: Iterable[IN], seed: OUT, f: (OUT, IN) => OUT): OUT = {
      var accumulator = seed
      m.foreach(t => accumulator = f(accumulator, t))
      accumulator
    }
  }

  // 类型类的实现，传入类型参数Option
  implicit val optionFolder = new Folder[Option] {
    override def apply[IN, OUT](m: Option[IN], seed: OUT, f: (OUT, IN) => OUT): OUT = {
      m match {
        case Some(v) => f(seed, v)
        case None    => seed
      }
    }
  }

  def apply[IN, OUT, M[IN]](m: M[IN])(seed: OUT)(f: (OUT, IN) => OUT)(implicit folder: Folder[M]): OUT =
    folder(m, seed, f)
}

object Main {

  def main(args: Array[String]): Unit = {
    val seqint = HKFoldLeft(Seq(1, 2, 5))(2)( (o, i) => o + i)
    println(s"HKFoldLeft(Seq(1, 2, 5), 2, (o, i) => o + i) = ${seqint}")

    val optionstr = HKFoldLeft(Option("Scala"))( "Hello")( (o, i) => o + i)
    println(s"""HKFoldLeft(Option("Scala"), "Hello", (o, i) => o + i) = ${optionstr}""")

    val nonestr = HKFoldLeft(Option(null))( "Hello")( (o, i) => o + i)
    println(s"""HKFoldLeft(Option(null), "Hello", (o, i) => o + i) = ${nonestr}""")
  }
}