# 🎁 Hybrid Command Examples
These are new examples of Hybrid Commands in Velen using the new file constructor system, all of these
attempts to utilize all possible methods for hybrid commands.

Please note that we are using the following handlers for this:
```java
public class Test {

    public static void main(String[] a) {
        Velen velen = Velen.ofDefaults();

        velen.addHandler("hybrid.hi", (event, responder, user, args) -> {
            if (args.withName("user").isPresent()) {
                responder.setContent("Hello " + args.withName("user").get().requestUser().get().join().getName());
            } else {
                responder.setContent("Hello " + user.getName());
            }

            responder.respond();
        });

        velen.addHandler("hybrid.number", (event, responder, user, args) -> {
            if (args.withName("number").isPresent()) {
                responder.setContent("I say number " + args.withName("number").get().asInteger().get()).respond();
            }
        });

        velen.addHandler("hybrid.number", (event, responder, user, args) -> {
            if (args.withName("number").isPresent()) {
                responder.setContent("I say number " + args.withName("number").get().asInteger().get()).respond();
            }
        });

        velen.addHandler("hybrid.ping", (event, responder, user, args) -> responder.setContent("Pong!").respond());
        velen.addHandler("hybrid.say", (event, responder, user, args) -> {
            if (args.withName("text").isPresent()) {
                responder.setContent(args.withName("text").get().asString().get()).respond();
            }
        });

        velen.addHandler("hybrid.regex", (event, responder, user, args) -> {
            if (args.withName("url").isPresent()) {
                responder.setContent(args.withName("url").get().asString().get()).respond();
            } else {
                // This actually only works on message commands.
                responder.setContent("The URL was rejected!").respond();
            }
        });

        velen.addHandler("hybrid.scream", (event, responder, user, args) -> {
            if (args.withName("scream").isPresent()) {
                String scream = args.withName("scream").get().asString().get();
                responder.setContent(scream).respond();
            }
        });


        velen.loadFrom("examples");
        DiscordApi api = new DiscordApiBuilder().setToken(System.getenv("token"))
                .addListener(velen)
                .setAllIntents()
                .login()
                .join();

        new SlashCommandChecker(api, SlashCommandCheckerMode.NORMAL).run(velen);
        System.out.println("You can now run the bot!");
    }
}
```