package com.example.dogsitmalta.utils;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented tests for {@link ImageUtils}.
 * Tests creation of internal image storage directory, temp image file creation,
 * and deleting internal images on device disk storage.
 */
@RunWith(AndroidJUnit4.class)
public class ImageUtilsInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testGetImageDirectory_createsAndReturnsImagesFolder() {
        File directory = ImageUtils.getImageDirectory(context);

        assertNotNull(directory);
        assertTrue("Directory must exist on disk", directory.exists());
        assertTrue("File must be a directory", directory.isDirectory());
        assertEquals("images", directory.getName());
    }

    @Test
    public void testCreateImageFile_generatesTempJpgFileInImagesDirectory() throws IOException {
        File imageFile = ImageUtils.createImageFile(context);

        assertNotNull(imageFile);
        assertTrue("Created image file must exist", imageFile.exists());
        assertTrue("File name must start with 'post_'", imageFile.getName().startsWith("post_"));
        assertTrue("File name must end with '.jpg'", imageFile.getName().endsWith(".jpg"));
        assertEquals(ImageUtils.getImageDirectory(context).getAbsolutePath(), imageFile.getParent());

        // Cleanup
        //noinspection ResultOfMethodCallIgnored
        imageFile.delete();
    }

    @Test
    public void testDeleteInternalImage_deletesExistingFileAndHandlesNullPathSafely() throws IOException {
        // Create temp file
        File imageFile = ImageUtils.createImageFile(context);
        String path = imageFile.getAbsolutePath();
        assertTrue("Temp file must exist before deletion", new File(path).exists());

        // Delete existing image
        ImageUtils.deleteInternalImage(path);
        assertFalse("File must be deleted from disk", new File(path).exists());

        // Test non-existent paths and null strings safely without throwing exceptions
        ImageUtils.deleteInternalImage(null);
        ImageUtils.deleteInternalImage("");
        ImageUtils.deleteInternalImage("   ");
        ImageUtils.deleteInternalImage("/nonexistent/path/image.jpg");
    }
}
