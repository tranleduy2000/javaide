/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.common.rendering.api;

import static com.android.ide.common.rendering.api.Result.Status.NOT_IMPLEMENTED;

import com.android.ide.common.rendering.api.Result.Status;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * An object allowing interaction with an Android layout.
 *
 * This is returned by {@link Bridge#createSession(SessionParams)}.
 * and can then be used for subsequent actions on the layout.
 *
 * @since 5
 *
 */
public class RenderSession {

    /**
     * Returns the last operation result.
     */
    public Result getResult() {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Returns the {@link ViewInfo} objects for the top level views.
     * <p/>
     * It contains {@code ViewInfo} for only the views in the layout. For {@code ViewInfo} of the
     * System UI surrounding the layout use {@link #getSystemRootViews()}. In most cases the list
     * will only contain one item. If the top level node is a {@code merge} though then it will
     * contain all the items under the {@code merge} tag.
     * <p/>
     * This is reset to a new instance every time {@link #render()} is called and can be
     * <code>null</code> if the call failed (and the method returned a {@link Result} with
     * {@link Status#ERROR_UNKNOWN} or {@link Status#NOT_IMPLEMENTED}.
     * <p/>
     * This can be safely modified by the caller, but {@code #getSystemRootViews} and
     * {@code #getRootViews} share some view infos, so modifying one result can affect the other.
     *
     * @return the list of {@link ViewInfo} or null if there aren't any.
     *
     * @see #getSystemRootViews()
     */
    public List<ViewInfo> getRootViews() {
        return null;
    }

    /**
     * Returns the {@link ViewInfo} objects for the system decor views, like the ActionBar.
     * <p/>
     * This is reset to a new instance every time {@link #render()} is called and can be
     * <code>null</code> if the call failed, or there was no system decor.
     * <p/>
     * This can be safely modified by the caller, but {@code #getSystemRootViews} and
     * {@code #getRootViews} share some view infos, so modifying one result can affect the other.
     *
     * @return the list of {@link ViewInfo} or null if there aren't any.
     */
    public List<ViewInfo> getSystemRootViews() {
        return null;
    }

    /**
     * Returns the rendering of the full layout.
     * <p>
     * This is reset to a new instance every time {@link #render()} is called and can be
     * <code>null</code> if the call failed (and the method returned a {@link Result} with
     * {@link Status#ERROR_UNKNOWN} or {@link Status#NOT_IMPLEMENTED}.
     * <p/>
     * This can be safely modified by the caller.
     */
    public BufferedImage getImage() {
        return null;
    }

    /**
     * Returns true if the current image alpha channel is relevant.
     *
     * @return whether the image alpha channel is relevant.
     */
    public boolean isAlphaChannelImage() {
        return true;
    }

    /**
     * Returns a map of (XML attribute name, attribute value) containing only default attribute
     * values, for the given view Object.
     * @param viewObject the view object.
     * @return a map of the default property values or null.
     */
    public Map<String, String> getDefaultProperties(Object viewObject) {
        return null;
    }

    /**
     * Re-renders the layout as-is.
     * In case of success, this should be followed by calls to {@link #getRootViews()} and
     * {@link #getImage()} to access the result of the rendering.
     *
     * This is equivalent to calling <code>render(SceneParams.DEFAULT_TIMEOUT)</code>
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result render() {
        return render(RenderParams.DEFAULT_TIMEOUT);
    }

    /**
     * Re-renders the layout as-is, with a given timeout in case other renderings are being done.
     * In case of success, this should be followed by calls to {@link #getRootViews()} and
     * {@link #getImage()} to access the result of the rendering.
     *
     * The {@link Bridge} is only able to inflate or render one layout at a time. There
     * is an internal lock object whenever such an action occurs. The timeout parameter is used
     * when attempting to acquire the lock. If the timeout expires, the method will return
     * {@link Status#ERROR_TIMEOUT}.
     *
     * @param timeout timeout for the rendering, in milliseconds.
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result render(long timeout) {
        return render(timeout, false);
    }

    /**
     * Re-renders the layout as-is, with a given timeout in case other renderings are being done.
     * In case of success, this should be followed by calls to {@link #getRootViews()} and
     * {@link #getImage()} to access the result of the rendering.
     * This call also allows triggering a forced measure.
     *
     * The {@link Bridge} is only able to inflate or render one layout at a time. There
     * is an internal lock object whenever such an action occurs. The timeout parameter is used
     * when attempting to acquire the lock. If the timeout expires, the method will return
     * {@link Status#ERROR_TIMEOUT}.
     *
     * @param timeout timeout for the rendering, in milliseconds.
     * @param forceMeasure force running measure for the layout.
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result render(long timeout, boolean forceMeasure) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Sets the value of a given property on a given object.
     * <p/>
     * This does nothing more than change the property. To render the scene in its new state, a
     * call to {@link #render()} is required.
     * <p/>
     * Any amount of actions can be taken on the scene before {@link #render()} is called.
     *
     * @param objectView
     * @param propertyName
     * @param propertyValue
     *
     * @return a {@link Result} indicating the status of the action.
     *
     * @throws IllegalArgumentException if the view object is not an android.view.View
     */
    public Result setProperty(Object objectView, String propertyName, String propertyValue) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * returns the value of a given property on a given object.
     * <p/>
     * This returns a {@link Result} object. If the operation of querying the object for its
     * property was successful (check {@link Result#isSuccess()}), then the property value
     * is set in the result and can be accessed through {@link Result#getData()}.
     *
     * @param objectView
     * @param propertyName
     *
     * @return a {@link Result} indicating the status of the action.
     *
     * @throws IllegalArgumentException if the view object is not an android.view.View
     */
    public Result getProperty(Object objectView, String propertyName) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Inserts a new child in a ViewGroup object, and renders the result.
     * <p/>
     * The child is first inflated and then added to its new parent, at the given <var>index<var>
     * position. If the <var>index</var> is -1 then the child is added at the end of the parent.
     * <p/>
     * If an animation listener is passed then the rendering is done asynchronously and the
     * result is sent to the listener.
     * If the listener is null, then the rendering is done synchronously.
     * <p/>
     * The child stays in the view hierarchy after the rendering is done. To remove it call
     * {@link #removeChild(Object, IAnimationListener)}
     * <p/>
     * The returned {@link Result} object will contain the android.view.View object for
     * the newly inflated child. It is accessible through {@link Result#getData()}.
     *
     * @param parentView the parent View object to receive the new child.
     * @param childXml an {@link ILayoutPullParser} containing the content of the new child,
     *             including ViewGroup.LayoutParams attributes.
     * @param index the index at which position to add the new child into the parent. -1 means at
     *             the end.
     * @param listener an optional {@link IAnimationListener}.
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result insertChild(Object parentView, ILayoutPullParser childXml, int index,
            IAnimationListener listener) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Move a new child to a different ViewGroup object.
     * <p/>
     * The child is first removed from its current parent, and then added to its new parent, at the
     * given <var>index<var> position. In case the <var>parentView</var> is the current parent of
     * <var>childView</var> then the index must be the value with the <var>childView</var> removed
     * from its parent. If the <var>index</var> is -1 then the child is added at the end of
     * the parent.
     * <p/>
     * If an animation listener is passed then the rendering is done asynchronously and the
     * result is sent to the listener.
     * If the listener is null, then the rendering is done synchronously.
     * <p/>
     * The child stays in the view hierarchy after the rendering is done. To remove it call
     * {@link #removeChild(Object, IAnimationListener)}
     * <p/>
     * The returned {@link Result} object will contain the android.view.ViewGroup.LayoutParams
     * object created from the <var>layoutParams</var> map if it was non <code>null</code>.
     *
     * @param parentView the parent View object to receive the child. Can be the current parent
     *             already.
     * @param childView the view to move.
     * @param index the index at which position to add the new child into the parent. -1 means at
     *             the end.
     * @param layoutParams an optional map of new ViewGroup.LayoutParams attribute. If non null,
     *             then the current layout params of the view will be removed and a new one will
     *             be inflated and set with the content of the map.
     * @param listener an optional {@link IAnimationListener}.
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result moveChild(Object parentView, Object childView, int index,
            Map<String, String> layoutParams, IAnimationListener listener) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Removes a child from a ViewGroup object.
     * <p/>
     * This does nothing more than change the layout. To render the scene in its new state, a
     * call to {@link #render()} is required.
     * <p/>
     * Any amount of actions can be taken on the scene before {@link #render()} is called.
     *
     * @param childView the view object to remove from its parent
     * @param listener an optional {@link IAnimationListener}.
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result removeChild(Object childView, IAnimationListener listener) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Starts playing an given animation on a given object.
     * <p/>
     * The animation playback is asynchronous and the rendered frame is sent vi the
     * <var>listener</var>.
     *
     * @param targetObject the view object to animate
     * @param animationName the name of the animation (res/anim) to play.
     * @param listener the listener callback.
     *
     * @return a {@link Result} indicating the status of the action.
     */
    public Result animate(Object targetObject, String animationName,
            boolean isFrameworkAnimation, IAnimationListener listener) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Discards the layout. No more actions can be called on this object.
     */
    public void dispose() {
    }
}
