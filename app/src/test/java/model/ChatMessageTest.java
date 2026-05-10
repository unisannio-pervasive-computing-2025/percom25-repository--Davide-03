package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChatMessageTest {

    @Test
    public void createTextMessage_isCorrect() {
        String senderId = "user123";
        String senderName = "Mario";
        String text = "Ciao a tutti";

        ChatMessage msg = ChatMessage.createTextMessage(senderId, senderName, text);

        assertEquals(senderId, msg.getSenderId());
        assertEquals(senderName, msg.getSenderName());
        assertEquals(text, msg.getMessage());
        assertEquals(ChatMessage.TYPE_TEXT, msg.getType());
        assertNull(msg.getImageUrl());
    }

    @Test
    public void createImageMessage_isCorrect() {
        String senderId = "user123";
        String senderName = "Mario";
        String imageUrl = "https://example.com/image.jpg";

        ChatMessage msg = ChatMessage.createImageMessage(senderId, senderName, imageUrl);

        assertEquals(senderId, msg.getSenderId());
        assertEquals(senderName, msg.getSenderName());
        assertEquals(imageUrl, msg.getImageUrl());
        assertEquals(ChatMessage.TYPE_IMAGE, msg.getType());
        assertNull(msg.getMessage());
    }
}
