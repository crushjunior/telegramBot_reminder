package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private  final TelegramBot telegramBot;
    private final NotificationTaskRepository repository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            String incomeMessage = update.message().text();

            if (incomeMessage != null) {
                String nameUser = update.message().chat().firstName();
                Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
                Matcher matcher = pattern.matcher(incomeMessage);

                if (incomeMessage.equals("/start")) {
                    String messageText = "Hello, " + nameUser + "!" + '\n' + "Enter a reminder in the format: 01.01.2023 20:00 Task";
                    sendMessage(update, messageText);
                }

                if (matcher.matches()) {
                    NotificationTask notification = new NotificationTask();
                    Timestamp date = Timestamp.valueOf(LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))); // извлекаем из 1 группы время в нужном формате и сохраняем в переменную
                    String task = String.valueOf(matcher.group(3));

                    notification.setChatId(update.message().chat().id());
                    notification.setDateTime(date.toLocalDateTime());
                    notification.setTextNotification(task);

                    repository.save(notification);

                    String taskRecord = "You created task: " + task + '\n' + "Date and time of execution: " + date.toLocalDateTime();
                    sendMessage(update, taskRecord);
                }
            }



        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Update update, String messageText) {
        SendMessage message = new SendMessage(update.message().chat().id(), messageText);
        SendResponse response = telegramBot.execute(message);

        if (response.isOk()) {
            logger.info("Message sent");
        } else {
            logger.error("Message was not sent because of " + response.errorCode());
        }
    }

}
