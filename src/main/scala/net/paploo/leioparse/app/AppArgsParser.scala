package net.paploo.leioparse.app

trait AppArgsParser extends (Array[String] => AppArgs)

object AppArgsParser {
  val apply: AppArgsParser = new AppArgsParser {
    override def apply(v1: Array[String]): AppArgs = ???
  }
}
