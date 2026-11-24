package com.example.dogsitmalta.utils;

import com.example.dogsitmalta.model.BlogPost;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Local JVM Unit Tests for {@link MapIntentUtils}.
 * Uses boundary-value analysis to test coordinate validation and map opening rules.
 */
public class MapIntentUtilsUnitTest {

    // ==========================================
    // 1. VALID LATITUDE & LONGITUDE VALUES
    // ==========================================

    @Test
    public void testHasCoordinates_validCoordinates_returnsTrue() {
        BlogPost post = new BlogPost();
        post.setLatitude(35.8997);
        post.setLongitude(14.5147);

        assertTrue("Valid Malta coordinates should return true", MapIntentUtils.hasCoordinates(post));
    }

    @Test
    public void testHasCoordinates_boundaryExactMinMax_returnsTrue() {
        BlogPost post = new BlogPost();

        // Exact minimum boundaries (-90, -180)
        post.setLatitude(-90.0);
        post.setLongitude(-180.0);
        assertTrue("Exact min boundaries -90, -180 should return true", MapIntentUtils.hasCoordinates(post));

        // Exact maximum boundaries (90, 180)
        post.setLatitude(90.0);
        post.setLongitude(180.0);
        assertTrue("Exact max boundaries 90, 180 should return true", MapIntentUtils.hasCoordinates(post));

        // Zero point (0, 0)
        post.setLatitude(0.0);
        post.setLongitude(0.0);
        assertTrue("Equator/Prime Meridian (0, 0) should return true", MapIntentUtils.hasCoordinates(post));
    }

    // ==========================================
    // 2. LATITUDE OUTSIDE RANGE (-90 to 90)
    // ==========================================

    @Test
    public void testHasCoordinates_latitudeOutOfBounds_returnsFalse() {
        BlogPost post = new BlogPost();
        post.setLongitude(14.5);

        // Slightly below min latitude
        post.setLatitude(-90.0001);
        assertFalse("Latitude -90.0001 should return false", MapIntentUtils.hasCoordinates(post));

        // Slightly above max latitude
        post.setLatitude(90.0001);
        assertFalse("Latitude 90.0001 should return false", MapIntentUtils.hasCoordinates(post));

        // Far out of bounds
        post.setLatitude(120.0);
        assertFalse("Latitude 120.0 should return false", MapIntentUtils.hasCoordinates(post));

        post.setLatitude(-120.0);
        assertFalse("Latitude -120.0 should return false", MapIntentUtils.hasCoordinates(post));

        // Special Double values
        post.setLatitude(Double.NaN);
        assertFalse("NaN latitude should return false", MapIntentUtils.hasCoordinates(post));

        post.setLatitude(Double.POSITIVE_INFINITY);
        assertFalse("Infinity latitude should return false", MapIntentUtils.hasCoordinates(post));
    }

    // ==========================================
    // 3. LONGITUDE OUTSIDE RANGE (-180 to 180)
    // ==========================================

    @Test
    public void testHasCoordinates_longitudeOutOfBounds_returnsFalse() {
        BlogPost post = new BlogPost();
        post.setLatitude(35.9);

        // Slightly below min longitude
        post.setLongitude(-180.0001);
        assertFalse("Longitude -180.0001 should return false", MapIntentUtils.hasCoordinates(post));

        // Slightly above max longitude
        post.setLongitude(180.0001);
        assertFalse("Longitude 180.0001 should return false", MapIntentUtils.hasCoordinates(post));

        // Far out of bounds
        post.setLongitude(200.0);
        assertFalse("Longitude 200.0 should return false", MapIntentUtils.hasCoordinates(post));

        // Special Double values
        post.setLongitude(Double.NaN);
        assertFalse("NaN longitude should return false", MapIntentUtils.hasCoordinates(post));

        post.setLongitude(Double.NEGATIVE_INFINITY);
        assertFalse("Negative infinity longitude should return false", MapIntentUtils.hasCoordinates(post));
    }

    // ==========================================
    // 4. MISSING COORDINATES & NULL OBJECTS
    // ==========================================

    @Test
    public void testHasCoordinates_nullOrMissingCoordinates_returnsFalse() {
        assertFalse("Null BlogPost should return false", MapIntentUtils.hasCoordinates(null));

        BlogPost post = new BlogPost();
        assertFalse("Post with null lat/lng should return false", MapIntentUtils.hasCoordinates(post));

        post.setLatitude(35.9);
        post.setLongitude(null);
        assertFalse("Post with null longitude should return false", MapIntentUtils.hasCoordinates(post));

        post.setLatitude(null);
        post.setLongitude(14.4);
        assertFalse("Post with null latitude should return false", MapIntentUtils.hasCoordinates(post));
    }

    // ==========================================
    // 5. CAN OPEN LOGIC & EMPTY LOCATION DESCRIPTIONS
    // ==========================================

    @Test
    public void testCanOpen_returnsTrueWhenLocationCannotBeOpened() {
        // canOpen() returns true when map CANNOT open (no valid coordinates AND no location string)
        assertTrue("Null post cannot open", MapIntentUtils.canOpen(null));

        BlogPost post = new BlogPost();
        assertTrue("Empty post with null fields cannot open", MapIntentUtils.canOpen(post));

        post.setLocation("");
        assertTrue("Empty location string cannot open", MapIntentUtils.canOpen(post));

        post.setLocation("   ");
        assertTrue("Whitespace location string cannot open", MapIntentUtils.canOpen(post));
    }

    @Test
    public void testCanOpen_returnsFalseWhenLocationCanBeOpened() {
        BlogPost post = new BlogPost();

        // Valid locality text only
        post.setLocation("Mdina");
        assertFalse("Valid location string should allow opening", MapIntentUtils.canOpen(post));

        // Valid coordinates only
        post.setLocation(null);
        post.setLatitude(35.9);
        post.setLongitude(14.4);
        assertFalse("Valid coordinates should allow opening", MapIntentUtils.canOpen(post));
    }
}
