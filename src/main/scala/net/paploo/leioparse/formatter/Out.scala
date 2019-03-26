package net.paploo.leioparse.formatter

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter, PrintWriter}

import net.paploo.leioparse.util.extensions.Implicits._

trait Out {
  def toOutputStream: OutputStream
  def stream: OutStream
  def writer: OutWriter
}

trait OutStream extends Out {
  def toOutputStream: OutputStream
  def map(f: OutputStream => Unit): OutStream
}

trait OutWriter extends Out {
  def map(f: PrintWriter => Unit): OutWriter
}

object Out {

  def apply(out: OutputStream): Out = new IOStreamWriter(out)

  private[this] class IOStreamWriter(val toOutputStream: OutputStream) extends Out {

    // This is the same set of events as just `new PrintWriter(out)` except that we use UTF-8 instead of the default encoding.
    //private[this] lazy val printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(toOutputStream, "UTF-8")))
    private[this] lazy val printWriter = new PrintWriter(new OutputStreamWriter(toOutputStream, "UTF-8"))

    override val stream: OutStream = new OutStream {
      override def toOutputStream: OutputStream = IOStreamWriter.this.toOutputStream
      override def map(f: OutputStream => Unit): OutStream = this.tap(_ => f(toOutputStream))
      override def stream: OutStream = IOStreamWriter.this.stream
      override def writer: OutWriter = IOStreamWriter.this.writer
    }

    override def writer: OutWriter = new OutWriter {
      override def toOutputStream: OutputStream = IOStreamWriter.this.toOutputStream
      override def map(f: PrintWriter => Unit): OutWriter = this.tap(_ => f(printWriter).tap(_ => printWriter.flush())) //Ensure flushing so that.
      override def stream: OutStream = IOStreamWriter.this.stream
      override def writer: OutWriter = IOStreamWriter.this.writer
    }
  }

}
