package com.fxn.utility.ui;

import com.fxn.pix.Pix;
import com.fxn.pix.PixSupport;

public interface FastScrollStateChangeListenerSupport {

    /**
     * Called when fast scrolling begins
     */
    void onFastScrollStart(PixSupport fastScroller);

    /**
     * Called when fast scrolling ends
     */
    void onFastScrollStop(PixSupport fastScroller);
}
