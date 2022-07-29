package ru.georgii.fonarserver.dialog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.georgii.fonarserver.user.User;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = "SELECT * \n" +
            "    FROM message m INNER JOIN\n" +
            "                (\n" +
            "                    SELECT  MAX(creation_date) AS max_date,\n" +
            "                            CONCAT(LEAST(from_user_id, user_id), '_', GREATEST(from_user_id, user_id)) dialog_id\n" +
            "                    FROM message\n" +
            "                    WHERE from_User_id = :#{#user.id} OR user_id = :#{#user.id} \n" +
            "                    GROUP BY dialog_id\n" +
            "                    order by max_date DESC\n" +
            "                ) recents\n" +
            " ON m.creation_date=recents.max_date AND CONCAT(LEAST(from_user_id, user_id), '_', GREATEST(from_user_id, user_id)) =recents.dialog_id\n" +
            " ORDER BY m.creation_date DESC LIMIT :quantity OFFSET :offset"
            , nativeQuery = true)
    List<Message> getDialogs(@Param("user") User user, @Param("quantity") Long quantity, @Param("offset") Long offset);


    @Query(value = "select * from message where (from_user_id = :#{#a.id} AND user_id = :#{#b.id}) OR (from_user_id = :#{#b.id} AND user_id = :#{#a.id}) ORDER BY creation_date DESC LIMIT :quantity OFFSET :offset", nativeQuery = true)
    List<Message> getDialog(@Param("a") User a, @Param("b") User b, @Param("quantity") Long quantity, @Param("offset") Long offset);

    Long countMessagesBySeenIsFalseAndFromUserAndToUser(User from, User to);

    @Query(value = "select * from message where from_user_id = :a AND user_id = :b AND id = :id LIMIT 1", nativeQuery = true)
    Message findMessageInConversation(Long a, Long b, Long id);

}