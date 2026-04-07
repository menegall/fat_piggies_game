package com.fatpiggies.game.android.network;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fatpiggies.game.network.AuthService.AuthCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AndroidAuthTest {
    @Mock
    private FirebaseAuth mockFirebaseAuth;
    @Mock
    private FirebaseUser mockUser;
    @Mock
    private Task<AuthResult> mockTask;
    @Mock
    private AuthCallback mockCallback;

    private AndroidAuth androidAuth;

    @Before
    public void setUp() {
        // Initialise injecting fake Firebase
        androidAuth = new AndroidAuth(mockFirebaseAuth);
    }

    @Test
    public void testGetCurrentUserId_WhenUserIsLoggedIn() {
        // Tell Mockito what to return when we call getCurrentUser()
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("fake_user_123");

        // Call the method under test
        String result = androidAuth.getCurrentUserId();

        // Check if result is correct
        org.junit.Assert.assertEquals("fake_user_123", result);
    }

    @Test
    public void testGetCurrentUserId_WhenUserIsNull() {
        // Prepare
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);
        // Action
        String result = androidAuth.getCurrentUserId();
        // Check
        org.junit.Assert.assertNull(result);
    }

    @Test
    public void testSignIn_Success() {
        // Asynchronous Test
        when(mockFirebaseAuth.signInAnonymously()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("fake_user_123");

        androidAuth.signIn(mockCallback);

        // Capture Listener
        ArgumentCaptor<OnCompleteListener> captor = ArgumentCaptor.forClass(OnCompleteListener.class);
        verify(mockTask).addOnCompleteListener(captor.capture());

        // Simulate end of task
        captor.getValue().onComplete(mockTask);

        // Check if callback is called with correct user ID
        verify(mockCallback).onSuccess("fake_user_123");
    }

    @Test
    public void testSignIn_Failure() {
        // Asynchronous Test
        when(mockFirebaseAuth.signInAnonymously()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);

        androidAuth.signIn(mockCallback);

        // Capture Listener
        ArgumentCaptor<OnCompleteListener> captor = ArgumentCaptor.forClass(OnCompleteListener.class);
        verify(mockTask).addOnCompleteListener(captor.capture());

        // Simulate end of task
        captor.getValue().onComplete(mockTask);

        // Check if callback is called with correct user ID
        verify(mockCallback).onFailure("Authentication failed.");
    }
}
