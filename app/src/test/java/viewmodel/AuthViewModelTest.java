package viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import repository.AuthRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class AuthViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private AuthRepository repository;

    @Mock
    private Observer<String> statusObserver;

    private AuthViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new AuthViewModel(repository);
        viewModel.getAuthStatus().observeForever(statusObserver);
    }

    @Test
    public void login_callsRepository() {
        String email = "test@test.com";
        String pass = "password";

        viewModel.login(email, pass);

        verify(repository).login(eq(email), eq(pass), any());
    }

    @Test
    public void logout_callsRepository() {
        viewModel.logout();
        verify(repository).logout();
    }
}
