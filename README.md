# pixels-cli

Command Line Image Editor

## Usage

Using leiningen:
```
lein run
```

Running the included jar directly:
```
java -jar target/pixels-cli-0.1.0-standalone.jar
```

```
Tiny Interactive Graphical Editor
Enter the commands, one command per line:
>I 5 5
>S
Current image:
OOOOO
OOOOO
OOOOO
OOOOO
OOOOO
>V 1 1 5 X
>S
Current image:
XOOOO
XOOOO
XOOOO
XOOOO
XOOOO
>H 1 5 1 Z
>S
Current image:
ZZZZZ
XOOOO
XOOOO
XOOOO
XOOOO
>F 2 2 K
>S
Current image:
ZZZZZ
XKKKK
XKKKK
XKKKK
XKKKK
>X
Terminating session. Bye
```

## License

Copyright © 2015 Oliver Martell

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
