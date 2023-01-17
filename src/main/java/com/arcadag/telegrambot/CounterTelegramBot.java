package com.arcadag.telegrambot;

import com.arcadag.telegrambot.component.BotCommands;
import com.arcadag.telegrambot.component.Buttons;
import com.arcadag.telegrambot.config.BotConfig;
import com.arcadag.telegrambot.database.UserRepository;
import com.arcadag.telegrambot.models.User;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class CounterTelegramBot extends TelegramLongPollingBot implements BotCommands {
    @Autowired
    private UserRepository userRepository;
    private final BotConfig config;

    public CounterTelegramBot(BotConfig config) {
        this.config = config;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        Long chatId = 0L;
        Long userId = 0L;
        String userName = null;
        String receivedMessage;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();

            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                botAnswerUtils(receivedMessage, chatId, userName);
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            receivedMessage = update.getCallbackQuery().getData();
            botAnswerUtils(receivedMessage, chatId, userName);
        }

        if (chatId == Long.valueOf(config.getChanId())) {
            updateDB(userId, userName);
        }
    }

    private void startBot(Long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Hi, " + userName + "! I'm a Telegram bot.");
        message.setReplyMarkup(Buttons.inlineMarkup());

        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void botAnswerUtils(String receivedMessage, Long chatId, String userName) {
        switch (receivedMessage) {
            case "/start" -> startBot(chatId, userName);
            case "/help" -> sendHelpText(chatId, HELP_TEXT);

        }
    }


    private void sendHelpText(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void updateDB(Long userId, String userName) {
        if (userRepository.findById(userId).isEmpty()) {
            User user = new User();
            user.setId(userId);
            user.setName(userName);
            user.setMsg_numb(1);

            userRepository.save(user);
            log.info("Added to DB: " + user);
        } else {
            userRepository.updateMsgNumberByUserId(userId);
        }
    }
}
