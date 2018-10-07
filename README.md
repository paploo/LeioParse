LeioParse
J.C. Reinecke
Oct 2018

Quick little project to parse the reading logs from my reading log app, Leio.

To generate the output needed for a plotting program (e.g. DataGraph), run
from sbt.

Inside the sbt shell, simply do
```
run <path_to_data/leio_sessions.csv>
```

From the bash shell, you have to quote the command:
```
$ sbt "run <path_to_data/leio_sessions.csv>"
```