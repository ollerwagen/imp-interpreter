# imp-interpreter

Interpreter for the IMP programming language (FMFP thingy)

## Compilation

Compile the IMP Interpreter with `javac imp/imp/*.java`
The Interpreter is already compiled though, so this step is not necessary unless you change some code because you think you're better than me, motherfucker.

## Execution

So far, the IMP Interpreter only supports REPL. Run the interpreter by: `cd imp; java imp.Imp`. I don't know why you have to switch directory, but otherwise it doesn't work and I'm too lazy to figure out why.

If you specify `-multiline` when executing the interpreter, you can enter code until you end a line with a `!` character.

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
