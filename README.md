# Raid Master Bot

The Raid Master bot provides a Discord bot to handle raids of the mobile game Tap Titans 2 (TT2).

## Setting up the bot

To use the bot simply add it to your server using this [link](https://discord.com/api/oauth2/authorize?client_id=793759001866928139&permissions=67648&scope=bot).
If you want to set the bot up by yourself either wait until I explain it here (which I probably never will), or just reach out to me. Once the bot is connected to your server
you may simply follow its instructions and are good to go.

The bot will send all of its messages to the first channel it finds on your server that it may send messages to. If you
use an additional channel to keep track of your raids, name it 'raid-master' for the bot to scan that channel.
If not the bot will use the standard channel and only scans for the last 600 messages.

## Raids

For the bot to recognize raids they have to be in a rather specific pattern. This pattern is explained
[here](https://github.com/felixhir/raid-master-bot/blob/main/RaidTemplate).

## Functionalities

The basic idea behind the bot is for your clan to have a maximum of raids a player may not participate in until they are kicked from your clan.
While the basic allowance is 2 you may change this time using the command '!setafk #>'. The command prefix
may also be changed using '!setprefix char'.

Now every time the bot identifies a new message as raid it will add it to its database and tell you who missed their attacks.
Another small feature is the possibility to invoke '!stats playerName' to gain a players' lifetime stats (for bragging purposes).

At last the bot is able to send PSAs. This can however only be done by me, and I promise to not use it for spam.

## Dependencies

This project runs on Java SDK 1.8.</br>
All dependencies are handled by Gradle.
 * slf4j-api
   * Version: **1.7.25**
   * [Website](https://www.slf4j.org/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.slf4j%3Aslf4j-api/view)
 * JDA
   * Version: **4.2.0_224**
   * [GitHub](https://github.com/DV8FromTheWorld/JDA)
 * MariaDB Java Client
   * Version: **2.7.1**
   * [Website](https://mariadb.com/kb/en/mariadb-connector-j/)