## 🍜 Categories
Velen allows support for creating categories that can contain middlewares, afterwares, and can be fetched through
the API. You can use categories to have all the middlewares and afterwares needed for a group of commands be implemented
without having to place the middleware line on every command that needs it.

All category files must have the file extension: `vecomp` which is short for `Velen Category Component` and should be
unique unless there is another application that uses the `vecomp` file extension.

For this example, we are using the following code for `log_command` middleware.
```java
velen.addHybridMiddleware("log_command", (event, arguments, command, gate) -> {
    System.out.println(event.getUser().getId() + " executed " + command.getName());
    return gate.allow();
});

velen.addMessageMiddleware("log_command", (event, command, options, gate) -> {
    System.out.println(event.getMessageAuthor().getId() + " executed " + command.getName());
    return gate.allow();
});

velen.addSlashMiddleware("log_command", (event, command, gate) -> {
    System.out.println(event.getSlashCommandInteraction().getUser().getId() + " executed " + command.getName());
    return gate.allow();
});
```