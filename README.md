#### FP门槛确实很高
如果把`OOP`比作是**道**，那么`FP`说成是**魔**一点不夸张。这里有两层意思:
- 它太性感了，会让部分人着**魔**
- 具备一些**魔法**，主要是`category theory`

#### 抽象的抽象
上大学那会，就觉得设计模式很高深，部分模式还不能理解；说的简单点，人的抽象能力有限，但可以通过训练，做的好一些。这里想到了王烁老师`思维的阶层`里面讲的，好像也适合软件开发：
> 现实中，大多数时候大多数地方大多数问题上，人们在零阶和一阶，所以对聪明人来说，二阶思维比较重要

> 思维阶次理论上可以无限上升，实际上是三阶以上大脑会热到爆炸

- 零阶，不会开发
- 一阶，面向过程，写一行代码，执行一句
- 二阶，面向对象，封装/反转
- 三阶，代码的代码，`category theory`就在这一层，太高阶了，烧脑，慎入

`category theory`是把`FP`中用到的一类设计模式数学化了，本质还是解决软件设计的问题。不必非得从范畴论的角度去看`FP`，可以从解决软件系统中问题的角度，去理解设计。


#### Monad
在容器之上抽象，这里`M[_]`就是一个容器。具备两个能力，就属于`Monad`:
- pure， 把基础类型放入容器，也可以理解为包裹起来
- flatMap，把包裹`M[A]`转换成`M[B]`，本质是封装一个操作序列：`提取` -> `转换` -> `再包裹`
- map，这个可以用上面两个操作实现

详细实践参见[CombineContainer](./src/main/scala/fp/things/app/CombineContainer.scala)
```
trait Monad[M[_]] {
  def pure[A](a: A): M[A]
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
  def map[A, B](ma: M[A])(f: A => B): M[B] = flatMap(ma)(a => pure(f(a)))
}
```

#### Free Monad
这个抽象在`Monad`之上，它能解决软件设计中逻辑和执行的分离，不好理解？建议了解一下[ZIO](https://zio.dev/reference/)。
- 对测试过程极度友好，因为执行环境可以随意切换
- ATD, algebra data type

详细实践参见[DatabaseOps](./src/main/scala/fp/things/app/DatabaseOps.scala)
```
trait ~>[F[_], G[_]] {
  def apply[A](fa: F[A]): G[A]
}

trait Free[M[_], A] {
  import Free._

  def flatMap[B](f: A => Free[M, B]): Free[M, B] = FlatMap(this, f)

  def map[B](f: A => B): Free[M, B] = flatMap(a => pure(f(a)))

  // 解释器: 完成逻辑脚本 到 可执行的代码
  def foldMap[G[_]](natTrans: M ~> G)(implicit monad: Monad[G]): G[A] = this match {
    case Pure(a)        => monad.pure(a)
    case Suspend(ma)    => natTrans(ma)
    case FlatMap(fa, f) => monad.flatMap(fa.foldMap(natTrans))(a => f(a).foldMap(natTrans))
  }
}

object Free {
  def pure[M[_], A](a: A): Free[M, A] = Pure(a)
  def liftM[M[_], A](ma: M[A]): Free[M, A] = Suspend(ma)

  case class Pure[M[_], A](a: A) extends Free[M, A]
  case class FlatMap[M[_], A, B](fa: Free[M, A], f: A => Free[M, B]) extends Free[M, B]
  case class Suspend[M[_], A](ma: M[A]) extends Free[M, A]
}
```

#### type class
在实现[zio-actor](https://github.com/changzhiwin/zio-actor)的时候，被`Actor[-F[+_]]`定义搞蒙了，不知道该怎么表达参数：`actorMap: Ref[Map[String, _ <: Actor[Any]]]`，本意是任意的`Actor[F]`实例，引出了对`type constructor`的理解。
```
// 尝试过错误的写法
actorMap: Ref[Map[String, Actor[_]]]

actorMap: Ref[Map[String, Actor]]
```
`Actor[-F[+_]]`表达的意思是`Actor`是一个`type class`，需要传入类型参数，才能转换成类型；而这个类型参数是：带有一个类型参数的类型构造函数。
我们来分析一下如何传这个参数：
- `Map[K, +V]`需要的传入的是类型，而不是`type class`；所以`Map[String, Actor]`肯定不对;
- `Actor[_]`表达的是：需要一个类型参数的类型构造函数，也不是类型；所以`Map[String, Actor[_]]`也是不对的;
- `Actor[Any]`传入了类型参数`Any`转换成了类型，`Any`是所有类型的父类，满足`Acotr[-F]`的约束（Contra-Variant）；所以`Map[String, Actor[Any]]`是可以的
- 使用`_ <: Actor[Any]`是为了表达明确的类型限制语义，直接使用`Map[String, Actor[Any]]`也是正确的。

**小结一下**:
- 普通类型，可以直接构造实例，例如`class Foobar(n: Int)`，`Foobar`就是普通类型
- 类型构造函数，需要传入参数才可以成为普通类型，例如`Seq[T]`、`Option[T]`，`Seq`、`Option`就是 *一个类型参数的* 类型构造函数
- 类型类（type class），例如`Actor[-F[+_]]`、`Free[M[_], A]`、`Seq[T]`
- 类型类，适用于定义场景
- 普通类型、类型构造函数，适用于使用场景，例如：参数声明


#### 可以挑战一下
看明白`Monad`，`Free Monad`我花了5个工作日，这还不能算是完全掌握。你可以试试看。
- [Monad](https://blog.rockthejvm.com/monads/)
- [Free Monad](https://blog.rockthejvm.com/free-monad/)

#### 执行
```
> sbt

sbt:fp-things> runMain fp.things.app.MonadApp

sbt:fp-things> runMain fp.things.app.FreeApp
```

#### TODO
- https://blog.rockthejvm.com/monads-are-monoids-in-the-category-of-endofunctors/