# Monad
这是个抽象的概念，我们程序员，将它视为工具，为我们所用就好，而未必非得完完全全弄清楚它到底是什么之后，才能写代码。

就像活着的意义到底是什么？不能因为没有领悟意义，就不活了；而应该先活着，再尝试寻找意义。

下面从不同层面来认识一下`Monad`：

## 接口定义层面
存在容器`C[_]`，在`C[_]`之上，支持`prue`和`flatMap`两个操作，我们可以认为这个容器就属于`Monad`。
```
trait Monad[C[_]] {
  def prue[A](a: A): C[A]
  def flatMap[A](f: A => C[B]): C[B]

  // 用上面两个操作可以衍生map操作
  def map[A](f: A => B): C[B] = flatMap(a => prue(f(a)))
}
```
由上面的定义可以看出，首先需要存在`C[_]`，所以这个概念不是发生在常规类型（Int/String/Person）层面。需要特别注意的是，定义中各种**参数类型**声明：
- `A`是0阶类型（常规类型）
- `C[_]`是1阶类型
- `Monad[M[_]]`是2阶类型
- `prue`和`flatMap`的返回类型都是1阶类型`C[_]`
- `f: A => C[B]`从0阶类型到1阶类型


## 使用过的经验层面
虽然对`Monad`这个概念比较陌生，但背后的逻辑，在实际编码过程中使用的很平凡（频繁）。下面例举三个肯定用过的Case，仔细体会，相信对上面的定义有更多的理解

### Option
```
val opt = Option(3)
// val opt: Option[Int] = Some(3)

opt.flatMap(n => Option(s"${n} times"))
// val res0: Option[String] = Some(3 times)
```
对比一下上面的接口定义，一条一条看
- `A`是`Int`
- `C[_]`是`Option[_]`
- `prue`是`Option()`，例子中从`Int`得到`Option[Int]`
- `f: A => C[B]`是`f: Int => Option[String]`

### List
```
val list = List("scala")
// val list: List[String] = List(scala)

val size = list.flatMap(s => List(s.size))
// val size: List[Int] = List(5)
```
和`Option`几乎是一样的，就不赘述

### for comprehension
```
for {
  a <- List(1, 2, 3)
  b <- List('A', 'B', 'C')
} yield (a, b)

// val res0: List[(Int, Char)] = List((1,A), (1,B), (1,C), (2,A), (2,B), (2,C), (3,A), (3,B), (3,C))
```
我们来把`for`用`Monad`的能力翻译一下：
```
List(1, 2, 3).flatMap { a =>

  List('A', 'B', 'C').flatMap { b =>

    List( (a, b) )
  }
}

// val res1: List[(Int, Char)] = List((1,A), (1,B), (1,C), (2,A), (2,B), (2,C), (3,A), (3,B), (3,C))
```
结果是一样的，因为编译器里面也是把`for`翻译成`Monad`的这种语法，可以认为`for`是一个语法糖。也可以使用`map`来重写一下：
```
List(1, 2, 3).flatMap { a =>

  List('A', 'B', 'C').map { b => (a ,b)}
}

// val res2: List[(Int, Char)] = List((1,A), (1,B), (1,C), (2,A), (2,B), (2,C), (3,A), (3,B), (3,C))
```
并没有什么神奇之处，因为`map`是用`prue`和`flatMap`衍生出来的。

## Design Pattern层面
`C[_]`我们把它类比成容器，这样借助现实世界的事物来理解，容易帮助我们消化；我在理解的时候把它类比成**包裹**，可能比容器更加具象一些。加这样一层抽象，有个很大的好处：**空包裹也是包裹**，可以用包裹的方式统一处理。我们可以用`Option`的实际代码来体会一下：
```
case class User(name: String, age: Int)

// 这个写法，在Java里面很常见
def getUser(name: String, age: Int): User = {
  if (name != null && age != null) {
    User(name, age)
  }
  else {
    throw new Exception("Invalid args")
  }
}

// 没了判断，没了手动异常，是不是更加优雅一些？
def getUserOpt(name: Option[String], age: Option[Int]): Option[User] = {
  for {
    n <- name
    a <- age
  } yield User(n, a)
}
```
注意，要转折了。

`Monad`是在容器（包裹）之上，对链式计算模式的一种抽象，使其可以一直`flatMap`/`map`下去（也可以`for`）。`Option`、`List`、`Try`等都实现了这种模式。

## type class层面
在Scala很对内置1阶类型中都实现了`Monad`这种模式，假如我们要在1阶类型之上支持`for`这种操作，该如何处理？来看具体例子：
```
def combine(str: List[String])(num: List[Int]): List[(String, Int)] =
  for {
    s <- str
    n <- num
  } yield (s, n)
```
这里的`combine`只支持`List`，如果需要支持`Option`则需要把代码在`Option`上拷贝一份，但拷贝代码是很忌讳的，需要尝试更优雅的实现，问题的关键需要解决两个问题：
- 在某些特定类型上，支持一组操作
- 这组操作实现`Monad`的定义

仔细理解一下，这就是`type class` + `Monad`组合可以实现的能力，详细[参见](./src/main/scala/fp/things/app/CombineContainer.scala)
```
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

  // 这个隐式转换不可或缺
  implicit class Ma2Monad[M[_], A](ma: M[A]) {
    def flatMap[B](f: A => M[B])(implicit monad: Monad[M]): M[B] = monad.flatMap(ma)(f)
    def map[B](f: A => B)(implicit monad: Monad[M]): M[B] = monad.map(ma)(f)
  }
}
```

## 数学公式层面
满足三个数学规则：
- prue(x).flatMap(f) = f(x)
- prue(x).flatMap(x => prue(x)) = prue(x)
- prue(x).flatMap(f).flatMap(g) = prue(x).flatMap(x => f(x).flatMap(g))
其中，`prue`/`flatMap`/`f`/`g`都是函数，可以拿之前的例子验证一下。
```
List(2).flatMap(n => List(n*2)) == List(2 * 2)

List(2).flatMap(x => List(x)) == List(2)

List(2).flatMap(x => List(x + 2)).flatMap(x => List(x * 2)) == List(2).flatMap(x => List(x + 2).flatMap(x => List(x * 2)))
```