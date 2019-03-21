package net.paploo.leioparse.app

import net.paploo.leioparse.app.App.Result

import scala.concurrent.Future

class App extends (AppArgs => Future[Result]) {

  override def apply(args: AppArgs): Future[Result] = ???

}

object App {

  def apply: App = new App

  case class Result(value: Any) //TODO: Figure out a meaningful result.

}
