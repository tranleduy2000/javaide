package com.android.tests.overlay2;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ImageView;


public class MainTest extends ActivityInstrumentationTestCase2<Main> {
    
    private final static int RED = 0xFFFF0000;
    private final static int GREEN = 0xFF00FF00;

    private ImageView mNoOverlayIV;
    private ImageView mDebugOverlayIV;
    private ImageView mBetaOverlayIV;
    private ImageView mFreeNormalOverlayIV;
    private ImageView mFreeBetaDebugOverlayIV;

    /**
     * Creates an {@link ActivityInstrumentationTestCase2} that tests the {@link Main} activity.
     */
    public MainTest() {
        super(Main.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Main a = getActivity();
        // ensure a valid handle to the activity has been returned
        assertNotNull(a);
        mNoOverlayIV = (ImageView) a.findViewById(R.id.no_overlay);
        mDebugOverlayIV = (ImageView) a.findViewById(R.id.debug_overlay);
        mBetaOverlayIV = (ImageView) a.findViewById(R.id.beta_overlay);
        mFreeNormalOverlayIV = (ImageView) a.findViewById(R.id.free_normal_overlay);
        mFreeBetaDebugOverlayIV = (ImageView) a.findViewById(R.id.free_beta_debug_overlay);
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mNoOverlayIV);
        assertNotNull(mDebugOverlayIV);
        assertNotNull(mFreeBetaDebugOverlayIV);
        assertNotNull(mFreeNormalOverlayIV);
        assertNotNull(mFreeBetaDebugOverlayIV);
    }

    public void testNoOverlay() {
        pixelLooker(mNoOverlayIV, GREEN);
    }

    public void testDebugOverlay() {
        if ("debug".equals(BuildConfig.BUILD_TYPE)) {
            pixelLooker(mDebugOverlayIV, GREEN);
        } else {
            pixelLooker(mDebugOverlayIV, RED);
        }
    }

    public void testBetaOverlay() {
        if ("beta".equals(BuildConfig.FLAVOR_releaseType)) {
            pixelLooker(mBetaOverlayIV, GREEN);
        } else {
            pixelLooker(mBetaOverlayIV, RED);
        }
    }

    public void testFreeNormalOverlay() {
        if ("freeNormal".equals(BuildConfig.FLAVOR)) {
            pixelLooker(mFreeNormalOverlayIV, GREEN);
        } else {
            pixelLooker(mFreeNormalOverlayIV, RED);
        }
    }

    public void testFreeBetaDebugOverlay() {
        if ("freeBeta".equals(BuildConfig.FLAVOR) && "debug".equals(BuildConfig.BUILD_TYPE)) {
            pixelLooker(mFreeBetaDebugOverlayIV, GREEN);
        } else {
            pixelLooker(mFreeBetaDebugOverlayIV, RED);
        }
    }
    
    private void pixelLooker(ImageView iv, int expectedColor) {
        BitmapDrawable d = (BitmapDrawable) iv.getDrawable();
        Bitmap bitmap = d.getBitmap();
        assertEquals(expectedColor, bitmap.getPixel(0, 0));
    }
}

