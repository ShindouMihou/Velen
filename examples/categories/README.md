## 🍜 Categories
Velen allows support for creating categories that can contain middlewares, afterwares, and can be fetched through
the API. You can use categories to have all the middlewares and afterwares needed for a group of commands be implemented
without having to place the middleware line on every command that needs it.

All category files must have the file extension: `vecomp` which is short for `Velen Category Component` and should be
unique unless there is another application that uses the `vecomp` file extension.

> 🔴 It is recommended to append the prefix `hybrid.`, `message.` or `slash.` before the afterware or middleware as
> Velen stores them without discrimination, that means any non-unique names will end up overwriting each other.
> This is done to reduce code used in the library.

For this example, we are using the following code for `log_command` middleware.

> 🟡 It isn't recommended to use middlewares for logging command execution, but rather use afterwares
> but since we don't have a simpler example to use, we are just using it for both.

```java
velen.addHybridMiddleware("hybrid.log_command", (event, arguments, command, gate) -> {
    System.out.println(event.getUser().getId() + " executed " + command.getName());
    return gate.allow();
});

velen.addMessageMiddleware("message.log_command", (event, command, options, gate) -> {
    System.out.println(event.getMessageAuthor().getId() + " executed " + command.getName());
    return gate.allow();
});

velen.addSlashMiddleware("slash.log_command", (event, command, gate) -> {
    System.out.println(event.getSlashCommandInteraction().getUser().getId() + " executed " + command.getName());
    return gate.allow();
});
```

We are also using the following code for `log_command` afterware:
```java
velen.addHybridAfterware("hybrid.log_command",
                (event, arguments, command) -> System.out.println(event.getUser().getId() + " used the command : " + command.getName()));

velen.addMessageAfterware("message.log_command",
                (event, command, options) -> System.out.println(event.getMessageAuthor().getId() + " used the command : " + command.getName()));

velen.addSlashAfterware("slash.log_command",
                (event, command) -> System.out.println(event.getSlashCommandInteraction().getUser().getId() +
                        " used the command : " + command.getName()));
```