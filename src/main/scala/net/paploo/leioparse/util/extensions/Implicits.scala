package net.paploo.leioparse.util.extensions

trait Implicits
  extends ExtendedAny.Implicits
  with ExtendedFuture.Implicits
  with CatsSeqImplementation.Implicits

object Implicits extends Implicits
