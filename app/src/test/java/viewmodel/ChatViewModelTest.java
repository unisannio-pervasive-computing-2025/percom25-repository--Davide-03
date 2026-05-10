package viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import repository.ChatRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ChatViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ChatRepository repository;

    @Mock
    private Application application;

    private ChatViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new ChatViewModel(application, repository);
    }

    @Test
    public void sendMessage_callsRepository() {
        String groupId = "group1";
        String senderId = "user1";
        String senderName = "Mario";
        String text = "Hello";

        viewModel.sendMessage(groupId, senderId, senderName, text);

        verify(repository).sendMessage(eq(groupId), any(), any());
    }
}
