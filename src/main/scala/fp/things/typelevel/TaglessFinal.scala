package fp.things.typelevel

// 表达式是一种树状的数据结构
// 以表达式(Expr)为例，理解Tagless
// 

object TaglessInitial {

  sealed trait Expr[A]
  /*
  case class B(a: Boolean) extends Expr[Boolean]
  case class I(a: Int) extends Expr[Int]
  */
  case class Value[A](a: A) extends Expr[A]
  case class Not(a: Expr[Boolean]) extends Expr[Boolean]
  case class And(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
  case class Or(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
  case class Sum(a: Expr[Int], b: Expr[Int]) extends Expr[Int]
  
  // 这里其实是解析、执行
  def eval[A](e: Expr[A]): A = {
    e match {
      //case B(a) => a
      //case I(a) => a

      // 这里解释一下：Value(a)这个写法其实调用了Companion的unapply
      case Value(a) => a
      case Not(a) => !eval(a)
      case And(a, b) => eval(a) && eval(b)
      case Or(a, b) => eval(a) || eval(b)
      case Sum(a, b) => eval(a) + eval(b)
    }
  }

  def demo(): Unit = {
    // 计算结构的表达，但不产生计算的结果
    val e1: Expr[Boolean] = And(Not(Value(false)), Or(Value(2 > 1), Value(false))) // And(Not(B(false)), Or(B(2 > 1), B(false)))
    val e2: Expr[Int] = Sum(Sum(Value(1), Value(2)), Sum(Value(3), Sum(Value(4), Value(50)))) // Sum(Sum(I(1), I(2)), Sum(I(3), Sum(I(4), I(5))))

    // 需要解析执行一遍，才能得到结果
    println(s"Tageless Initial: e1 = ${eval(e1)}, e2 = ${eval(e2)}")
  }
}

object TaglessFinal_V1 {
  trait Expr[A] {
    def value: A
  }
  
  /*
  def b(a: Boolean): Expr[Boolean] = new Expr[Boolean] {
    def value: Boolean = a
  }

  def i(a: Int): Expr[Int] = new Expr[Int] {
    def value: Int = a
  }
  */

  // 上面两个的结构完全一样，可以抽象一下：
  def v[A](a: A): Expr[A] = new Expr[A] {
    def value: A = a
  }

  // 这种方式把类型定义和计算放在了一起，返回带计算能力的实例
  def not(e: Expr[Boolean]) = new Expr[Boolean] {
    def value: Boolean = !e.value
  }

  def and(e1: Expr[Boolean], e2: Expr[Boolean]) = new Expr[Boolean] {
    def value: Boolean = e1.value && e2.value
  }

  def or(e1: Expr[Boolean], e2: Expr[Boolean]) = new Expr[Boolean] {
    def value: Boolean = e1.value || e2.value
  }

  def sum(e1: Expr[Int], e2: Expr[Int]) = new Expr[Int] {
    def value: Int = e1.value + e2.value
  }

  def demo(): Unit = {
    val e1: Expr[Boolean] = and(not(v(false)), or(v(true), v(false))) // And(Not(B(false)), Or(B(2 > 1), B(false)))
    val e2: Expr[Int] = sum(sum(v(1), v(2)), sum(v(3), sum(v(4), v(5)))) // Sum(Sum(I(1), I(2)), Sum(I(3), Sum(I(4), I(5))))
    println(s"Tageless Final V1: e1 = ${e1.value}, e2 = ${e2.value}")

    // Error: type mismatch
    // sum(v(true), v(2)).value
  }
}

object TaglessFinal_V2 {

  // 把包裹透明化，可以切换多种包裹方式
  trait Algebra[E[_]] {
    def b(a: Boolean): E[Boolean]
    def i(a: Int): E[Int]
    def not(a: E[Boolean]): E[Boolean]
    def sum(left: E[Int], right: E[Int]): E[Int]
    def and(left: E[Boolean], right: E[Boolean]): E[Boolean]
    def or(left: E[Boolean], right: E[Boolean]): E[Boolean]
  }

  // 一种包裹方式的实现
  case class SimpleExpr[A](value: A)
  implicit val simpleExpr = new Algebra[SimpleExpr] {
    def b(a: Boolean): SimpleExpr[Boolean] = SimpleExpr(a)
    def i(a: Int): SimpleExpr[Int] = SimpleExpr(a)
    def not(a: SimpleExpr[Boolean]): SimpleExpr[Boolean] = SimpleExpr(!a.value)
    def sum(left: SimpleExpr[Int], right: SimpleExpr[Int]): SimpleExpr[Int] = SimpleExpr(left.value + right.value)
    def and(left: SimpleExpr[Boolean], right: SimpleExpr[Boolean]): SimpleExpr[Boolean] = SimpleExpr(left.value && right.value)
    def or(left: SimpleExpr[Boolean], right: SimpleExpr[Boolean]): SimpleExpr[Boolean] = SimpleExpr(left.value || right.value)
  }

  def program1[E[_]](implicit expr: Algebra[E]): E[Boolean] = {
    import expr._

    // 这里的计算描述，其实是不需要理解具体是那个解释器的；解释器使用implicit声明
    // 这也是面向接口编程的一种理念，非常棒！！
    and(or(b(true), b(false)), not(b(false)))
  }

  def program2[E[_]](implicit expr: Algebra[E]): E[Int] = {
    import expr._

    sum(i(10), sum(i(20), sum(i(30), i(41))))
  }

  def demo(): Unit = {
    println(s"Tagless Final V2: ${program1}, ${program2}")
  }

}

object TaglessFinal {

  def main(args: Array[String]): Unit = {

    TaglessInitial.demo()

    TaglessFinal_V1.demo()

    TaglessFinal_V2.demo()
  }
}

// Ref: https://blog.rockthejvm.com/tagless-final/