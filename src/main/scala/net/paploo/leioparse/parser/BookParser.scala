package net.paploo.leioparse.parser

import net.paploo.leioparse.data.{Book, BookLibrary}

object BookParser {

  def mutable(initialLibrary: BookLibrary = BookLibrary.empty): String => Book = new Function[String, Book]{

    var bookLibrary: BookLibrary = initialLibrary

    override def apply(bookName: String): Book = {
      val (newLib, book) = bookLibrary.getOrAdd(bookName)
      bookLibrary = newLib
      book
    }

  }

}

