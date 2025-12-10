package i.imessenger.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(ChatMessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<ChatMessageEntity> messages);

    @Query("SELECT * FROM chat_messages WHERE groupId = :groupId ORDER BY dateObject ASC")
    LiveData<List<ChatMessageEntity>> getGroupMessages(String groupId);

    @Query("SELECT * FROM chat_messages WHERE (senderId = :senderId AND receiverId = :receiverId) OR (senderId = :receiverId AND receiverId = :senderId) ORDER BY dateObject ASC")
    LiveData<List<ChatMessageEntity>> getPrivateMessages(String senderId, String receiverId);
}
