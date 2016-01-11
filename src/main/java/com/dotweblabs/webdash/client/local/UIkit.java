package com.divroll.webdash.client.local;

public class UIkit {
    public static native void notify(String message)/*-{
        $wnd.UIkit.notify(message, {pos:'top-right'});
    }-*/;
}
