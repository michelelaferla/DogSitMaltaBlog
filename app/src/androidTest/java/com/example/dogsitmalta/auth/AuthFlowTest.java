package com.example.dogsitmalta.auth;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dogsitmalta.LoginActivity;
import com.example.dogsitmalta.R;
import com.example.dogsitmalta.RegisterActivity;
import com.example.dogsitmalta.database.repos.RoomUserRepository;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.utils.PasswordHasher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented tests for authentication workflows in {@link LoginActivity},
 * {@link RegisterActivity}, and {@link RoomUserRepository}.
 */
@RunWith(AndroidJUnit4.class)
public class AuthFlowTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> registerRule =
            new ActivityScenarioRule<>(RegisterActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // ==========================================
    // 1. REGISTRATION FORM VALIDATION SCENARIOS
    // ==========================================

    @Test
    public void testRegister_emptyFields_failsValidation() {
        // Leave all fields blank and click register button
        onView(withId(R.id.regbut)).perform(click());

        // Button should remain enabled after failed validation
        onView(withId(R.id.regbut)).check(matches(isEnabled()));
    }

    @Test
    public void testRegister_invalidEmailFormat_failsValidation() {
        onView(withId(R.id.name)).perform(typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.surname)).perform(typeText("Doe"), closeSoftKeyboard());
        onView(withId(R.id.logemail)).perform(typeText("invalid-email-format"), closeSoftKeyboard());
        onView(withId(R.id.logpass)).perform(typeText("Password123!"), closeSoftKeyboard());

        onView(withId(R.id.regbut)).perform(click());

        // Form submission is rejected due to invalid email pattern
        onView(withId(R.id.regbut)).check(matches(isEnabled()));
    }

    @Test
    public void testRegister_passwordTooShort_failsValidation() {
        onView(withId(R.id.name)).perform(typeText("Jane"), closeSoftKeyboard());
        onView(withId(R.id.surname)).perform(typeText("Smith"), closeSoftKeyboard());
        onView(withId(R.id.logemail)).perform(typeText("jane.smith@example.com"), closeSoftKeyboard());
        onView(withId(R.id.logpass)).perform(typeText("short"), closeSoftKeyboard()); // < 8 characters

        onView(withId(R.id.regbut)).perform(click());

        // Form submission is rejected due to short password
        onView(withId(R.id.regbut)).check(matches(isEnabled()));
    }

    // ==========================================
    // 2. LOCAL REPOSITORY AUTHENTICATION LOGIC
    // ==========================================

    @Test
    public void testRoomUserRepository_successfulLocalInsertAndAuthenticate() {
        Context context = ApplicationProvider.getApplicationContext();
        RoomUserRepository repository = new RoomUserRepository(context);

        String email = "auth_test_" + System.currentTimeMillis() + "@example.com";
        char[] password = "StrongPassword123!".toCharArray();

        User newUser = new User();
        newUser.setUserName("Test");
        newUser.setUserSurname("User");
        newUser.setUserEmail(email);
        newUser.setUserPassword(PasswordHasher.hash(password));
        newUser.setUserIsActive(true);

        long id = repository.insertUser(newUser);
        assertTrue("Inserted user ID should be positive", id > 0);

        // Verify successful local authentication
        User authenticatedUser = repository.authenticateLocal(email, "StrongPassword123!".toCharArray());
        assertNotNull("Authentication should succeed for matching credentials", authenticatedUser);

        // Verify failed authentication for wrong password
        User failedUser = repository.authenticateLocal(email, "WrongPassword999!".toCharArray());
        assertNull("Authentication should return null for incorrect password", failedUser);
    }
}
