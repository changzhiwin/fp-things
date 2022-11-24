package fp.things.typelevel

// Generics, 泛型，模板，一种抽象方式
// 作用：
// 1，代码复用，具有模板的特性，一套代码打天下
// 2，type safe，这是一个终极追求，越高级的程序员越想要

object Generics {

  // 问题来源于这样的场景：希望下面的Int可以替换成任意的类型，但不需要每个类型单独定义
  // 想到用Any来替代Int，是很正常的；但这完全丧失了type safe，开启了危险代码的大门
  sealed trait IntList {
    def head: Int
    def tail: IntList

    final def touch(f: Int => Unit): Unit = this match {
      case IntList.SomeIntList(head, tail) => {
        f(head)
        tail.touch(f)
      }
      case IntList.NilIntList => // do nothing
    }
  }

  object IntList {
    object NilIntList extends IntList {
      def head: Int = throw new NoSuchElementException()
      def tail: IntList = throw new NoSuchElementException()
    }

    case class SomeIntList(h: Int, t: IntList) extends IntList {
      def head: Int = h
      def tail: IntList = t
    }

    def demo(): Unit = {
      // 问题：只适用于Int
      val intList = SomeIntList(1, SomeIntList(2, SomeIntList(3, NilIntList)))
      intList.touch(println(_))
    }
  }

  sealed trait SafeList[T] {
    def head: T
    def tail: SafeList[T]
    
    final def touch(f: T => Unit): Unit = this match {
      case SafeList.SomeSafeList(h, t) => {
        f(h)
        t.touch(f)
      }
      case SafeList.NilSafeList()      => // do nothing
    }
  }

  object SafeList {
    // 这里需要定义成class，因为有参数T
    case class NilSafeList[T]() extends SafeList[T] {
      def head: T = throw new NoSuchElementException
      def tail: SafeList[T] = throw new NoSuchElementException
    }

    case class SomeSafeList[T](head: T, tail: SafeList[T]) extends SafeList[T] {}

    def demo(): Unit = {
      val safeList = SomeSafeList("a", SomeSafeList("bb", SomeSafeList("ccc", NilSafeList())))

      // 关键点：里面是String类型，使用String的操作是安全的
      safeList.touch(s => println(s"${s} -> ${s.length}"))
    }
  }

  def main(args: Array[String]): Unit = {
    
    IntList.demo()

    SafeList.demo()
  }
}

// Ref: https://blog.rockthejvm.com/scala-generics/