
Hello World Example
===================

*Build the DKO project first, or this won't work!  `../../lib/dko.jar` should exist.* 

The SQLite3 file `helloworld.sqlite3` is the database in this example.  If you have 
[Sqlite Database Browser](http://sourceforge.net/projects/sqlitebrowser/) installed
you can view it with:

```bash
$ sqlitebrowser helloworld.sqlite3
```

To extract the database schema, run:

```bash
$ ant extract-schemas
```

Then to build the project, run:

```bash
$ ant
```

Then to run the example, run:

```bash
$ ./helloworld.sh
Where is everyone from?
[Person id:1 name:Charles] is from [Place id:1 city:Houston state:Texas]
```

That's it!

