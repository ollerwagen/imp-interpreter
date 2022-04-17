# imp-interpreter

Interpreter for the IMP programming language (FMFP thingy)

## IMPI 2.0

This version implements the same features, I just got tired of the disgusting parser for the first version. It uses error productions to improve error messages and its parser is slightly less terrible. It also comes with a debugger that executes instructions one step at a time by specifying `-debug` or `-d` in the argument list when executing the program.

IMPI 2.0 can be compiled in the directory `imp2` with `javac imp2/*.java` and executed by `java imp2.Imp`, followed by the program arguments.

## Compilation

In the folder `imp`, Compile the IMP Interpreter with `javac imp/*.java`
The Interpreter is already compiled though, so this step is not necessary unless you change some code because you think you're better than me, motherfucker.

## Execution

So far, the IMP Interpreter only supports REPL. In the folder `imp` run the interpreter by: `java imp.Imp`.

If you specify `-multiline` when executing the interpreter, you can enter code until you end a line with a `!` character.

It is also possible to directly enter arithmetic or boolean expressions in the REPL window which are evaluated and the result printed to the console. Right now, the error messages generated for such expressions are quite lackluster though.

## The Language

I extended the language by an additional command `print` (used the same way as `skip`) in order to print the names and values of all variables.

### Scope

Note that IMP doesn't know scope: Once initialized, a variable only dies when you close the interpreter, and variables defined within sub-blocks (e.g. the body of a loop) can be used outside of that loop as well.

A consequence of this is e.g. the following: `if <some condition> then c := 0 else d := 0 end`. If the condition holds, `c` gets defined, otherwise `d`.

### Semicola

If multiple instructions are added after one another, they need to be separated by semicola. This -- as specified by the language's grammar discussed in the lecture -- even holds for `if` and `while` statements.

## Example

`a := 1; while a < 64 do print; a := ((a*2)+1); end` generates the following output:

    Program State:
      a <- 1
    Program State:
      a <- 3
    Program State:
      a <- 7
    Program State:
      a <- 15
    Program State:
      a <- 31
    Program State:
      a <- 63
