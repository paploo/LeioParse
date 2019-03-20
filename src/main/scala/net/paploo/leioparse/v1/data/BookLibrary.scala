package net.paploo.leioparse.v1.data

case class BookLibrary(toSeq: Seq[Book]) {

  def findById(id: Book.Id): Option[Book] = toSeq.find(_.id == id)

  def findByTitle(title: Book.Title): Option[Book] = toSeq.find(_.title == title)

}