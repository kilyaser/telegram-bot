package com.arcadag.telegrambot.component;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/start", "start bot"),
            new BotCommand("/help", "bot info")
    );

    String HELP_TEXT = """
                This bot will help to count the number of message in the chat. The following commands are available to you:
                 /start - start the bot
                 /help = help menu
                 """;
}
