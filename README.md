# LeioParse

J.C. Reinecke<br/>
(c) 2019

[![pipeline status](https://gitlab.com/paploo/leioparse/badges/master/pipeline.svg)](https://gitlab.com/paploo/leioparse/commits/master)

* Project Home: https://gitlab.com/paploo/leioparse
* Project Mirror: https://github.com/paploo/LeioParse

A little project for myself to parse the reading logs from the reading log app for iOS, Leio.

## Overview

This program parses the log files exported from Leio (an iOS reading tracker), generates statistics, and outputs
the data in several formats for processing in various systems.

This currently supports dumping to a large JSON structure, as well as outputting CSV in formats that are easy to
parse in spreadsheets for graphical display and analysis.

## Quick Start

Since this is for me, I haven't tried to bundle this as a self-contained app, instead I run it from
[sbt](https://www.scala-sbt.org), the [scala](https://www.scala-lang.org)-centric (and ironically named) *simple build tool*.

To run the program, either start the sbt shell, or invoke with arguments from the shell directly. In its most simple
form, that would be:

Inside the sbt shell, simply do
```
run <path_to_data_dir>
```

From the bash shell, you have to quote the command:
```
$ sbt "run <path_to_data_dir>"
```

use the `--help` switch to get further options, such as formatters, output to file, and use of a book library overlay
that is different than the one I've bundled in (which happens to be mine).

## License

See the LICENSE file.