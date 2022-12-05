package fp.things.implics 

object DifferentType {

  def process[A, B](la: List[A], lb: List[B]): List[(A, B)] = {
    for {
      a <- la
      b <- lb
    } yield (a, b)
  }

  def processSameTypeV1[A](la: List[A], lb: List[A]): List[(A, A)] = {
    process(la, lb)
  }

  def processSameTypeV2[A, B](la: List[A], lb: List[B])(implicit ev: =:=[A, B]): List[(A, B)] = {
    process(la, lb)
  }

  // how to differe type ? =!= is not build in

  trait =!=[A, B]
  implicit def diffType[A, B]: =!=[A, B] = null
  // it's a trick!!
  implicit def generate1[A]: A =!= A = null
  implicit def generate2[A]: A =!= A = null

  def processDiffTypeV1[A, B](la: List[A], lb: List[B])(implicit ev: =!=[A, B]): List[(A, B)] = {
    process(la, lb)
  }


  def main(args: Array[String]): Unit = {
    Seq(
      process(List(1, 2, 3), List(4,5,6)),
      processSameTypeV1(List(1, 2, 3), List(4,5,6)),
      processSameTypeV2(List(1, 2, 3), List(4,5,6)),
      processDiffTypeV1(List(1, 2, 3), List('a', 'b', 'c')),
      // processDiffTypeV1(List('1', '2', '3'), List('a', 'b', 'c')), // not work
    ).foreach(println(_))
  }
}

// Ref: https://blog.rockthejvm.com/anti-implicits/