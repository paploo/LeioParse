package net.paploo.leioparse.app

import java.nio.file.{Path, Paths}

case class AppArgs(dataDirPath: Path) {
  val sessionPath: Path = Paths.get(dataDirPath.toString, "leio_sessions.csv")
  val bookPath: Path = Paths.get(dataDirPath.toString, "leio_data.csv")
}
