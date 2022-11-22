package fp.things.typelevel

import java.lang.annotation.Native

object PeanoNumber {

  import scala.reflect.runtime.universe._

  def show[T](value: T)(implicit tag: TypeTag[T]) = tag.toString.replace("fp.things.typelevel.Utils.", "")

  trait Nat
  class _0 extends Nat
  class Succ[N <: Nat] extends Nat

  type _1 = Succ[_0]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]

  trait <=[A <: Nat, B <: Nat]
  object <= {
    implicit def lteBasic[B <: Nat]: <=[_0, Succ[B]] = new <=[_0, Succ[B]] {}

    // 特别注意：隐式参数 A/B 比 返回类型少一级 Succ[A]/Succ[B]
    implicit def inductive[A <: Nat, B <: Nat](implicit lt: <=[A, B]): <=[Succ[A], Succ[B]] = new <=[Succ[A], Succ[B]] {}

    def apply[A <: Nat, B <: Nat](implicit lt: <=[A, B]): <=[A, B] = lt
  }

  def main(args: Array[String]): Unit = {

    println(show(new Succ()))  // TypeTag[Succ[Nothing]]
    println(show(new _1()))    // TypeTag[Succ[_0]]

    println(show(<=[_0, _3]))  // TypeTag[_0 <= _3]
    // <.apply[_0, _3] 依赖隐式参数 <=[_0, _3]
    // lteBasic[_2] 可以得到 <=[_0, Succ[_2]] 也就是 <=[_0, _3]

    println(show(<=[_2, _5]))  // TypeTag[_2 <= _5]
    // <.apply[_2, _5] 需要隐式参数 <=[_2, _5]
    // inductive[_1, _4] 可以得到 <=[Succ[_1], Succ[_4]]，也就是 <=[_2, _5]；需要隐式参数<=[_1, _4]
    // inductive[_0, _3] 可以得到 <=[Succ[_0], Succ[_3]]，也就是 <=[_1, _4]；需要隐式参数<=[_0, _3]
    // ltBasic[_2] 可以得到 <=[_0, Succ[_2]] 也就是<=[_0, _3]

  }

}

// Ref:
// https://www.youtube.com/watch?v=qwUYqv6lKtQ&list=PLmtsMNDRU0ByOQoz6lnihh6CtMrErNax7
// https://blog.rockthejvm.com/type-level-programming-part-1/