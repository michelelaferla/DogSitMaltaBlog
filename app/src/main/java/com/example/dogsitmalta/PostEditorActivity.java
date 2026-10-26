package com.example.dogsitmalta;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.dogsitmalta.database.DatabaseHelper;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.utils.ImageUtils;
import com.example.dogsitmalta.sync.SyncManager;
import android.widget.TextView;
import java.util.Locale;

import java.io.File;
import java.io.IOException;

public class PostEditorActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private EditText editTitle;
    private EditText editLocation;
    private EditText editMessage;
    private ImageView imagePreview;

    private DatabaseHelper databaseHelper;

    private long postId = -1L;
    private String originalImagePath;
    private String currentImagePath;
    private boolean saved = false;
    private Double selectedLat, selectedLng;
    private TextView textCoordinates;

    private final ActivityResultLauncher<android.content.Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra(LocationPickerActivity.EXTRA_LAT, 0);
                    selectedLng = result.getData().getDoubleExtra(LocationPickerActivity.EXTRA_LNG, 0);
                    String locality = result.getData().getStringExtra(LocationPickerActivity.EXTRA_LOCALITY);
                    editLocation.setText(locality == null ? "" : locality);
                    if (locality == null || locality.trim().isEmpty()) {
                        Toast.makeText(this, R.string.map_enter_locality_manually,
                                Toast.LENGTH_LONG).show();
                    }
                    showCoordinates();
                }
            });

    private void showCoordinates() {
        textCoordinates.setText(selectedLat == null ? getString(R.string.map_picker_none) :
                String.format(Locale.US, "%.5f, %.5f", selectedLat, selectedLng));
    }

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    this::handleGalleryImage
            );

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success) {
                            displayCurrentImage();
                        } else {
                            discardFailedCameraImage();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_editor);

        databaseHelper = new DatabaseHelper(this);

        editTitle = findViewById(R.id.editTitle);
        editLocation = findViewById(R.id.editLocation);
        editMessage = findViewById(R.id.editMessage);
        imagePreview = findViewById(R.id.imagePreview);
        textCoordinates = findViewById(R.id.textCoordinates);
        findViewById(R.id.buttonPickMap).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, LocationPickerActivity.class);
            if (selectedLat != null && selectedLng != null) {
                intent.putExtra(LocationPickerActivity.EXTRA_LAT, selectedLat);
                intent.putExtra(LocationPickerActivity.EXTRA_LNG, selectedLng);
                intent.putExtra(LocationPickerActivity.EXTRA_LOCALITY,
                        editLocation.getText().toString().trim());
            }
            mapLauncher.launch(intent);
        });

        Button galleryButton =
                findViewById(R.id.buttonGallery);
        Button cameraButton =
                findViewById(R.id.buttonCamera);
        Button removeImageButton =
                findViewById(R.id.buttonRemoveImage);
        Button saveButton =
                findViewById(R.id.buttonSave);

        postId =
                getIntent().getLongExtra(EXTRA_POST_ID, -1L);

        if (postId >= 0) {
            setTitle(R.string.edit_post);
            loadExistingPost();
        } else {
            setTitle(R.string.create_post);
        }

        galleryButton.setOnClickListener(
                v -> galleryLauncher.launch("image/*")
        );

        cameraButton.setOnClickListener(
                v -> takePhoto()
        );

        removeImageButton.setOnClickListener(
                v -> removeCurrentImage()
        );

        saveButton.setOnClickListener(
                v -> savePost()
        );
    }

    private void loadExistingPost() {
        BlogPost post =
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

        editTitle.setText(post.getTitle());
        editLocation.setText(post.getLocation());
        selectedLat = post.getLatitude();selectedLng = post.getLongitude();showCoordinates();
        editMessage.setText(post.getMessage());

        originalImagePath = post.getImagePath();
        currentImagePath = originalImagePath;

        displayCurrentImage();
    }

    private void handleGalleryImage(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }

        try {
            deleteUnsavedReplacementImage();

            currentImagePath =
                    ImageUtils.copyUriToInternalStorage(
                            this,
                            sourceUri
                    );

            displayCurrentImage();

        } catch (IOException exception) {
            Toast.makeText(
                    this,
                    R.string.image_import_failed,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void takePhoto() {
        try {
            deleteUnsavedReplacementImage();

            File imageFile =
                    ImageUtils.createImageFile(this);

            currentImagePath =
                    imageFile.getAbsolutePath();

            Uri destinationUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            imageFile
                    );

            cameraLauncher.launch(destinationUri);

        } catch (IOException | IllegalStateException exception) {
            Toast.makeText(
                    this,
                    R.string.camera_file_failed,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void displayCurrentImage() {
        if (currentImagePath == null ||
                currentImagePath.trim().isEmpty()) {
            imagePreview.setImageDrawable(null);
            imagePreview.setVisibility(View.GONE);
            return;
        }

        File imageFile =
                new File(currentImagePath);

        if (!imageFile.exists()) {
            imagePreview.setVisibility(View.GONE);
            return;
        }

        imagePreview.setImageURI(null);
        imagePreview.setImageURI(Uri.fromFile(imageFile));
        imagePreview.setVisibility(View.VISIBLE);
    }

    private void removeCurrentImage() {
        deleteUnsavedReplacementImage();
        currentImagePath = null;
        displayCurrentImage();
    }

    private void deleteUnsavedReplacementImage() {
        if (currentImagePath != null &&
                !currentImagePath.equals(originalImagePath)) {
            ImageUtils.deleteInternalImage(currentImagePath);
        }
    }

    private void discardFailedCameraImage() {
        deleteUnsavedReplacementImage();
        currentImagePath = originalImagePath;
        displayCurrentImage();
    }

    private void savePost() {
        String title =
                editTitle.getText().toString().trim();

        String location =
                editLocation.getText().toString().trim();

        String message =
                editMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTitle.setError(
                    getString(R.string.title_required)
            );
            editTitle.requestFocus();
            return;
        }

        if (selectedLat != null && selectedLng != null && TextUtils.isEmpty(location)) {
            editLocation.setError(getString(R.string.map_locality_required));
            editLocation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            editMessage.setError(
                    getString(R.string.message_required)
            );
            editMessage.requestFocus();
            return;
        }

        BlogPost post = new BlogPost();

        post.setId(postId);
        post.setTitle(title);
        post.setLocation(location);
        post.setMessage(message);
        post.setImagePath(currentImagePath);
        post.setLatitude(selectedLat);post.setLongitude(selectedLng);
        com.google.firebase.auth.FirebaseUser signedIn = SyncManager.currentUser(this);
        if (signedIn != null) post.setOwnerUid(signedIn.getUid());

        if (postId >= 0) {
            databaseHelper.updatePost(post);

            if (originalImagePath != null &&
                    !originalImagePath.equals(currentImagePath)) {
                ImageUtils.deleteInternalImage(
                        originalImagePath
                );
            }

            Toast.makeText(
                    this,
                    R.string.post_updated,
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            databaseHelper.insertPost(post);

            Toast.makeText(
                    this,
                    R.string.post_saved,
                    Toast.LENGTH_SHORT
            ).show();
        }

        saved = true;
        finish();
    }

    @Override
    protected void onDestroy() {
        if (!saved &&
                currentImagePath != null &&
                !currentImagePath.equals(originalImagePath)) {
            ImageUtils.deleteInternalImage(
                    currentImagePath
            );
        }

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
