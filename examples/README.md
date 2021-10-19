# ☪ Velen Mirror Examples
To understand all the examples that are shown here, please read each of the README.md of the individual folders
as they show details such as how the handlers (that handles the commands) and what the purpose of the folder is, these
examples are made to showcase the neatness of Velen Mirror.

# 🎁 What is Velen Mirror
Velen Mirror is the new command constructor system of Velen that aims to separate the nature of your code and 
the command constructors which tends to be a mess especially for bots with tons of commands. Mirror is far cleaner
and verbose to read than ordinary Java builders, written under the `.velen` syntax.

To fully understand the outline of Mirror, let us take a glance at a simple ping-pong command that can be found written
similarly on both Message and Slash folders but adapted to their own use-case. Our example here will showcase how they can
be written in Hybrid Command.

**Note: I will be showing all the possible options and their explanation in the code block below**

### ⏱ Ping.velen
```velen
# This is how we specify what category the command
# will be located on.
# &[fun]: category

# We construct the name and type of the 
# command using this.
&[ping]: hybrid {

    # This is the description of the command that will show up in
    # slash commands and also can be fetched via getDescription method.
    desc: Do you want to ping-pong with me?
    
    # This is how we initialize a usage that can be fetched via
    # the getUsages method, it returns back a list because there can be
    # multiple usages in a single command.
    usage: ping [pong|ping]
    usage: ping [ping|pong]
    
    # This is otherwisely called an alias of a command, this only applies
    # to message commands but can be placed in hybrid commands as it utilizes
    # message commands as well.
    shortcut: pong
    
    # This is how you initialize an option, if you want to setup a subcommand
    # all you have to do is change `option` to `subcommand` and wrap the options
    # inside the brackets, the same thing for `subcommand_group`.
    &[response]: option {
        desc: What kind of move will you make?
        
        # This is how you initialize choices in hybrid and slash commands.
        # because slash commands uses [key, value], it is required here.
        # but on message commands, you can use [choiceOne, choiceTwo, choiceThree].
        # In our case, we are using [USER'S CHOICE, OUR RESPONSE].
        choice: [PING, PONG]
        choice: [PONG, PING]
        
        # This is how we make a slash command option required.
        # Message commands always has options required.
        required: true
        
        # This can be placed on the last option of a command
        # to tell Velen to grab all the input of the user and squash
        # it into this option, for example, Hello World would be returned
        # if this option is enabled instead of just "Hello"
        # but you can also replicate this behavior by wrapping the user input
        # in quotes like: "Hello World".
        # has_many: true
        
        # You can specify the type of this option, there are a little bit
        # of a list of options and to list them all:
        #
        # user, channel, role, numeric, boolean, mentionable, number, integer : These will all convert to their slash command equivalent.
        # user, channel, role, message, emoji, webhook, boolean, numeric, integer, string: These will all convert to string on slash command
        # but the second line of types will be aliased to their regex pattern (or algorithm) for message commands.
        type: string
        
        # You can also set regex patterns required for an option
        # this only applies to message commands at the moment though.
        # regex: ^[0-9]+
    }
    
    # This is how you specify the cooldown in milliseconds.
    # It must be in milliseconds.
    cooldown: 5000
    
    # This is how you specify a command format using message commands.
    # You should generally not use this and simply use the option constructor
    # shown above since it will also be easier to move to slash commands.
    # This is how the resposne option above looks though if you were to convert
    # it to a command format.
    # has_formats: ping :[response::(ping,pong)::required()]
    
    # This is how you tell Velen to make this command only respond
    # to servers.
    # server_only: [true]
    
    # This is how you tell Velen to make this command only in 
    # the specified server (also register the slash command there).
    # server_only: [true, 123123123123L]
    
    # This is how you tell Velen to limit this command to only
    # the people with these permissions. 
    # 
    # For a list of permissions, please use the names of the enums on
    # https://github.com/Javacord/Javacord/blob/master/javacord-api/src/main/java/org/javacord/api/entity/permission/PermissionType.java
    # permissions: [MANAGE_SERVER, ADMINISTRATOR]
    
    # This is how you tell Velen to limit this command to
    # the people with the specific roles.
    # roles: [123123123L, 123123123L]
    
    # This is how we specify the handler for the command.
    # The best practice right now is to place [command type].[command name] 
    # to make them easier to find, you can also just go with [command name] or 
    # some other name.
    handler: hybrid.ping
    
    # If you want to separate the handling for both message and slash
    # you can tell Velen by replacing the handler above with these two.
    # handler: message.ping
    # handler: slash.ping
}
```

Now that we know of all the currently possible options of Velen Mirror, let us start to minimize
the file to view the actual parts of the command, shall we?
### 😲 ping.min.velen
```velen
&[ping]: hybrid {
    desc: Do you want to ping-pong with me?
    usage: ping [pong|ping]
    usage: ping [ping|pong]
    shortcut: pong
    
    &[response]: option {
        desc: What kind of move will you make?
        choice: [PING, PONG]
        choice: [PONG, PING]
        required: true
        type: string
    }
    
    cooldown: 5000
    handler: hybrid.ping
}
```

Doesn't that look quite clean and readable, compare that to the original Java builder method:
```java
VelenCommand.ofHybrid("ping", "Do you want to ping-pong with me?",
        velen, new PingHandler(),
        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "response", "What kind of move will you make?", true, 
        Arrays.asList(
                SlashCommandOptionChoice.create("ping", "pong"),
                SlashCommandOptionChoice.create("pong", "ping")
        )))
        .addFormats("ping :[response::(ping,pong)::required()]")
        .addShortcut("pong")
        .addUsages("ping [pong|ping]", "ping [ping|pong]")
        .attach();
```

And now, imagine the same command but with two or four options, doesn't it look a bit too meh?
That is what was Velen Mirror was created for, to mirror a cleaner and readable look of command constructing.

You may be thinking now, where is the logic behind how this ping command will work?
```java
Velen velen = Velen.ofDefaults();
velen.addHandler("hybrid.ping", (event, responder, user, args) -> {
   args.withName("response").flatMap(VelenOption::asString).ifPresent(s -> responder.setContent(s).respond());
});
```

And that's quite literally how the command works, in fact, you can make it into two lines but for the sake for
readability, I've decided to expand the lambda function. Velen allows you to have a single handler for both message
and slash commands which is a key feature of the framework, but you can also opt to using multiple handlers.

That is the beauty of Velen Mirror and how you can make an entire Discord bot with very little code, did you like it?
