package net.paploo.leioparse.app

class AppArgsParser extends (Array[String] => AppArgs) {
  override def apply(args: Array[String]): AppArgs = ???
}

object AppArgsParser {
  implicit def apply: AppArgsParser = new AppArgsParser
}
