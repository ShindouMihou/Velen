# 🌯 Velen CLI
Velen now offers a little CLI function to create commands, categories and other future implementations more
easily. The CLI is written with Rust to offer high performance, low-size and memory safety.

> The CLI does not support producing commands with options, but it supports most fields for commands
> such as cooldowns, usages and so forth. Due to the nature of options, it is a bit hard to write a CLI that
> can create them.


## 🍜 Installation & Building
To install the CLI, all you need is to download the `Velen.exe` file from the `releases` folder and
store it somewhere such as your `Documents` folder and add it to your path.

If you can't run the `.exe` file, you can build the version for your own operating system (since Rust compiler tends to
build the source depending on what operating system you are using), to build the source, simply follow the steps:
1. `git clone https://github.com/ShindouMihou/Velen`
2. `cd Velen/cli-core` (or whatever directory to cli-core folder)
3. `cargo build --release`
4. Copy the file from `/target/release/Velen(.exe or .sh or whatever)`.

To run the CLI, all you need to do is (for EXE), open a terminal and run the line:
```terminal
.\velen.exe
```

Additionally, for anyone using Powershell, you can alias the executable via:
```
new-alias velen /path/to/velen.exe
```

## 🌵 Command Creation
You can easily create a command with the Velen CLI via the:
```
velen make command <NAME> --model <MODEL (e.g. SLASH, HYBRID or MESSAGE)> --handler <HANDLER>
```

There are also some more extra arguments for the `make command` which you can find via the `velen make command --help` command
with the descriptions of the arguments also visible.

A short example of creating a command via the CLI is:
```
velen make command ping --model hybrid --handler hybrid.ping --desc "Sends a PONG or PING" --afterwares hybrid.log_command --shortcuts pong pang
```

This will create a command that has the following:
- A command with the name of `ping`
- A command with two aliases: `pong` and `pang`
- A command with the desc: `Sends a PONG or PING`.
- A command with the afterware: `hybrid.log_command`.
- A command with the handler: `hybrid.ping`

## 🎂 Category Creation
You can also create categories in a similar fashion via the:
```
velen make category <NAME>
```

There are also some more extra arguments for the `make category` which you can find via the `velen make category --help` command
with the descriptions of the arguments also visible.

A short example of creating a command via the CLI is:
```
velen make category Fun --desc "The commands that are for entertainment" --afterwares slash.log_command hybrid.log_command message.log_command
```

This will create a category that has the following:
- A category with the name `Fun`
- A category with three afterwares: `slash.log_command`, `hybrid.log_command` and `message.log_command`.
- A category with the description: `The commands that are for entertainment`
