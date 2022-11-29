# 待解决的问题
下面这个泛型接口，如何实现在`Int`、`String`上都兼容了？
```
// 实现这个List的+功能：
// 对于Int，是求和
// 对于String, 是字符串连接
def processSum[T](list: List[T]): T

// 希望达到的使用效果
val intsum = processSum(List(1, 2, 3))        // 6
val strsum = processSum(List("Hi", " Scala")) // "Hi Scala"
```

# 解决办法一
在运行时判断`list`的类型，不同类型实现不同的代码逻辑
```
def processSumOld[T](list: List[T]): T = {

  val head = list.head
  // type erase !!!
  head match {
    case _: String => (list.asInstanceOf[List[String]].mkString("")).asInstanceOf[T]
    case _: Int    => (list.asInstanceOf[List[Int]].sum).asInstanceOf[T]
    case _         => throw new Exception("not support")
  }
}
```
需求是满足了，但这个方法代码写起来丑就不说，最大的问题类型错误检查发生在运行态。

# 解决办法二
```
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
```
这个是有技术含量的代码，技术体现在：
- `trait`的抽象，定义了一组功能（这里只定义了一个），谁需要谁去实现
- `implicit`的使用，让`compiler`发挥巨大潜力（推导出哪些类型是实现了这组功能的）
- 类型检查发生在编译态

在这个例子中`intSummable`，`strSummable`是`trait Summable[T]`的实现&实例，编译会进行类型推导，选择合适的实现来完成功能输出。

# type class
解决办法二就是`type class`的一个实践，下一个定义:
> type class，使某些类型上具备一组功能，通常会结合`implicit`使得接口十分友好的一种代码设计模式

- 一组功能，由`trait`定义
- 某些类型，需要这组功能的具体类型，各自进行实现，如`intSummable`、`strSummable`
- `implicit`，可选，但会让你的代码看起来爽的不行

# ad hoc polymorphism
暂且翻译成“临时性多态”，我们就按多态的意思去理解。它表达的意思是`type class`这种设计模式，表现出了一种多态的特性：
```
import Summable._
val intsum = processSum(List(1, 2, 3))
val strsum = processSum(List("Hi", "Scala"))
```
一个方法定义，可以作用在不同类型上`List[Int]`、`List[String]`，还可以扩展到其他类型。

# 参考
- https://www.youtube.com/watch?v=bupBZKJT0EA
