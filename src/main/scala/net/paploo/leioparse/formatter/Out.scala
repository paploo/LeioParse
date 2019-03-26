package net.paploo.leioparse.formatter

import java.io.{OutputStream, OutputStreamWriter, PrintWriter}

import net.paploo.leioparse.util.extensions.Implicits._

trait OutputStreamable {
  def toOutputStream: OutputStream
}

trait Out extends OutputStreamable {
  def streamProjection: OutStream
  def writerProjection: OutWriter
  def map[B](f: PrintWriter => B): Out = this.tap(_.writerProjection.map(f))
}

trait OutStream extends OutputStreamable {
  def map[B](f: OutputStream => B): OutStream
}

trait OutWriter extends OutputStreamable {
  def map[B](f: PrintWriter => B): OutWriter
}

object Out {

  /**
    * Creates a new Out wrapping the given OutputStream.
    *
    * The OutpuStream must be closed outside of this.
    */
  def apply(out: OutputStream): Out = new OutStreamWriter(out)

  private[this] class OutStreamWriter(val toOutputStream: OutputStream) extends Out {

    //PrintWriter.close() and OutputStreamWriter.close() just proxy to what they wrap, so closing them is not needed since we don't manage the OutputStream lifecycle.
    private[this] lazy val printWriter = new PrintWriter(new OutputStreamWriter(toOutputStream, "UTF-8"))

    override val streamProjection: OutStream = new OutStream {
      override val toOutputStream: OutputStream = OutStreamWriter.this.toOutputStream
      override def map[B](f: OutputStream => B): OutStream = this.tap(_ => f(toOutputStream))
    }

    override val writerProjection: OutWriter = new OutWriter {
      override val toOutputStream: OutputStream = OutStreamWriter.this.toOutputStream
      override def map[B](f: PrintWriter => B): OutWriter = this.tap(_ => f(printWriter).tap(_ => printWriter.flush()))
    }

  }

}