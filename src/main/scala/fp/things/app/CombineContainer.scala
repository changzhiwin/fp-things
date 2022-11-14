package fp.things.app

import fp.things.monad.Monad

object CombineContainer {

  import Monad._

  def combine[M[_]](str: M[String])(num: M[Int])(implicit monad: Monad[M]): M[(String, Int)] =
    monad.flatMap(str)(s => monad.map(num)(n => (s, n))) 
  
  // 需要声明为M[_]: Monad，把这个隐式参数传递，否则在作用域里找不到
  def combine2[M[_]: Monad](str: M[String])(num: M[Int]): M[(String, Int)] =
    for {
      s <- str     // 编译器翻译成str.flatMap -> 转换成Ma2Monad类型 -> M2Monad#flatMap -> 查找隐式参数
      n <- num
    } yield (s, n)

}

object MonadApp {

  def main(args: Array[String]): Unit = {

    import CombineContainer._

    val list = List(1, 2, 3)
    val strList = List("A", "B", "C")

    println(combine(strList)(list))
    println("<----- for comprehension ------>")
    println(combine2(strList)(list))

    val nOpt = Option(100)
    val sOpt = Option("Zzz")

    println(combine(sOpt)(nOpt))
    println("<----- for comprehension ------>")
    println(combine2(sOpt)(nOpt))
  }
}