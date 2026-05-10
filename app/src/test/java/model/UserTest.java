package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void userConstructor_isCorrect() {
        String uid = "uid123";
        String email = "test@example.com";
        String nickname = "Tester";

        User user = new User(uid, email, nickname);

        assertEquals(uid, user.getUid());
        assertEquals(email, user.getEmail());
        assertEquals(nickname, user.getNickname());
    }

    @Test
    public void userSetters_workCorrectly() {
        User user = new User();
        user.setUid("newUid");
        user.setEmail("new@email.com");
        user.setNickname("NewNick");

        assertEquals("newUid", user.getUid());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("NewNick", user.getNickname());
    }
}
