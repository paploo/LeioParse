package net.paploo.leioparse.compositor

import net.paploo.leioparse.data.core.Session
import net.paploo.leioparse.data.leiofile.LeioSession

/**
  * Produces a converter of LeioSessions to sessions, using a
  */
trait SessionCompositor extends (LeioSession => Session)

object SessionCompositor {

  def apply: StandardSessionCompositor = new StandardSessionCompositor

  private class StandardSessionCompositor extends SessionCompositor {

    override def apply(leioSession: LeioSession): Session = Session(
      bookTitle = leioSession.bookTitle,
      startDate = leioSession.startedOn,
      duration = leioSession.duration,
      startLocation = leioSession.firstPage,
      endLocation = leioSession.lastPage
    )

  }

}
