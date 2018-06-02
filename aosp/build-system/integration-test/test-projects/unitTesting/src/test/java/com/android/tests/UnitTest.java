package com.android.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.SyncResult;
import android.content.SyncStats;
import android.util.ArrayMap;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.PowerManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.io.InputStream;
import java.net.URL;

public class UnitTest {
    @Test
    public void referenceProductionCode() {
        // Reference production code:
        Foo foo = new Foo();
        assertEquals("production code", foo.foo());
    }

    @Test
    public void mockFinalMethod() {
        Activity activity = mock(Activity.class);
        Application app = mock(Application.class);
        when(activity.getApplication()).thenReturn(app);

        assertSame(app, activity.getApplication());

        verify(activity).getApplication();
        verifyNoMoreInteractions(activity);
    }

    @Test
    public void mockFinalClass() {
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        when(adapter.isEnabled()).thenReturn(true);

        assertTrue(adapter.isEnabled());

        verify(adapter).isEnabled();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void mockInnerClass() throws Exception {
        PowerManager.WakeLock wakeLock = mock(PowerManager.WakeLock.class);
        when(wakeLock.isHeld()).thenReturn(true);
        assertTrue(wakeLock.isHeld());
    }

    @Test
    public void aarDependencies() throws Exception {
        org.jdeferred.Deferred<Integer, Integer, Integer> deferred =
                new org.jdeferred.impl.DeferredObject<Integer, Integer, Integer>();
        org.jdeferred.Promise promise = deferred.promise();
        deferred.resolve(42);
        assertTrue(promise.isResolved());
    }

    @Test
    public void exceptions() {
        try {
            ArrayMap map = new ArrayMap();
            map.isEmpty();
            fail();
        } catch (RuntimeException e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertTrue(e.getMessage().contains("isEmpty"));
            assertTrue(e.getMessage().contains("not mocked"));
            assertTrue(e.getMessage().contains("androidstudio/not-mocked"));
        }

        try {
            Debug.getThreadAllocCount();
            fail();
        } catch (RuntimeException e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertTrue(e.getMessage().contains("getThreadAllocCount"));
            assertTrue(e.getMessage().contains("not mocked"));
            assertTrue(e.getMessage().contains("androidstudio/not-mocked"));
        }

    }

    @Test
    public void enums() throws Exception {
        assertNotNull(AsyncTask.Status.RUNNING);
        assertNotEquals(AsyncTask.Status.RUNNING, AsyncTask.Status.FINISHED);

        assertEquals(AsyncTask.Status.FINISHED, AsyncTask.Status.valueOf("FINISHED"));
        assertEquals(1, AsyncTask.Status.PENDING.ordinal());
        assertEquals("RUNNING", AsyncTask.Status.RUNNING.name());

        assertEquals(AsyncTask.Status.RUNNING, Enum.valueOf(AsyncTask.Status.class, "RUNNING"));

        AsyncTask.Status[] values = AsyncTask.Status.values();
        assertEquals(3, values.length);
        assertEquals(AsyncTask.Status.FINISHED, values[0]);
        assertEquals(AsyncTask.Status.PENDING, values[1]);
        assertEquals(AsyncTask.Status.RUNNING, values[2]);
    }

    @Test
    public void instanceFields() throws Exception {
        SyncResult result = mock(SyncResult.class);
        Field statsField = result.getClass().getField("stats");
        SyncStats syncStats = mock(SyncStats.class);
        statsField.set(result, syncStats);

        syncStats.numDeletes = 42;
        assertEquals(42, result.stats.numDeletes);
    }

    @Test
    public void javaResourcesOnClasspath() throws Exception {
        URL url = UnitTest.class.getClassLoader().getResource("resource_file.txt");
        assertNotNull("expected resource_file.txt to be in the ClassLoader's resources", url);

        InputStream stream = UnitTest.class.getClassLoader().getResourceAsStream("resource_file.txt");
        assertNotNull("expected resource_file.txt to be opened as a stream", stream);
        byte[] line = new byte[1024];
        assertTrue("Expected >0 bytes read from input stream", stream.read(line) > 0);
        String s = new String(line, "UTF-8").trim();
        assertEquals("success", s);
    }

    @Test
    public void prodJavaResourcesOnClasspath() throws Exception {
        URL url = UnitTest.class.getClassLoader().getResource("prod_resource_file.txt");
        assertNotNull("expected resource_file.txt to be in the ClassLoader's resources", url);

        InputStream stream = UnitTest.class.getClassLoader().getResourceAsStream("prod_resource_file.txt");
        assertNotNull("expected resource_file.txt to be opened as a stream", stream);
        byte[] line = new byte[1024];
        assertTrue("Expected >0 bytes read from input stream", stream.read(line) > 0);
        String s = new String(line, "UTF-8").trim();
        assertEquals("prod", s);
    }

    @Test
    public void prodRClass() {
        int id = R.string.app_name;
        assertTrue(id > 0);
    }

    @Test
    @Ignore
    public void thisIsIgnored() {
        // Just excercise more JUnit features.
    }

    @Test
    public void taskConfiguration() {
        // This property is set in build.gradle:
        assertEquals("bar", System.getProperty("foo"));
    }

    @Test
    public void commonsLogging() {
        Log log = LogFactory.getLog(getClass());
        log.info("I can use commons-logging!");
    }
}
