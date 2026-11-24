package com.example.dogsitmalta.adapter;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dogsitmalta.model.BlogPost;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented tests for {@link BlogPostAdapter}.
 * Tests adapter dataset updates, selection mode toggles, selected item count,
 * selected IDs collection, and selected post models filtering.
 */
@RunWith(AndroidJUnit4.class)
public class BlogPostAdapterTest {

    private BlogPostAdapter adapter;
    private boolean selectionChangedCalled = false;

    @Before
    public void setUp() {
        selectionChangedCalled = false;
        adapter = new BlogPostAdapter(new BlogPostAdapter.Listener() {
            @Override
            public void onPostClicked(BlogPost post) {}

            @Override
            public void onPostLongClicked(BlogPost post) {}

            @Override
            public void onSelectionChanged() {
                selectionChangedCalled = true;
            }
        });
    }

    @Test
    public void testSetPosts_updatesItemCount() {
        assertEquals(0, adapter.getItemCount());

        BlogPost post1 = new BlogPost();
        post1.setId(1L);
        post1.setTitle("Title 1");

        BlogPost post2 = new BlogPost();
        post2.setId(2L);
        post2.setTitle("Title 2");

        adapter.setPosts(Arrays.asList(post1, post2));
        assertEquals(2, adapter.getItemCount());

        adapter.setPosts(null);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testToggleSelection_updatesSelectedIdsAndSelectedPosts() {
        BlogPost post1 = new BlogPost();
        post1.setId(10L);
        post1.setTitle("Post 10");

        BlogPost post2 = new BlogPost();
        post2.setId(20L);
        post2.setTitle("Post 20");

        adapter.setPosts(Arrays.asList(post1, post2));
        adapter.setSelectionMode(true);

        assertEquals(0, adapter.getSelectedCount());

        // Toggle post 10 selection
        adapter.toggleSelection(10L);
        assertEquals(1, adapter.getSelectedCount());
        assertTrue(adapter.getSelectedIds().contains(10L));
        assertTrue(selectionChangedCalled);

        List<BlogPost> selectedPosts = adapter.getSelectedPosts();
        assertEquals(1, selectedPosts.size());
        assertEquals("Post 10", selectedPosts.get(0).getTitle());

        // Toggle post 10 off
        adapter.toggleSelection(10L);
        assertEquals(0, adapter.getSelectedCount());
    }

    @Test
    public void testSetSelectionModeFalse_clearsSelectedIds() {
        BlogPost post1 = new BlogPost();
        post1.setId(1L);
        adapter.setPosts(java.util.Collections.singletonList(post1));

        adapter.setSelectionMode(true);
        adapter.toggleSelection(1L);
        assertEquals(1, adapter.getSelectedCount());

        // Disabling selection mode clears selected IDs
        adapter.setSelectionMode(false);
        assertEquals(0, adapter.getSelectedCount());
    }
}
