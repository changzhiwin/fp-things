# Tagless

减少代码的`Tag`标记，强制类型转换，提供类型安全（Type Safe）。

## 解决了什么问题
首先来看一个表达式计算的例子，如何兼容`Boolean`和`Int`的返回结果？只好返回`AnyVal`。代码有点长，先忍耐一下
```
object Tagmore {
  trait Expr {
    def tag: String
  }

  case class B(a: Boolean) extends Expr {
    def tag: String = "boolean"
  }
  case class I(a: Int) extends Expr {
    def tag: String = "int"
  }

  case class Not(a: Expr) extends Expr {
    def tag: String = "boolean"
  }

  case class And(left: Expr, right: Expr) extends Expr {
    def tag: String = "boolean"
  }

  case class Or(left: Expr, right: Expr) extends Expr {
    def tag: String = "boolean"
  }

  case class Sum(left: Expr, right: Expr) extends Expr {
    def tag: String = "int"
  }

  def eval(e: Expr): AnyVal = {
    e match {
      case B(a) => a
      case I(a) => a
      case Not(a) => {
        if (a.tag == "boolean")
          !(eval(a).asInstanceOf[Boolean])
        else
          throw new NoSuchElementException("Error Expr")
      }
      case And(left, right) => {
        if (left.tag == "boolean" && right.tag == "boolean") 
          eval(left).asInstanceOf[Boolean] && eval(right).asInstanceOf[Boolean]
        else 
          throw new NoSuchElementException("Error Expr")
      }
      case Or(left, right) => {
        if (left.tag == "boolean" && right.tag == "boolean") 
          eval(left).asInstanceOf[Boolean] || eval(right).asInstanceOf[Boolean]
        else
          throw new NoSuchElementException("Error Expr")
      }
      case Sum(left, right) => {
        if (left.tag == "int" && right.tag == "int")
          eval(left).asInstanceOf[Int] + eval(right).asInstanceOf[Int]
        else
          throw new NoSuchElementException("Error Expr")
      }
    }
  }

  def demo(): Unit = {
    val e1: Expr = And(Not(B(false)), Or(B(2 > 1), B(false)))
    val e2: Expr = Sum(Sum(I(1), I(2)), Sum(I(3), Sum(I(4), I(5))))

    println(s"Tagemore: e1 = ${eval(e1).asInstanceOf[Boolean]}, e2 = ${eval(e2).asInstanceOf[Int]}")
  }
}
```
这段代码解决了需求，不是不能用；但有很多的雷区：
- 可读性没啥问题，但维护性差
- 对类型检查支持不足，类型错误需要延迟到运行态
- 无法返回该有的类型，需要频繁的强制类型转换

本质的问题是为了满足兼容性，选择失去类型安全。我们需要寻找两者兼得的方法，比较容易想到的是泛型，下面是一种实现：
```
object TaglessInitial {

  sealed trait Expr[A]
  case class Value[A](a: A) extends Expr[A]
  case class Not(a: Expr[Boolean]) extends Expr[Boolean]
  case class And(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
  case class Or(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
  case class Sum(a: Expr[Int], b: Expr[Int]) extends Expr[Int]
  
  def eval[A](e: Expr[A]): A = {
    e match {
      case Value(a) => a
      case Not(a) => !eval(a)
      case And(a, b) => eval(a) && eval(b)
      case Or(a, b) => eval(a) || eval(b)
      case Sum(a, b) => eval(a) + eval(b)
    }
  }

  def demo(): Unit = {
    val e1: Expr[Boolean] = And(Not(Value(false)), Or(Value(2 > 1), Value(false)))
    val e2: Expr[Int] = Sum(Sum(Value(1), Value(2)), Sum(Value(3), Sum(Value(4), Value(50))))

    println(s"Tageless: e1 = ${eval(e1)}, e2 = ${eval(e2)}")
  }
}
```
代码少了很多，这都不是**最亮眼**的提升；它没有了`tag`，更优雅的是类型错误的检查发生在编译态，不接受你写错误的代码。

这个就是当下我理解到的`Tagless`的意思。

# Tagless Final

> Tagless final in the original paper is much more than a design pattern, it’s a way of creating new “languages”

缩写为`TF`，分开来讲，因为`TF`比较强大，这里只描述理解到了的一面。

上面`Tagless`的实现方式，其实挺不错的了，要说瑕疵的话，还是有一点：像上面的`e1`、`e2`都不是**最后**的值，还需要经过`eval()`，才是结果。

我们再努力一下，让它变的更雅一些:
```
object TaglessFinal {
  trait Expr[A] {
    def value: A
  }
  
  def v[A](a: A): Expr[A] = new Expr[A] {
    def value: A = a
  }

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
    val e1: Expr[Boolean] = and(not(v(false)), or(v(true), v(false)))
    val e2: Expr[Int] = sum(sum(v(1), v(2)), sum(v(3), sum(v(4), v(5))))

    println(s"Tageless Final V1: e1 = ${e1.value}, e2 = ${e2.value}")
  }
}
```
直观的对比一下两种表达式的写法：
- `And(Not(Value(false)), Or(Value(2 > 1), Value(false)))`
- `and(not(v(false)), or(v(true), v(false)))`

上面的写法像是类型，下面的写法更像是函数。更重要的是这种实现，不需要`eval()`来支持，可以直接取到`value`，也就是**最后**得到的就是值。

**最后**得到的是值，这就是`Final`的一层意思。也是`Tagless Final`的一种理解。

# 与type class的结合
先给两者各自擅长的领域下个定义
- `type class`：在某些类型之上，再抽象一组功能（function），如`Monad[M[_]]`
- `tagless final`：在某些类型之上，规约表达方式，使其类型安全（type safe），如上面例子

直接来看结合后的效果：
```
object TaglessFinal_TypeClass {

  trait Algebra[E[_]] {
    def b(a: Boolean): E[Boolean]
    def i(a: Int): E[Int]
    def not(a: E[Boolean]): E[Boolean]
    def sum(left: E[Int], right: E[Int]): E[Int]
    def and(left: E[Boolean], right: E[Boolean]): E[Boolean]
    def or(left: E[Boolean], right: E[Boolean]): E[Boolean]
  }

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
```
仔细观察一下代码，有没有发现什么模式？

哦，确实，这不就是面向接口编程（programming to interfaces）吗？表达的方式已经在`trait Algebra[E[_]]`定义好了，`SimpleExpr`只是一种实现，完全可以用其他实现替换。

# 总结
描述了对`Tagless`、`Tagless Final`的理解，以及和`type class`之间的分界；将两者结合，展现了抽象力的申神奇效果。

当然，这不是它们的全部，需要逐步去理解，但已经感受到了它们的强大。

感谢，[Daniel](https://blog.rockthejvm.com/tagless-final/)的分享，延展了我的思维。