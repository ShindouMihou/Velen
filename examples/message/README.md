# 🎁 Message Command Examples
These are new examples of Hybrid Commands in Velen using the new file constructor system, all of these
attempts to utilize all possible methods for message commands.

Please note that we are using the following handlers for this:
```java
public class Test {

    public static void main(String[] a) {
        Velen velen = Velen.ofDefaults();

        velen.addHandler("message.hi", (event, message, user, args, options) -> {
            Optional<CompletableFuture<User>> userFuture = options.requestUserFromNamedArgument("user");
            if (userFuture.isPresent()) {
                userFuture.get().thenAccept(target -> message.reply("Hello " + target.getName()));
            } else {
                message.reply("Hello " + user.getName());
            }
        });

        velen.addHandler("message.number", (event, message, user, args, options) -> options.getIntegerWithName("number")
                .ifPresent(integer -> message.reply("I say number " + integer)));

        velen.addHandler("message.ping", (event, message, user, args, options) -> options.withName("response")
                .ifPresent(message::reply));

        velen.addHandler("message.say", (event, message, user, args, options) -> options.withName("text")
                .ifPresent(message::reply));

        velen.addHandler("message.scream", (event, message, user, args, options) -> options.withName("scream")
                .ifPresent(message::reply));

        velen.addHandler("message.regex", (event, message, user, args, options) -> {
            if (options.withName("url").isPresent()) {
                message.reply(options.withName("url").get());
            } else {
                message.reply("The URL was rejected");
            }
        });

        velen.loadFrom("examples/message");
        DiscordApi api = new DiscordApiBuilder().setToken(System.getenv("token"))
                .addListener(velen)
                .setAllIntents()
                .login()
                .join();

        System.out.println("You can now run the bot!");
    }

}
```