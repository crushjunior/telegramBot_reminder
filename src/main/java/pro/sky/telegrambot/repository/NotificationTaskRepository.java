package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.ArrayList;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    ArrayList<NotificationTask> findAllByDateTime(LocalDateTime dateTime);
}
