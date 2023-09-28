> **Unmaintained!**
>
> Velen has been unmaintained for over years now. For slash commands and context menus, we recommend using the successor framework [`Nexus`](https://github.com/ShindouMihou/Nexus) instead
> which has been actively maintained since Velen went inactive and is used more, always kept up-to-date with Discord's latest changes.

<img src="https://i.ibb.co/Ny1V3sg/Velen-Banner.png" width="800px" width="250px">

# ✨ Velen ![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/ShindouMihou/Velen?label=version&style=flat-square) [![Discord](https://img.shields.io/discord/807084089013174272?color=blue&label=Discord&style=flat-square)](https://discord.gg/9FefYq4p83) ![GitHub commit activity](https://img.shields.io/github/commit-activity/m/ShindouMihou/Velen?color=red&style=flat-square) ![GitHub last commit](https://img.shields.io/github/last-commit/ShindouMihou/Velen?color=orange&style=flat-square)
Aiming to become the Laravel of Javacord; Velen is a framework for Discord bots using Javacord with complete features from hybrid (message and slash commands), slash commands, prefix managers, blacklists, fuzzy command search, cooldowns and many more. 

The aim of Velen is to reduce the time it takes for developers to setup a Discord bot without compromising on performance.
For a more organized look-through at Velen, please check our [GitHub Wiki](https://github.com/ShindouMihou/Velen/wiki) instead
where everything is more organized and easier to read.

## 🔌 Requirements
Velen only has one requirements and that is the latest Javacord, this will be
updated everytime Javacord releases a new patch. Please ensure your Javacord version
will be always up-to-date when using Velen!
- Javacord v3.4.0

## 📚 Wiki & Guide
As of Velen 3.0, the wiki explains not much compared to the examples for building commands and also fetching commands, we highly recommend taking a look at our examples instead to see a good look over how Velen looks:
- [MUST READ FIRST](https://github.com/ShindouMihou/Velen/tree/master/examples)
- [Hybrid Commands](https://github.com/ShindouMihou/Velen/tree/master/examples/hybrid)
- [Message Commands](https://github.com/ShindouMihou/Velen/tree/master/examples/message)
- [Slash Commands](https://github.com/ShindouMihou/Velen/tree/master/examples/slash)

We highly recommend reading our wiki where everything is explained more in detailed
- [Getting Started](https://github.com/ShindouMihou/Velen/wiki/Getting-Started)
- [Building Velen Component](https://github.com/ShindouMihou/Velen/wiki/Velen-Main-Component)
- [Building Message, Hybrid and Slash Commands](https://github.com/ShindouMihou/Velen/wiki/Building-Commands!)
- [Mastering Velen Options](https://github.com/ShindouMihou/Velen/wiki/Mastering-Velen-Options)
- [Customizing Prefix Manager](https://github.com/ShindouMihou/Velen/wiki/Prefix-Manager)
- [Customizing Messages](https://github.com/ShindouMihou/Velen/wiki/Velen-Message-Component)
- [Customizing Cooldowns](https://github.com/ShindouMihou/Velen/wiki/Rate-limiter)
- [Blacklist or Ignoring Users](https://github.com/ShindouMihou/Velen/wiki/Blacklist!)
- [Fuzzy Command Suggestion](https://github.com/ShindouMihou/Velen/wiki/Fuzzy-Command-Suggestion)
- [Simple Pagination](https://github.com/ShindouMihou/Velen/wiki/Velen-Pagination-Helper)
- [Velen Utilities](https://github.com/ShindouMihou/Velen/wiki/Velen-Utils)

## ✔️ Ping-Pong Example
All examples are located at the examples folders below.
- [MUST READ FIRST](https://github.com/ShindouMihou/Velen/tree/master/examples)
- [Hybrid Commands](https://github.com/ShindouMihou/Velen/tree/master/examples/hybrid)
- [Message Commands](https://github.com/ShindouMihou/Velen/tree/master/examples/message)
- [Slash Commands](https://github.com/ShindouMihou/Velen/tree/master/examples/slash)

## ❤️ Installation
You can install Velen from Maven Central, otherwise known as Sonatype, which also hosts Javacord and many 
other libraries.

### 📦 Maven
```xml
<dependency>
    <groupId>pw.mihou</groupId>
    <artifactId>Velen</artifactId>
    <version>3.1.1</version>
</dependency>
```

### 📦 Gradle
```groovy
implementation 'pw.mihou:Velen:3.1.1'
```

## ⛰️ Velen is used by
- [Amelia](https://github.com/ManaNet/Amelia): A Discord bot that is dedicated to a webnovel site called ScribbleHub.
- More to be added, feel free to create an issue if you want to add yours here!

## 🔮 Version Policy
Velen has a specific version policy, in which, unless there is a critical change to be made, an update will only be released on
either of these conditions (this is to prevent me from overworking):
- 3+ issues (bug fixes, feature requests, improvements, etc).
- A critical bug fix.
- A new Javacord update.

The library also follows a similar version number policy with Javacord:
- A change in the first digit of the version will mean: **major update or two-three major breaking change**.
- A change in the second digit of the version will mean: **a large quantity of __accumulated__ updates or a breaking change**.
- A change in the last digit of the version will mean: **a minor update, usually new features or fixes**.
