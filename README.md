# LeioParse

J.C. Reinecke
Mar 2019

Quick little project to parse the reading logs from my reading log app, Leio.

## Quick Start

First, add your books to the book library configuration file, so that statistics
can be calculated for them.

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
