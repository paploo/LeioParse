package net.paploo.leioparse.outputterformatter

import java.io.{File, FileOutputStream, OutputStream, OutputStreamWriter, PrintWriter}
import java.nio.file.Path

import scala.concurrent.{ExecutionContext, Future}

/**
  * Wrapper for an OutputStream to provide controlled/safe access to formatters from within the Output monad.
  *
  * The OutputStream lifecycle should be managed outside of the Outputter; see the run companion object methods which
  * safely wrap the lifecycle management for various objects.
  */
class Outputter private(os: OutputStream) extends OutputStream {

  val toOutputStream: OutputStream = this

  lazy val toWriter: PrintWriter = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)

  override def write(b: Int): Unit = os.write(b)

  override def close(): Unit = {/** Do nothing to not leak closing into formatter scope; defer close management to whatever created the OutputStream **/}

}

object Outputter {

  private def apply(os: OutputStream): Outputter = new Outputter(os)

  def runStdout[A](f: Outputter => A)(implicit ec: ExecutionContext): Future[A] = Future(f(Outputter(System.out)))

  def runFile[A](file: File)(f: Outputter => A)(implicit ec: ExecutionContext): Future[A] = Future {
    val fos = new FileOutputStream(file)
    val result = f(Outputter(fos))
    fos.close()
    result
  }

  def runPath[A](path: Path)(f: Outputter => A)(implicit ec: ExecutionContext): Future[A] = runFile(path.toFile)(f)

}