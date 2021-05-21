package uk.co.oliverdelange.bugs;

import android.util.Log;

/** Testing whether the issue is caused by mocking the real System
 * https://github.com/mockk/mockk/issues/136 - Suggests mockkStatic should work for Java static methods */
public class FakeSystem {
    public static void loadLibrary(String libname) {
        Log.w("BUGS", "Fake loading library " + libname);
        throw new RuntimeException("Imagine we want to run tests without the rust binary");
    }
}
