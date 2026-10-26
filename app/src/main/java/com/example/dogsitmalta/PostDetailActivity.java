package com.example.dogsitmalta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogsitmalta.database.DatabaseHelper;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.utils.ImageUtils;
import com.example.dogsitmalta.utils.MapIntentUtils;
import com.example.dogsitmalta.utils.ShareUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private DatabaseHelper databaseHelper;

    private long postId;
    private BlogPost post;

    private TextView titleText;
    private TextView locationText;
    private TextView messageText;
    private TextView dateText;
    private ImageView postImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        databaseHelper = new DatabaseHelper(this);

        postId = getIntent().getLongExtra(EXTRA_POST_ID, -1L);

        titleText = findViewById(R.id.textDetailTitle);
        locationText = findViewById(R.id.textDetailLocation);
        messageText = findViewById(R.id.textDetailMessage);
        dateText = findViewById(R.id.textDetailDate);
        postImage = findViewById(R.id.imageDetail);
        Button editButton = findViewById(R.id.buttonEdit);
        Button deleteButton = findViewById(R.id.buttonDelete);
        Button shareButton = findViewById(R.id.buttonShareEmail);
        Button publishButton = findViewById(R.id.buttonPublish);

        editButton.setOnClickListener(
                v -> editPost()
        );

        deleteButton.setOnClickListener(
                v -> confirmDelete()
        );

        shareButton.setOnClickListener(v -> {
            if (post != null) {
                ShareUtils.shareSinglePost(
                        this, post, getString(R.string.share_post_chooser)
                );
            }
        });

        publishButton.setOnClickListener(v -> {
            if (post != null) {
                ShareUtils.shareSinglePost(
                        this, post, getString(R.string.publish_online)
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPost();
    }

    private void loadPost() {
        post =
                databaseHelper.getPost(postId);

        if (post == null) {
            Toast.makeText(
                    this,
                    R.string.post_not_found,
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        titleText.setText(post.getTitle());
        messageText.setText(post.getMessage());

        if (!MapIntentUtils.canOpen(post)) {
            locationText.setVisibility(View.GONE);
            locationText.setOnClickListener(null);
            locationText.setClickable(false);
        } else {
            locationText.setVisibility(View.VISIBLE);
            locationText.setText(getString(R.string.location_format,
                    MapIntentUtils.locationLabel(this, post)));
            locationText.setContentDescription(getString(R.string.map_open_locality,
                    MapIntentUtils.locationLabel(this, post)));
            locationText.setPaintFlags(locationText.getPaintFlags()
                    | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            locationText.setOnClickListener(v -> MapIntentUtils.openSavedLocation(this, post));
        }

        dateText.setText(
                DateFormat.getDateTimeInstance(
                        DateFormat.LONG,
                        DateFormat.SHORT
                ).format(new Date(post.getCreatedAt()))
        );

        String imagePath = post.getImagePath();

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                postImage.setImageURI(null);
                postImage.setImageURI(
                        Uri.fromFile(imageFile)
                );
                postImage.setVisibility(View.VISIBLE);
            } else {
                postImage.setVisibility(View.GONE);
            }
        } else {
            postImage.setVisibility(View.GONE);
        }
    }

    private void editPost() {
        Intent intent =
                new Intent(this, PostEditorActivity.class);

        intent.putExtra(
                PostEditorActivity.EXTRA_POST_ID,
                postId
        );

        startActivity(intent);
    }

    private void confirmDelete() {
        if (post == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_post)
                .setMessage(R.string.confirm_delete_post)
                .setPositiveButton(
                        R.string.delete,
                        (dialog, which) -> deletePost()
                )
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deletePost() {
        databaseHelper.deletePost(postId);

        ImageUtils.deleteInternalImage(
                post.getImagePath()
        );

        Toast.makeText(
                this,
                R.string.post_deleted,
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
