package fp.things.typelevel

object TypeParameter {

  def mapOptions(map: Map[String, Option[_]]): Unit = {

    map.foreach { case ((k, v)) => 
      println(s"key = ${k}, value = ${v}")
    }
  }

  // Case: type constraints
  // trait Map[K, +V] 
  // sealed abstract classList[+A]
  trait ConstraintTrait[X, Container <: List[Any]]

  // Case: type parameter
  // Container[_] which means ParameterTrait expects a type constructor that accepts one type
  // Container[Int] is already a "constructed" type
  // A 不会解释为Container的类型参数（是参数的参数），但是可以表达类型的约束，如下面必须是AnyVal的子类
  trait ParameterTrait[X, Container[A <: AnyVal]]

  def main(args: Array[String]): Unit = {

    val nt = new ConstraintTrait[String, List[Int]] {}

    // OK
    val pt = new ParameterTrait[String, List] {}
    
    // Error, List[Int] takes no type parameters, expected: one
    //val ept = new ParameterTrait[String, List[Int]] {}
  }
}

// Refs:
// https://users.scala-lang.org/t/solved-service-does-not-take-type-parameters/6348/3
// https://groups.google.com/g/scala-user/c/QOvgJb6C0Ho