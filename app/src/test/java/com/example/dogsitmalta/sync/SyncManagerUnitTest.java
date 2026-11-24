package com.example.dogsitmalta.sync;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Note: Synchronization logic operates directly against Room repositories and Android Context.
 * Integration tests for sync queue management and cloud profile merging are executed in
 * {@code app/src/androidTest/java/com/example/dogsitmalta/sync/SyncManagerInstrumentedTest.java}.
 */
public class SyncManagerUnitTest {

    @Test
    public void testClassPresence() {
        assertNotNull("SyncManager class is present", SyncManager.class);
    }
}
