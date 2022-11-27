# Scala Hard Part
记录一些在实践Scala过程中，难以理解的点。大部分可能和`FP`相关，但也不全是。

如果把`OOP`比作是**道**，那么`FP`说成是**魔**一点不夸张。这里有两层意思:
- 它太性感了，会让部分人着**魔**
- 具备一些**魔法**，很多源自`category theory`

## type constructor
这个是一个语法概念，我们常理解的构造函数都是构造出常规实例的，例如`val person = Person("Bob", 33)`；不曾想类型本身也可以构造，在写库或者读一些开源库的时候遇到这个问题的概率比较大。

详细理解，[参见](./type-and-constructor.md)

## Monad
在容器之上抽象，这里`M[_]`就是一个容器。具备两个能力，就属于`Monad`:
- pure， 把基础类型放入容器，也可以理解为包裹起来
- flatMap，把包裹`M[A]`转换成`M[B]`，本质是封装一个操作序列：`提取` -> `转换` -> `再包裹`
- map，这个可以用上面两个操作实现

详细理解，[参见](./monad-basic.md)

## Functor
这可能是最容易理解的一个，毕竟`map`太常用了
- map，只需要支持这个操作就可以

详细实践，[参见](./src/main/scala/fp/things/app/FunctorDemo.scala)

## Free Monad
这个抽象在`Monad`之上，它能解决软件设计中逻辑和执行的分离，不好理解？建议了解一下[ZIO](https://zio.dev/reference/)。
- 对测试过程极度友好，因为执行环境可以随意切换
- ATD, algebra data type

详细实践，参见[DatabaseOps](./src/main/scala/fp/things/app/DatabaseOps.scala)

## Tagless Final
这个概念挺有争议的，也比较火；只能记录一点浅薄的理解，[参见](./tagless-and-final.md)


# 执行
```
> sbt

sbt:fp-things> runMain fp.things.app.MonadApp

sbt:fp-things> runMain fp.things.app.FreeApp
```

# 引用
- [Monad](https://blog.rockthejvm.com/monads/)
- [Free Monad](https://blog.rockthejvm.com/free-monad/)
- [TODO...](https://blog.rockthejvm.com/monads-are-monoids-in-the-category-of-endofunctors/)