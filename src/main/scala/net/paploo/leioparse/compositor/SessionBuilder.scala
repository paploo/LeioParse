package net.paploo.leioparse.compositor

import net.paploo.leioparse.data.core.Session
import net.paploo.leioparse.data.leiofile.LeioSession

/**
  * Produces a converter of LeioSessions to sessions, using a
  */
trait SessionBuilder extends (LeioSession => Session)

object SessionBuilder {

  def apply: SessionBuilder = new StandardSessionBuilder

  private class StandardSessionBuilder extends SessionBuilder {

    override def apply(leioSession: LeioSession): Session = Session(
      bookTitle = leioSession.bookTitle,
      startDate = leioSession.startedOn,
      duration = leioSession.duration,
      startLocation = leioSession.firstPage,
      endLocation = leioSession.lastPage
    )

  }

}
