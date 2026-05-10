package viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

import repository.GroupRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class GroupViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private GroupRepository repository;

    @Mock
    private Observer<String> statusObserver;

    private GroupViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new GroupViewModel(repository);
        viewModel.getActionStatus().observeForever(statusObserver);
    }

    @Test
    public void createGroup_callsRepository() {
        String name = "New Group";
        String ownerId = "owner123";
        String ownerNick = "Owner";

        viewModel.createGroup(name, ownerId, ownerNick);

        // Verify that createGroup was called on repository
        verify(repository).createGroup(eq(name), eq(ownerId), eq(ownerNick), any());
    }

    @Test
    public void addMember_withEmptyEmail_setsError() {
        viewModel.addMember("groupId", "");

        verify(statusObserver).onChanged("ERR_EMPTY_EMAIL");
    }

    @Test
    public void addMember_callsRepository() {
        String groupId = "groupId";
        String email = "test@test.com";

        viewModel.addMember(groupId, email);

        verify(repository).addMember(eq(groupId), eq(email), any());
    }
}
