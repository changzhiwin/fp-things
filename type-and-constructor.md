# 值、类型、类型构造函数
[Daniel](https://blog.rockthejvm.com/)说这个主题，很多高级程序员都没弄明白。我想大概的原因，一是日常的业务实现，很少涉及更高层次的抽象；二来，高级程序员并不是代码的设计能力来衡量的。

首先，我们来做一个定义：
- 值，具体的内容；如`2`，`"Hello"`，`new Person("Bob", 44)`
- 0阶类型，对值的抽象和归类，如`Int`，`String`，`Person`
- 1阶类型，对0阶类型的抽象，如`List[T]`，`Option[T]`，`Map[K, V]`
- 2阶类型，对1阶类型的抽象，如`Actor[F[_]]`，`Monad[M[_]]`
- 3阶以上，更高维度的抽象，可以一直往上；但不好理解，最好不用

## 值和0阶类型
这是最常见的、最容易理解的。想当年，学习编程一上来就要掌握基本类型，对吧？然后各种面向对象编程，这里的指的对象，大意是指的**0阶类型**，如Java同学经常讲的`BO`、`VO`等。
```
// Int
val n: Int = 12
// String
val s: String = "Scala"
// Person
case class Person(name: String, age: Int)
val person = Person("Bob", 32)
```
这里要再提一个熟悉的概念，**构造函数（constructor）**：0阶类型也是值的构造函数。不用太多解释，大家都能明白，只是有时候，构造函数可能隐藏起来了，但实际上还是在那里。

## 1阶类型
换了和这个统一的命名方式，其他叫法如“模板”、“泛型”，还是属于平常容易见到的。最常见的用途就是代码复用，一套模板打天下。例如List[T]就是封装的一组集合功能，而不用管集合里面具体是那种类型的值。

为什么叫1阶了？因为无法直接和值进行关联，`new List`这是个什么东东？编译器不认识的。需要传递一个0阶类型，才能把它和值关联起来，例如`List[String]`，用`String`这个0阶类型，把`List`这个1阶类型降维成0阶，这样就可以实例化了`new List[String]("Hi")`。

再归纳一下：1阶类型需要传递0阶类型，**作为参数**，得到0阶类型之后，才能使用。

## 2阶类型
这里才是疑惑的零界点，因为上面说的内容，都是`OOP`里经常念叨的，不懂的也都磨破了。

从上面两段描述，我们可以大胆推断：2阶类型就是需要使用1阶类型**作为参数**，才能得到0阶类型；例如`Actor[F[_]]`、`Monad[M[_]]`。嗯，这个定义我看懂了，可这有什么用了？我不懂这个东西，好像也写了N年代码了啊。我们来看一个需求吧：
```
// 实现一个统一foldLeft的接口，能在多种集合类型之上操作，如List[T]，Set[T]，Option[T]，Try[T]

val seqint    = MyFoldLeft(  Seq(1, 2, 5) )(    2   )( (o, i) => o + i)  // 10
val optionstr = MyFoldLeft(Option("Scala"))( "Hello")( (o, i) => o + i)  // HelloScala
```
不用纠结，哪里会有这种需求；我只能说框架或库里面很多这种代码。从抽象层面看，这是在集合类型（1阶）之上的再次抽象，下面给出一种实现方式：
```
object MyFoldLeft {
  
  // Folder是2阶类型
  trait Folder[M[_]] {
    def apply[IN, OUT](m: M[IN], seed: OUT, f: (OUT, IN) => OUT): OUT
  }

  // Iterable是1阶类型: trait Iterable[+A]
  implicit val iterFolder = new Folder[Iterable] {
    override def apply[IN, OUT](m: Iterable[IN], seed: OUT, f: (OUT, IN) => OUT): OUT = {
      var accumulator = seed
      m.foreach(t => accumulator = f(accumulator, t))
      accumulator
    }
  }

  // Option是1阶类型: class Option[+A]
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
    val seqint    = MyFoldLeft(  Seq(1, 2, 5) )(    2   )( (o, i) => o + i)  // 10
    val optionstr = MyFoldLeft(Option("Scala"))( "Hello")( (o, i) => o + i)  // HelloScala
  }
}
```


## 类型构造函数，type constructor
上面有说到了0阶类型是值的构造函数。1阶及以上的类型定义，其实都是类型构造函数，只是使用参数类型不同。举几个例子，这个概念还是不难理解的：
- `Int/String/Person`，是0阶类型，也是值的构造函数
- `List[T]`是1阶类型，List是类型构造函数，需要传递0阶类型，如`List[Person]`
- `Map[K, V]`是1阶类型，Map是类型构造函数，需要传递0阶类型，如`Map[String, User]`
- `Folder[M[_]]`是2阶类型，Folder是类型构造函数，需要传递1阶类型，如`Folder[Option]`

因为我们编写的执行态代码，都需要在0阶上，所以高阶类型都需要实例化到0阶，才能发挥作用。加深一下理解，可以对比一下函数的使用：
- 0阶函数：f(a: Int): String，传递值得到值
- 1阶函数: map(f: Int => String)，传递函数得到值

注意：1阶及以上的函数被称为**高阶函数**(higher-function)，1阶及以上的类型被称为**高阶类型**（higher-kind）

## 3阶及以上
算了，这个就不写了，理论层面推导一下就行。

### 小结一下
我是在写[zio-actor](https://github.com/changzhiwin/zio-actor)时，遇到了高阶参数类型声明，怎么写怎么不对，研究了一下`type constructor`这个概念。这种分阶的定义模型，可以统一概念，理解起来容易很多。感谢一下[Danile](https://blog.rockthejvm.com/scala-types-kinds/)老师。

长这么大，有三次遇到了这种分阶的模型：
- 大学时学微积分
- 得到上听王烁的老师讲过
- 学Scala