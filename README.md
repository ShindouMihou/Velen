# ✨ Velen ![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/ShindouMihou/Velen?label=version&style=flat-square) ![Discord](https://img.shields.io/discord/807084089013174272?color=blue&label=Discord&style=flat-square) ![GitHub commit activity](https://img.shields.io/github/commit-activity/m/ShindouMihou/Velen?color=red&style=flat-square) ![GitHub last commit](https://img.shields.io/github/last-commit/ShindouMihou/Velen?color=orange&style=flat-square)
Velen is a command framework (or library) that is created mainly for Javacord with the aim to make everything more easier
and faster to create, for example, slash commands, hybrid commands, cooldowns (rate-limiters), per-server prefixes, pagination and more!

For a more organized look-through at Velen, please check our [GitHub Wiki](https://github.com/ShindouMihou/Velen/wiki) instead
where everything is more organized and easier to read.

## 📦 How many components does Velen have?
Velen has several components: `VelenRateLimiter`, `VelenCommand`, `VelenPrefixManager`, `VelenBlacklist` and the main `Velen` component
which is the core. Each of the components have their own uses and is decently flexible to use for many situations.

## ✔️ Ping-Pong Example
A very simple of a ping-pong command in Velen is:
```java
Velen velen = Velen.builder().setDefaultPrefix("v.").build();
VelenCommand.of("ping", velen, (event, message, user, args) -> message.reply("Pong!")).attach();

DiscordApi api = new DiscordApiBuilder().setToken(token)
                 .addListener(velen).login().join();
```

## ❤️ Installation
You can easily install Velen through Maven Central by adding these entries onto your `build.gradle` or `pom.xml`:

### Maven
```xml
<dependency>
  <groupId>pw.mihou</groupId>
  <artifactId>Velen</artifactId>
  <version>1.0.6</version>
</dependency>
```

### Gradle
```gradle
implementation 'pw.mihou:Velen:1.0.6'
```

## ⛰️ Velen is used by
- [Amelia](https://github.com/ManaNet/Amelia): A Discord bot that is dedicated to a webnovel site called ScribbleHub.
- More to be added, feel free to create an issue if you want to add yours here!

## ⌛ Velen Rate Limiter
The cooldown handler for Velen, this is an extremely simple implementation and doesn't have much settings other than
being able to change the duration of the cooldown, the message of the cooldown (which is done when building Velen). You
can change the cooldown of Velen and the message by modifying them during build, for example:
```java
Velen velen = Velen.builder()
                .setDefaultCooldownTime(Duration.ofSeconds(10))
                .setRatelimitedMessage((remainingSeconds, user, channel, command) -> 
                    "You are currently rate-limited for " + remainingSeconds + " seconds.");
```

## ⚙️ Velen Prefix Manager
This component is in charge of providing prefixes to all the servers whether it'd be a per-server prefix or a single 
prefix for all servers, by default, all servers uses the prefix `v.` but that is easily changeable through the builder.
```java
Velen velen = Velen.builder().setDefaultPrefix("v.");
```

If you want to use per-server prefixes, you need to have a database where you store the prefixes per-server, for example,
Redis or MongoDB then Velen will handle the storing of the prefixes locally, an example is:
```java
Velen velen = Velen.builder().setPrefixManager(new VelenPrefixManager("v.", 
                        key -> MongoDB.collection("servers", "prefixes")
                               .find(Filters.eq("server", key)).first()
                                .getString("prefix")));
```

The example above uses MongoDB (with a helper class), the flow of the prefix manager would then become like this:
`Check if prefix already exists on local application memory` -> `if not, fetch through MongoDB`

The loader is also not just limited to external databases, in fact, you can even use a `HashMap<Long, String>` and
store stuff there but database is recommended for persistence.

## ⚔️ Velen Blacklist
Velen also supports another simple feature that will assist you in blacklisting specific users from using any command of
your bot with `VelenBlacklist` which supports both persistent and non-persistent blacklists *(persistent requires a database).*

To get started, first, you have to create a blacklist instance and integrate it with Velen, an example usage would be 
(non-persistent blacklist):
```java
Velen velen = Velen.builder().setBlacklist(new VelenBlacklist()).build();
```

If you want to blacklist a user, all you have to do is simply:
```java
velen.getBlacklist().add(userId);
```

To remove, all you have to do is:
```java
velen.getBlacklist().remove(userId);
```

The two methods above will only modify the internal blacklist and won't touch your database which means, if you are using
a persistent database, additional means are needed. For example, if you are making changes onto your database through a method 
like add, you have to use either methods as well to apply the changes to internal list.

You can also opt to refreshing it which uses the loader you set when building the blacklist, the method to refresh
the user is simply:
```java
velen.getBlacklist().refresh(userId);
```

**BUT** the method above as I mentioned, requires a loader which you can quickly build when constructing the blacklist, 
for example (MongoDB with a Helper Class):
```java
new VelenBlacklist(aLong -> MongoDB.collection("blacklists", "someBot").find(Filters.eq("userId", aLong)).first() != null);
```

The reason why we are using `!= null` on the above is because the blacklist expects a `boolean` as a return, and so, the database
is expected to be something similar like: `If the user is on the database, then the user is blacklisted otherwise not`.

**We recommend placing `null` or not adding `setBlacklist()` at all if you are not using the blacklist
since a null blacklist is a signal to the application that we are not using a blacklist (to stop the application from heading
to the blacklist and getting the value and instead assume no one is blacklisted).**

## ✔️ Velen Component
The Velen component is, by far, the simplest to make with the exception of `PrefixManager` and `Ratelimiter` with
an example of creating the component being:
```java
Velen velen = Velen.builder()
  // Sets the default cooldown time of the command if no cooldown is specified.
  .setDefaultCooldownTime(Duration.ofSeconds(10))

  // Sets the default prefix that Velen will use (this will be overriden by Prefix Manager).
  .setDefaultPrefix("v.")

  // Sets the message to be sent once every ratelimit cycle if the user is rate-limited.
  .setRatelimitedMessage((remainingSeconds, user, channel, command) -> "You are currently rate-limited for " + remainingSeconds + " seconds.")

  // Set the message to be sent if the user doesn't have all the permissions needed to use the command. (default)
  .setNoPermissionMessage((permission, user, channel, command) -> "You need the permission(s): " + permission.stream()
    .map(Enum::name).collect(Collectors.joining(",")) + " to use this command!")

  // Set the message to be sent if the user doesn't have the proper roles to use the command. (default)
  .setNoRoleMessage((roles, user, channel, command) -> "You need the role(s): " + roles + " to use this command!")

  // Sets the prefix manager which is a more advanced default prefix which allows you to fetch
  // prefixes from a database for per-server prefixes, example below.
  // Key is equals to the server id, you should only use this if you are using a database to store
  // per-server prefixes.
  .setPrefixManager(new VelenPrefixManager("v.", key -> prefixes.get(key)))
  .build();
```

You can also use the default settings of Velen which can be retrieved through the one-liner method:
```java
Velen velen = Velen.ofDefaults();
```

After building the Velen instance, you can then attach it to your DiscordApi instance through the following:
```java
DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntents()
                .addListener(velen)
                .login()
                .join();
```

## 🗨️ Velen Message Component
This is the part of Velen where you can customize the no permission, no role and rate-limited message as seen on the example
below. To modify the `no permission` message, you can simply create an `VelenPermissionMessage` which would give you the following
parameters: `(List<PermissionType> permission, User user, TextChannel channel, String command)` which you can easily turn into a 
message like this:
```java
Velen.builder().setNoPermissionMessage((permission, user, channel, command) -> "You need the permission(s): " + permission.stream()
                     .map(Enum::name).collect(Collectors.joining(",")) + " to use this command!");
```

An `VelenRoleMessage` which is used for no role messages gives you the parameters `(String roles, User user, TextChannel channel, String command)`
which is nearly identical to the `VelenPermissionMessage` with the exception of the roles being in String format (pre-formatted to mention the roles,
they won't ping or mention the users who have the role since they are sent with a MessageBuilder with no mentions allowed), the default message is:
```java
Velen.builder().setNoRoleMessage((roles, user, channel, command) -> "You need the role(s): " + roles + " to use this command!");
```

The `VelenMessage` which was the original first VelenMessage, is used for ratelimit messages which are sent once every ratelimit cycle which means
the bot will only send this message once for each time the user gets ratelimited. The default message is:
```java
Velen.builder().setRatelimitedMessage((remainingSeconds, user, channel, command) -> "You can use this command in **"+remainingSeconds+" seconds**, " +
                                                  "during this period, the bot will not respond to to any invocation of the command: **"+command+"** for the user. " +
                                                  "This message will delete itself when cooldown is over.");
```

## 🧰 Velen Utils
This is a utility class of Velen which you can use as you like to retrieve user mentions, role mentions, channel mentions in their
proper order. Here are all the methods of the utility class:
```java
Collection<Long> : VelenUtils.getOrderedUserMentions(String message);
Collection<Long> : VelenUtils.getOrderedChannelMentions(String message);
Collection<Long> : VelenUtils.getOrderedRoleMentions(String message);

Collection<User> : VelenUtils.getOrderedUserMentions(DiscordApi api, String message);
Collection<Ch..> : VelenUtils.getOrderedChannelMentions(DiscordApi api, String message);
Collection<Role> : VelenUtils.getOrderedRoleMentions(DiscordApi api, String message);

String           : VelenUtils.getCommandSuggestions(Velen velen, String query);
```

## 🔍 Fuzzy Command Suggestion
As part of VelenUtils, the library supports fuzzy searching for a command. This is especially handy for cases like an `help` command
where you have `help [command]` but the user enters in a typo and writes a non-existent command as an argument instead. Fuzzy Command
Suggestion can help you guide the user to the potential command. An example would be:
```java
// Some random commands.
VelenCommand.of("readbackwards", velen, (event, message, user, args1) -> message.reply("hello!")).attach();
VelenCommand.of("command", velen, (event, message, user, args1) -> message.reply("hello!")).attach();
VelenCommand.of("another", velen, (event, message, user, args1) -> message.reply("hello!")).attach();
VelenCommand.of("hello", velen, (event, message, user, args1) -> message.reply("hello!")).attach();

// The help command.
VelenCommand.of("help", velen, (event, message, user, args) -> {
  if (args.length > 0) {
    message.reply("Do you mean: " + VelenUtils.getCommandSuggestion(velen, args[0]));
  } else {
    message.reply("Help command: " + velen.getCommands().stream().map(VelenCommand::getName)
      .collect(Collectors.joining(", ")));
  }
}).attach();
```

The example below would output: `command` if you were to type: `help com` or would output: `Help command: readbackwards, command, 
another, hello, help` if you were to type: `help`.

## 📛 Velen Command
This is the most amusing part of Velen which takes a lot of inspiration from the library it uses. A VelenCommand is
composed of two parts, the metadata and the event handler which is invoked whenever the command is triggered. A simple
example of a Velen Command is:
```java
VelenCommand.of(
    // The name of the command.
    "hi",

    // The velen instance.
    velen,

    // The event handler which will be invoked if the command is triggered.
    (event, message, user, arg) -> event.getChannel().sendMessage("Hello!"))

  // Attach directly to the Velen instance.
  .attach();

// Short form
VelenCommand.of("hi", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hello!")).attach();
```

The example above will trigger if a user says `v.hi` *or whatever the prefix you are using* and will reply with: `Hello!`
while also using the default cooldown. If you want to change the cooldown, simply add `setCooldown(Duration.ofSeconds(someNumber))`
for example (2 second cooldown):
```java
VelenCommand.of("hi", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hello!"))
                .setCooldown(Duration.ofSeconds(2)).attach();
```

If you want to disable cooldown for the command, simply set the duration to zero like below:
```java
VelenCommand.of("hi", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hello!"))
                .setCooldown(Duration.ofSeconds(0)).attach();
```

Locking a certain command to a certain role:
```java
VelenCommand.of("hello", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hi!"))
                .requireRole(852650686872813569L)
                .attach();
```

Locking a certain command to certain permissions:
```java
VelenCommand.of("hello", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hi!"))
                .requirePermission(PermissionType.MANAGE_CHANNELS)
                .attach();
```

Locking a certain command to only a certain user:
```java
VelenCommand.of("hello", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hi!"))
                .requireUser(584322030934032393L)
                .attach();
```

Adding conditions to a command:
```java
VelenCommand.of("say", velen, (event, message, user, args) -> message.reply("Hello World!"))
                .addCondition(event -> event.getMessageContent().contains("Hello"))
                .attach();
```

Adding conditions and condition not met message to a command:
```java
VelenCommand.of("say", velen, (event, message, user, args) -> message.reply("Hello World!"))
                .addCondition(event -> event.getMessageContent().contains("Hello"))
                .setConditionalMessage((user, channel, command) -> "You do meet the conditions to use this command!")
                .attach();
```

Setting a description and usage of the command:
```java
VelenCommand.of("hello", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hi!"))
                .setDescription("Say Hello to Velen!")
                .setUsage("hello")
                .attach();
```

After setting the description, in case you need to use it for a help command, you can easily get the command
through the following (this applies to any parameter of a Velen Command):
```java
velen.getCommand("hello").ifPresent(command -> System.out.println(command.getDescription));
```

Additionally, you can set a category which you can retrieve in bulk which is especially handy for help commands:
```java
VelenCommand.of("hello", velen, (event, message, user, arg) -> event.getChannel().sendMessage("Hi!"))
                .setDescription("Say Hello to Velen!")
                .setUsage("hello")
                .setCategory("Miscelleanous")
                .attach();
```

To retrieve in bulk when building a help command, you can do:
```java
velen.getCategory("Miscelleanous");
```
The example above will return a `List<VelenCommand>` which you can then use to grab the name of the command,
description, usage and the category itself.

You can also move the handler to its own class, as seen on the example below:

### ExampleEvent.class
The event handler class, it's exactly similar to Javacord.
```java
class ExampleEvent implements VelenEvent {

        @Override
        public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
            event.getMessage().reply("This is inside a class!");
        }

}
```

### Main.class
The main class where you register all your Velen commands.
```java
// This is pretty much the same as the examples above but with the handler on a different class.
VelenCommand.of("hi", velen, new ExampleEvent()).attach();
```

## ☄️ Velen Slash Command and Hybrid Commands
Velen offers a simple implementation that allows you to implement slash commands and hybrid commands as fast
as possible.

### 🗺️ Hybrid commands.
An example implementation of a hybrid command looks like this:
```java
VelenCommand.ofHybrid("velenHybrid", "A velen hybrid command!",
                velen, 
                (event, message, user, args) -> message.reply("Hello!"),
                (event, user, args1, options, firstResponder) -> firstResponder.setContent("Hello").respond())
                .setServerOnly(true, 853911163355922434L)
                .attach();
```

Does it look intimidating, okay! Let's break it down, piece by piece!
```java
VelenCommand.ofHybrid(

                // This is the command name.
                "velenHybrid",
                
                // This is the description (required for slash commands and hybrid).
                "A velen hybrid command!",
                
                // This is the velen instance.
                velen,

                // This is the response for non-slash commands (message commands).
                (event, message, user, args) -> message.reply("Hello!"),

                // This is the response for slash commands (it uses a first responder for first response)
                // but you can use event.respondLater() to tell Discord you will respond later!
                (event, user, args1, options, firstResponder) -> firstResponder.setContent("Hello").respond())

                // This is for setting the slash command to only work on X server.
                // This only works for the slash command, extra implementation will be added for message commands
                // far in the future BUT THIS IS RECOMMENDED FOR DEBUGGING.
                // If you want global slash command, please remove this.
                .setServerOnly(true, 853911163355922434L)
                .attach();
```

**We highly recommend using `setServerOnly(true, serverId)` for testing as Discord immediately updates the command
for that server. After you are done testing, simply remove it and the command will run globally.**

Now, you may have realized that hybrid commands have two separate handlers which is indeed not wrong as the 
implementation of Slash commands and MessageCreateEvent in Javacord are marginally different which is why we require
two different handlers for these two but that doesn't mean you can't place them in a single class!

### HybridEvent.class
```java
public class HybridEvent implements VelenSlashEvent, VelenEvent {

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        message.reply("Hello!");
    }

    @Override
    public void onEvent(SlashCommandInteraction event, User user, VelenArguments args, List<SlashCommandInteractionOption> options, 
    InteractionImmediateResponseBuilder firstResponder) {
        firstResponder.setContent("Hello!").respond();
    }
    
}
```

### Main.class
```java
HybridEvent hybridEvent = new HybridEvent();
VelenCommand.ofHybrid("velenHybrid", "A velen hybrid command!", velen, hybridEvent, hybridEvent)
                .attach();
```

### 🎉 Slash Commands
A simple example of a Velen Slash Command is simply like this:
```java
VelenCommand.ofSlash("velenSlash", "A normal velen slash command.", velen,
                (event, user, args, options, firstResponder) -> {
                    firstResponder.setContent("Hello!").respond();
                }).attach();
```

### 🎀 Options!
You can also add options to the commands (though, message commands will have to handle it differently. Options only works
for slash command events). An example of a slash command with options is:
```java
VelenCommand.ofSlash("velenSlash", "A normal velen slash command.", velen,
                (event, user, args, options, firstResponder) -> {
                    if(args.getStringOptionWithName("text").isPresent()) {
                        firstResponder.setContent(args.getStringOptionWithName("text").get()).respond();
                    } else {
                        firstResponder.setContent("Hello!").respond();
                    }
                }).addOptions(new SlashCommandOptionBuilder()
                        .setName("text")
                        .setDescription("What should I say?")
                        .setType(SlashCommandOptionType.STRING)).attach();
```

`addOptions(...)` can take in multiple options at the same time, which why you can flood it with options
for as much as you want. To build an option, you have to make a `SlashCommandOptionBuilder` which comes from Javacord,
you can simply follow the one above and try out several things with it.

Now, you may have realized as well that there are two types of options: `args` and `options` which both are pretty
much the same except `args` is `options` but wrapped by Velen to make it easier for you to get the values from
the name of the option.

`options` is also a `List<SlashCommandInteractionOptions>` which you have to do a pile load of if checks.

### 💎 Registering the commands!
Slash Commands are a bit different from ordinary commands in which you have to register the commands at least once
(and also for every change you do like changing the name of the slash command, description or options), this is done
so Discord knows the change.

Velen offers a quick method to have everything registered, simply add this line:
```
velen.registerAllSlashCommands(DiscordApi);
```

Please remember to remove it once everything has been registered (and there are no changes to the name, description or
options of the slash commands) as this is quite an expensive task to perform, only re-add it if you have changed a slash
command's **name**, **description** or **options**.

### 📝 Additional notes
Hybrid commands shares the same rate-limit with each other which means if a user uses a hybrid command on slash command,
they will be shown the rate-limited message when they try to use the message command after (vice-versa).

## 📃 Velen Pagination
Velen also offers a helper class that helps you paginate items easily with the `Paginate<T>` class, an example
usage can be seen below:
```java
VelenCommand.of("paginate", "Tests pagination.", velen, (event, message, user, args) -> {

  List<String> testList = Arrays.asList("Test 0", "Test 1", "Test 2", "Test 3", "Test 4");
  
  // Create a Pagination object (you can also save the pagination object to reuse later).
  new Paginate<>(testList).paginate(event, new PaginateEvent<String>() {

    private EmbedBuilder embed(String currentItem, int arrow, int maximum) {
    
      // Remember to always add +1 to arrow for these types of stuff since
      // arrow returns the raw position which means it starts at 0 instead of 1.
      return new EmbedBuilder().setTitle("Item [" + (arrow + 1) + "/" + maximum + "]")
        .setDescription(currentItem).setColor(Color.BLUE);
        
    }

    private EmbedBuilder embed(String currentItem) {
      return new EmbedBuilder().setTitle("Item")
        .setDescription(currentItem).setColor(Color.BLUE);
    }

    @Override
    public MessageBuilder onInit(MessageCreateEvent event, String currentItem,
      int arrow, Paginator<String> paginator) {
      
      // This is the initial message that will be sent on start of pagination.
      return new MessageBuilder().setEmbed(embed(currentItem, arrow, paginator.size()));
      
    }

    @Override
    public void onPaginate(MessageCreateEvent event, Message paginateMessage, String currentItem,
      int arrow, Paginator<String> paginator) {
      
      // This is what will be executed when you paginate next or backwards.
      paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
      
    }

    @Override
    public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
    
      // This is sent when the paginator has no items.
      return new MessageBuilder().setContent("There are currently no items!");
      
    }

    @Override
    public void onSelect(MessageCreateEvent event, Message paginateMessage, String itemSelected,
      int arrow, Paginator<String> paginator) {
      
      // Similar to onPaginate except this is sent whenever the user
      // has selected a page that they want.
      paginateMessage.edit(embed(itemSelected));
      
    }
    
  }, Duration.ofMinutes(5));
}).attach();
```

Similar to `VelenEvent`, you can also place the handler onto its own class. An example of such can be 
seen below:

### ExamplePaginateEvent.class
This is the handler class for pagination.
```java
class ExamplePaginateEvent implements PaginateEvent<String> {

    private EmbedBuilder embed(String currentItem, int arrow, int maximum) {
      return new EmbedBuilder().setTitle("Item [" + (arrow + 1) + "/" + maximum + "]")
        .setDescription(currentItem).setColor(Color.BLUE);
    }

    private EmbedBuilder embed(String currentItem) {
      return new EmbedBuilder().setTitle("Item")
        .setDescription(currentItem).setColor(Color.BLUE);
    }

    @Override
    public MessageBuilder onInit(MessageCreateEvent event, String currentItem,
      int arrow, Paginator<String> paginator) {
      return new MessageBuilder().setEmbed(embed(currentItem, arrow, paginator.size()));
    }

    @Override
    public void onPaginate(MessageCreateEvent event, Message paginateMessage, String currentItem,
      int arrow, Paginator<String> paginator) {
      paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
    }

    @Override
    public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
      return new MessageBuilder().setContent("There are currently no items!");
    }

    @Override
    public void onSelect(MessageCreateEvent event, Message paginateMessage, String itemSelected,
      int arrow, Paginator<String> paginator) {
      paginateMessage.edit(embed(itemSelected));
    }
    
}
```

### Main.class
The main class where you register all your Velen commands.
```java
VelenCommand.of("paginate", "Tests pagination.", velen, (event, message, user, args) -> {
  List<String> testList = Arrays.asList("Test 0", "Test 1", "Test 2", "Test 3", "Test 4");
  new Paginate<>(testList).paginate(event, new ExamplePaginateEvent(), Duration.ofMinutes(5));
}).attach();
```

Optionally, you can customize the emojis (unicode or Javacord Emoji objects) that will be used, for example (this example will use Unicode):
```java
new Paginate<>(items, "➡", "⬅", "👍", "👎").paginate(...);
```

## 🏎️ How does Velen work?
Velen works through a single monolith listener who then processes each command invocation onto their own
respective commands in a multi-threaded fashion (which means each command is on their own threads) when executed,
similar to a Listener in Javacord (or rather, they are pretty much the same).
