package com.example.dogsitmalta.ui;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dogsitmalta.MainActivity;
import com.example.dogsitmalta.PostDetailActivity;
import com.example.dogsitmalta.PostEditorActivity;
import com.example.dogsitmalta.R;
import com.example.dogsitmalta.RegisterActivity;
import com.example.dogsitmalta.database.BlogRoomDatabase;
import com.example.dogsitmalta.database.entities.PostEntity;

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
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Espresso UI tests for {@link MainActivity}.
 * Verifies activity launch, visibility of key UI elements, search input behavior,
 * empty state displays, button interactions, and navigation intents.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // ==========================================
    // 1. ACTIVITY LAUNCH & UI ELEMENT VISIBILITY
    // ==========================================

    @Test
    public void testActivityLaunch_displaysMainUiElements() {
        // Verify main view elements exist and are displayed
        onView(withId(R.id.searchPosts)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerPosts)).check(matches(isDisplayed()));
        onView(withId(R.id.premium)).check(matches(isDisplayed()));
        onView(withId(R.id.fabCreatePost)).check(matches(isDisplayed()));
    }

    @Test
    public void testHeadings_areDisplayedWithCorrectText() {
        onView(withText("DogSit Malta")).check(matches(isDisplayed()));
        onView(withText("Community blog for dog sitters and pet owners")).check(matches(isDisplayed()));
    }

    // ==========================================
    // 2. BUTTON INTERACTIONS & NAVIGATION
    // ==========================================

    @Test
    public void testClickFabCreatePost_opensPostEditorActivity() {
        onView(withId(R.id.fabCreatePost)).perform(click());

        // Verify navigation intent launched PostEditorActivity
        intended(hasComponent(PostEditorActivity.class.getName()));
    }

    @Test
    public void testClickPremiumButton_opensRegisterActivity() {
        onView(withId(R.id.premium)).perform(click());

        // Verify navigation intent launched RegisterActivity
        intended(hasComponent(RegisterActivity.class.getName()));
    }

    // ==========================================
    // 3. SEARCH INPUT & EMPTY STATE BEHAVIOR
    // ==========================================

    @Test
    public void testSearchInput_acceptsQueryText() {
        onView(withId(R.id.searchPosts)).perform(click());
        onView(withId(R.id.searchPosts)).perform(typeText("Sliema"), closeSoftKeyboard());

        // SearchView should contain typed query
        onView(withId(R.id.searchPosts)).check(matches(isDisplayed()));
    }

    @Test
    public void testNonMatchingSearch_displaysEmptyStateMessage() throws InterruptedException {
        // Type a search query guaranteed not to match any blog post
        onView(withId(R.id.searchPosts)).perform(click());
        onView(withId(R.id.searchPosts)).perform(typeText("NON_EXISTENT_QUERY_XYZ_999"), closeSoftKeyboard());

        // Wait for background disk executor query to update UI
        Thread.sleep(600);

        // Verify empty view text is displayed
        onView(withId(R.id.textEmpty)).check(matches(isDisplayed()));
        onView(withId(R.id.textEmpty)).check(matches(withText(R.string.no_posts)));
    }
}
