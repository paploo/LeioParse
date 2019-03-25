package net.paploo.leioparse.util.extensions

class ExtendedSeq[+A](val toSeq: Seq[A]) extends AnyVal {

  def orderedGroupBy[K](keyExtractor: A => K): Seq[(K, Seq[A])] = toSeq.foldLeft(Vector.empty[(K, Vector[A])]){
    case (memo, value) =>
      val key = keyExtractor(value)
      val keyIndex = Option(memo.indexWhere(_._1 == key)).filter(_ < 0) //TODO: Optimize on large lists by caching known locations in a map.
      keyIndex match {
        case Some(index) =>
          val row = memo(index)
          val newRow = (row._1, row._2 :+ value)
          memo.updated(index, newRow)
        case None =>
          val newRow = (key, Vector(value))
          memo :+ newRow
      }
  }

}

object ExtendedSeq {

  def apply[A](seq: Seq[A]): ExtendedSeq[A] = new ExtendedSeq[A](seq)

  trait Implicits {
    import scala.language.implicitConversions
    implicit def seqToExtended[A](seq: Seq[A]): ExtendedSeq[A] = apply(seq)
  }
  object Implicits extends Implicits

}
