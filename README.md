# LeioParse

J.C. Reinecke\
Oct 2018

## Quick Start

Quick little project to parse the reading logs from my reading log app, Leio.

To generate the output needed for a plotting program (e.g. DataGraph), run
from sbt, supplying the *directory* that *contains* the *leio_data.csv* and
*leio_sessions.csv* files.

Inside the sbt shell, simply do
```
run <path_to_data_dir>
```

From the bash shell, you have to quote the command:
```
$ sbt "run <path_to_data_dir>"
```

## License

TODO: Insert 3-clause BSD here.

## Future Plans

## CSV Library
1. Wrap CSV reading library in a call where my streams don't fall out from under me (not worried about loading into RAM!)
2. Build better library for mapping a CSV Map to a case class.

## BookLibrary
1. Rather than have a the row parser also handle overlay, instead make the book library build by composing each.

## Book Stats
1. Better structure around books as a focus and having a sequence of sessions and a stats object.
2. When outputting a book, include the overall stats like first/last read date, first/last read page, total pages,
   avg pages/day, cumulative session time, avg session time per page, and word versions too.
3. In prep for Kindle, parse pages/locs/etc differently?
