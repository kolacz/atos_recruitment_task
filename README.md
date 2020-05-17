# Atos Recruitment Task

The task is to develop a simple library manager application. The formal description can be found at `./docs/Zadanie ATOS Java_biblioteka.docx`.

## Building

Use [sbt](https://www.scala-sbt.org) to build the project from the root directory. Scala version: `2.13.2`, sbt version: `1.3.10`.

```bash
sbt compile
```

## Usage

Run the app with sbt.

The app reads requests from the `stdin`.
Each request is a one-line [JSON](https://www.json.org/json-en.html) string with the `actionName` as it's only key. Action arguments are within its value.

```
{"actionName": {"argName1": "val1", "argName2": "val2"}}
```

Actions are:
- `"addBook"`
  * arguments: `"title": String, "year": Int, "author": String`,
- `"removeBook"`
  * arguments: `"id": Long`,
- `"listBooks"`
  * arguments: `None`,
- `"searchBook"`
  * arguments: `"title": String, "year": Int, "author": String (each is optional)`,
- `"lendBook"`
  * arguments: `"id": Long`,
- `"bookDetails"`
  * arguments: `"id": Long`.

Actions are executed after pressing `return` key.
Instantly, the response is sent back to the `stdout`. To gracefully stop the app, enter an empty line.

### Example:
```bash
sbt run
...
{"addBook": {"title": "Ostry Cień Mgły", "year": 2020, "author": "MC President"}}
{"OK": {"message": "(id=1, title=Ostry Cień Mgły, year=2020, author=MC President, isAvailable=true, lentBy=nobody) has been added"}}
{"lendBook": {"id": 1, "userName": "foo"}}
{"OK": {"message": "User foo has lent the book with id=1"}}
{"removeBook": {"id": 1}}
{"ERROR": {"message": "Cannot delete the book with id=1, because it is lent"}}

[success] Total time: 1153 s (19:13), completed May 17, 2020, 10:18:58 PM
```


## Testing

To test the application use sbt:

```bash
sbt test
```
