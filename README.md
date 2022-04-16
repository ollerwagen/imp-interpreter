# imp-interpreter

Interpreter for the IMP programming language (FMFP thingy)

## IMP 2.0

This version implements the same features, I just got tired of the disgusting parser for the first version. The error messages are still questionable and the parser is still objectively terrible, but less so than before.

## Compilation

In the folder `imp`, Compile the IMP Interpreter with `javac imp/*.java`
The Interpreter is already compiled though, so this step is not necessary unless you change some code because you think you're better than me, motherfucker.

## Error Messages

I use error productions (allowing a larger grammar that contains some faulty expressions) to produce better error messages. For example, the parser recognizes binary arithmetic expressions that are not surrounded by parentheses to then produce an error message that clarifies that IMP enforces parentheses around such expressions.

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

If multiple instructions are added after one another, they need to be separated by semicola. This -- as specified by the language's grammar discussed in the lecture -- even holds for `if` and `while` statements. The last instruction in a sequence of instructions can (but doesn't have to be) terminated by a semicolon. As specified in the lecture, we allow for some leeway compared to the formal grammar of the language because it makes our lives easier.

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
