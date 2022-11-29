package fp.things.typelevel

import scala.reflect.ClassTag

object TypeClass {

  trait Summable[T] {
    def sum(list: List[T]): T
  }

  object Summable {
    implicit val intSummable = new Summable[Int] {
      def sum(list: List[Int]): Int = list.sum
    }

    implicit val strSummable = new Summable[String] {
      def sum(list: List[String]): String = list.mkString(" ")
    }
  }

  def processSum[T](list: List[T])(implicit summable: Summable[T]): T = {
    summable.sum(list)
  }

  def processSumOld[T](list: List[T]): T = {

    val head = list.head
    // type erase !!!
    head match {
      case _: String => (list.asInstanceOf[List[String]].mkString("")).asInstanceOf[T]
      case _: Int    => (list.asInstanceOf[List[Int]].sum).asInstanceOf[T]
      case _         => throw new Exception("not support")
    }
  }

  def main(args: Array[String]): Unit = {

    import Summable._
    val intsum = processSum(List(1, 2, 3))
    val strsum = processSum(List("Hi", "Scala"))

    println(s"intsum = ${intsum}")
    println(s"strsum = ${strsum}")

    println(processSumOld(List("A", " B", " C")))
    println(processSumOld(List(1, 2, 3)))
  }

}