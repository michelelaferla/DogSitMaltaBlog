package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogsitmalta.adapter.BlogPostAdapter;
import com.example.dogsitmalta.database.DatabaseHelper;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.utils.ImageUtils;
import com.example.dogsitmalta.sync.SyncManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements BlogPostAdapter.Listener {

    private static final int ACTION_PUBLISH = 1001;
    private static final int ACTION_DELETE = 1002;

    private DatabaseHelper databaseHelper;
    private BlogPostAdapter adapter;
    private SearchView searchView;
    private TextView emptyView;

    private ActionMode actionMode;

    private Button premium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        RecyclerView recyclerPosts = findViewById(R.id.recyclerPosts);
        searchView = findViewById(R.id.searchPosts);
        emptyView = findViewById(R.id.textEmpty);

        FloatingActionButton createButton =
                findViewById(R.id.fabCreatePost);

        adapter = new BlogPostAdapter(this);

        recyclerPosts.setLayoutManager(
                new LinearLayoutManager(this)
        );
        recyclerPosts.setAdapter(adapter);

        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.clearFocus();

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        refreshPosts(query);
                        searchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        refreshPosts(newText);
                        return true;
                    }
                }
        );

        premium = findViewById(R.id.premium);
        premium.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });


        createButton.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, PostEditorActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String search =
                searchView == null ? "" : searchView.getQuery().toString();

        refreshPosts(search);
        new SyncManager(this).sync(() -> {
            if (!isFinishing() && !isDestroyed()) refreshPosts(searchView.getQuery().toString());
        });
    }

    private void refreshPosts(String searchText) {
        List<BlogPost> posts =
                databaseHelper.searchPosts(searchText);

        adapter.setPosts(posts);

        emptyView.setVisibility(
                posts.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public void onPostClicked(BlogPost post) {
        Intent intent =
                new Intent(this, PostDetailActivity.class);

        intent.putExtra(
                PostDetailActivity.EXTRA_POST_ID,
                post.getId()
        );

        startActivity(intent);
    }

    @Override
    public void onPostLongClicked(BlogPost post) {
        if (actionMode == null) {
            actionMode =
                    startSupportActionMode(actionModeCallback);

            adapter.setSelectionMode(true);
        }

        adapter.toggleSelection(post.getId());
    }

    @Override
    public void onSelectionChanged() {
        if (actionMode == null) {
            return;
        }

        int count = adapter.getSelectedCount();

        actionMode.setTitle(
                getResources().getQuantityString(
                        R.plurals.selected_count,
                        count,
                        count
                )
        );

        if (count == 0) {
            actionMode.finish();
        }
    }

    private final ActionMode.Callback actionModeCallback =
            new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(
                ActionMode mode,
                Menu menu
        ) {
            menu.add(Menu.NONE, ACTION_DELETE, Menu.NONE, R.string.delete_selected)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(
                ActionMode mode,
                Menu menu
        ) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(
                ActionMode mode,
                MenuItem item
        ) {

            if (item.getItemId() == ACTION_DELETE) {
                confirmBulkDelete();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapter.setSelectionMode(false);
        }
    };

    private void confirmBulkDelete() {
        int count = adapter.getSelectedCount();

        if (count == 0) {
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_selected)
            .setMessage(getResources().getQuantityString(
                            R.plurals.confirm_bulk_delete,
                            count,
                            count
                    ))
            .setPositiveButton(R.string.delete, (dialog, which) -> deleteSelectedPosts())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void deleteSelectedPosts() {
        List<Long> selectedIds = adapter.getSelectedIds();

        for (Long id : selectedIds) {
            BlogPost post = databaseHelper.getPost(id);

            if (post != null) {
                ImageUtils.deleteInternalImage(post.getImagePath());
            }
        }

        databaseHelper.deletePosts(selectedIds);

        if (actionMode != null) {
            actionMode.finish();
        }

        refreshPosts(searchView.getQuery().toString());
        new SyncManager(this).sync(() -> refreshPosts(searchView.getQuery().toString()));

        Toast.makeText(
                this,
                R.string.posts_deleted,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
