package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository repository;

    public SchedulerConfig(TelegramBot telegramBot, NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @Scheduled(cron = "0 0/1 * * * *") // метод будет выполняться каждую минуту
    public void run() {
        List<NotificationTask> list = repository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)); // получаем списсок задач на текущее время, при этом его “обрезаем”  до минут, чтобы получилось время с 00 секунд

        for (NotificationTask task : list) {
            if (task != null) { // проходим по списку задач
                SendMessage notification = new SendMessage(task.getChatId(), task.getTextNotification()); // берем из задачи чатАйди и текс Напоминания
                telegramBot.execute(notification); // отправляем
            }
        }
    }
}
