package net.paploo.leioparse.parser

trait Row[Column] {
  def toMap: Map[Column, String]

  @inline def apply(column: Column): String = toMap(column)
  @inline def get(column: Column): Option[String] = toMap.get(column)

  @inline def convert[B](column: Column)(f: String => B): B = f(apply(column))
}

object Row {

  def fromRaw[R <: Row[Column], Column](rowCreator: Map[Column, String] => R, columnMapper: String => Column)(map: Map[String, String]): R =
    rowCreator(map.filter { case (_,v) => !v.isEmpty }.map { case (k,v) => columnMapper(k) -> v })

}