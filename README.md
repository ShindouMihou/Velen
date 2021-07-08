# ✨ Velen ![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/ShindouMihou/Velen?label=version&style=flat-square) ![Discord](https://img.shields.io/discord/807084089013174272?color=blue&label=Discord&style=flat-square) ![GitHub commit activity](https://img.shields.io/github/commit-activity/m/ShindouMihou/Velen?color=red&style=flat-square) ![GitHub last commit](https://img.shields.io/github/last-commit/ShindouMihou/Velen?color=orange&style=flat-square)
Velen is a command framework (or library) that is created mainly for Javacord with the aim to make everything more easier
and faster to create, for example, slash commands, hybrid commands, cooldowns (rate-limiters), per-server prefixes, pagination and more!

For a more organized look-through at Velen, please check our [GitHub Wiki](https://github.com/ShindouMihou/Velen/wiki) instead
where everything is more organized and easier to read.

## 🎂 Features
|           Feature          	| Supported 	|
|:--------------------------:	|:---------:	|
| Permission-locked commands 	|     ✔️     	|
|    Role-locked commands    	|     ✔️     	|
|  Fuzzy Command Suggestion  	|     ✔️     	|
|  Blacklist (Ignore Users)  	|     ✔️     	|
|       Prefix Manager       	|     ✔️     	|
|    Customizable Messages   	|     ✔️     	|
|         Pagination         	|     ✔️     	|
|   Rate-limiter (Cooldown)  	|     ✔️     	|
|       Slash Comamnds       	|     ✔️     	|
|       Hybrid Commands      	|     ✔️     	|
|       Normal Commands      	|     ✔️     	|
|      Mention as Prefix     	|     ✔️     	|
|        Fast Updates        	|     ✔️     	|

## 🔌 Requirements
Velen only has one requirements and that is the latest Javacord, this will be
updated everytime Javacord releases a new patch. Please ensure your Javacord version
will be always up-to-date when using Velen!
- Javacord v3.3.2

## 📚 Wiki & Guide
We highly recommend reading our wiki where everything is explained more in detailed: https://github.com/ShindouMihou/Velen/wiki
- [Getting Started](https://github.com/ShindouMihou/Velen/wiki/Getting-Started)
- [Building Velen Component](https://github.com/ShindouMihou/Velen/wiki/Velen-Main-Component)
- [Building Message Commands](https://github.com/ShindouMihou/Velen/wiki/Building-Commands!)
- [Building Slash and Hybrid Commands](https://github.com/ShindouMihou/Velen/wiki/Building-Slash-&-Hybrid-Commands!)
- [Customizing Prefix Manager](https://github.com/ShindouMihou/Velen/wiki/Prefix-Manager)
- [Customizing Messages](https://github.com/ShindouMihou/Velen/wiki/Velen-Message-Component)
- [Customizing Cooldowns](https://github.com/ShindouMihou/Velen/wiki/Rate-limiter)
- [Blacklist or Ignoring Users](https://github.com/ShindouMihou/Velen/wiki/Blacklist!)
- [Fuzzy Command Suggestion](https://github.com/ShindouMihou/Velen/wiki/Fuzzy-Command-Suggestion)
- [Simple Pagination](https://github.com/ShindouMihou/Velen/wiki/Velen-Pagination-Helper)
- [Velen Utilities](https://github.com/ShindouMihou/Velen/wiki/Velen-Utils)

## ✔️ Ping-Pong Example
A very simple of a ping-pong command in Velen is:
```java
Velen velen = Velen.builder().setDefaultPrefix("v.").build();
VelenCommand.of("ping", velen, (event, message, user, args) -> message.reply("Pong!")).attach();

DiscordApi api = new DiscordApiBuilder().setToken(token)
                 .addListener(velen).login().join();
```

An example of slash command in Velen is:
```java
Velen velen = Velen.builder().setDefaultPrefix("v.").build();
VelenCommand.ofSlash("velenSlash", "A normal velen slash command.", velen, (event, user, args, options, firstResponder) -> 
          firstResponder.setContent("Hello!").respond()).attach();

DiscordApi api = new DiscordApiBuilder().setToken(token)
                 .addListener(velen).login().join();
```

An example of a hybrid command in Velen is:
```java
VelenCommand.ofHybrid("velenHybrid", "A velen hybrid command!", velen, 
                (event, message, user, args) -> message.reply("Hello!"),
                (event, user, args1, options, firstResponder) -> firstResponder.setContent("Hello").respond())
                .attach();
```

You can place the event handlers on their own classes, as well. If you want to learn more about those,
feel free to look at our Wiki where we explain everything more in detail: 
- [Slash Commands and Hybrid Commands](https://github.com/ShindouMihou/Velen/wiki/Building-Slash-&-Hybrid-Commands!)
- [Message Commands](https://github.com/ShindouMihou/Velen/wiki/Building-Commands!)

## ❤️ Installation
You can easily install Velen through Maven Central by adding these entries onto your `build.gradle` or `pom.xml`:

### Maven
```xml
<dependency>
  <groupId>pw.mihou</groupId>
  <artifactId>Velen</artifactId>
  <version>1.0.9</version>
</dependency>
```

### Gradle
```gradle
implementation 'pw.mihou:Velen:1.0.9'
```

## ⛰️ Velen is used by
- [Amelia](https://github.com/ManaNet/Amelia): A Discord bot that is dedicated to a webnovel site called ScribbleHub.
- More to be added, feel free to create an issue if you want to add yours here!

## 🏎️ How does Velen work?
Velen works through a single monolith listener who then processes each command invocation onto their own
respective commands in a multi-threaded fashion (which means each command is on their own threads) when executed,
similar to a Listener in Javacord (or rather, they are pretty much the same).
