package net.paploo.leioparse.formatter

import java.io.{BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream, InputStreamReader, OutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import net.paploo.leioparse.data.core.BookReport

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Wrapper for an OutputStream to provide controlled/safe access to formatters from within the Output monad.
  *
  * The OutputStream lifecycle should be managed outside of the Outputter; see the runTo companion object methods which
  * safely wrap the lifecycle management for various objects.
  *
  * Note that OutputStream was chosen as the base, since binary modes can be written with the OutputStream, and it
  * can be wrapped in a PrintWriter to do a character stream.
  */
class Outputter private(os: OutputStream) extends OutputStream {

  val toOutputStream: OutputStream = this

  lazy val toWriter: PrintWriter = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true) {
    override def close(): Unit = {/** Do nothing to not leak closing into formatter scope; defer close management to whatever created the OutputStream} **/}
  }

  override def write(b: Int): Unit = os.write(b)

  override def close(): Unit = {/** Do nothing to not leak closing into formatter scope; defer close management to whatever created the OutputStream **/}

}

object Outputter {

  private def apply(os: OutputStream): Outputter = new Outputter(os)

  def runToStdout[A](f: Outputter => A)(implicit ec: ExecutionContext): Future[A] = Future(f(Outputter(System.out)))

  def runToFile[A](file: File)(f: Outputter => A)(implicit ec: ExecutionContext): Future[A] = Future {
    val fos = new FileOutputStream(file)
    val result = f(Outputter(fos))
    fos.close()
    result
  }

  def runToLinesPromise[A](linesPromise: Promise[Seq[String]])(f: Outputter => A)(implicit ec: ExecutionContext):  Future[A] = Future {
    val os: ByteArrayOutputStream = new ByteArrayOutputStream
    val result = f(Outputter(os))

    val reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray), StandardCharsets.UTF_8))
    val lines = reader.lines.toArray[String](size => new Array[String](size)).toSeq
    linesPromise.success(lines)

    os.close()
    result
  }

  def runToLines[A](f: Outputter => A)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    val p = Promise[Seq[String]]()
    runToLinesPromise(p)(f).flatMap(_ => p.future)
  }

  def runToPath[A](path: Path)(f: Outputter => A)(implicit ec: ExecutionContext): Future[A] = runToFile(path.toFile)(f)

  def formatToStdOut[A](formatter: Formatter[A])(implicit ec: ExecutionContext): Seq[BookReport] => Future[A] =
    reports => runToStdout(out => formatter(FormatterEnv(out, reports)))

  def formatToFile[A](file: File)(formatter: Formatter[A])(implicit ec: ExecutionContext): Seq[BookReport] => Future[A] =
    reports => runToFile(file)(out => formatter(FormatterEnv(out, reports)))

  def formatToPath[A](path: Path)(formatter: Formatter[A])(implicit ec: ExecutionContext): Seq[BookReport] => Future[A] =
    formatToFile(path.toFile)(formatter)

  def formatToLinesPromise[A](linesPromise: Promise[Seq[String]])(formatter: Formatter[A])(implicit ec: ExecutionContext): Seq[BookReport] => Future[A] =
    reports => runToLinesPromise(linesPromise)(out => formatter(FormatterEnv(out, reports)))

  def formatToLines[A](formatter: Formatter[A])(implicit ec: ExecutionContext): Seq[BookReport] => Future[Seq[String]] =
    reports => runToLines(out => formatter(FormatterEnv(out, reports)))

}