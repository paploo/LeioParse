package net.paploo.leioparse.app

import net.paploo.leioparse.app.App.Result

import scala.concurrent.Future

class App(config: AppConfig) extends (AppArgs => Future[Result]) {

  override def apply(args: AppArgs): Future[Result] = ???

}

object App {

  def apply(config: AppConfig): App = new App(config)

  case class Result(value: Any) //TODO: Figure out a meaningful result.

}
