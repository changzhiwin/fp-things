package fp.things.typelevel

object TaglessInit {

  sealed trait Expr[A]
  case class B(a: Boolean) extends Expr[Boolean]
  case class I(a: Int) extends Expr[Int]
  case class Not(a: Expr[Boolean]) extends Expr[Boolean]
  case class And(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
  case class Or(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
  case class Sum(a: Expr[Int], b: Expr[Int]) extends Expr[Int]
  
  def eval[A](e: Expr[A]): A = {
    e match {
      case B(a) => a
      case I(a) => a
      case Not(a) => !eval(a)
      case And(a, b) => eval(a) && eval(b)
      case Or(a, b) => eval(a) || eval(b)
      case Sum(a, b) => eval(a) + eval(b)
    }
  }

  def demo(): Unit = {
    val e1: Expr[Boolean] = And(Not(B(false)), Or(B(2 > 1), B(false)))
    val e2: Expr[Int] = Sum(Sum(I(1), I(2)), Sum(I(3), Sum(I(4), I(5))))
    println(s"e1 = ${eval(e1)}, e2 = ${eval(e2)}")
  }
}

object TaglessFinal_V1 {

}

object TaglessFinal_V2 {

}

object TaglessFinal {

  def main(args: Array[String]): Unit = {

    TaglessInit.demo()
  }
}