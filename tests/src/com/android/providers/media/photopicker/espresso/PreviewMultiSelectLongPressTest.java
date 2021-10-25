/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.providers.media.photopicker.espresso;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.android.providers.media.photopicker.espresso.RecyclerViewTestUtils.assertItemNotSelected;
import static com.android.providers.media.photopicker.espresso.RecyclerViewTestUtils.assertItemSelected;
import static com.android.providers.media.photopicker.espresso.RecyclerViewTestUtils.longClickItem;

import static org.hamcrest.Matchers.not;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.android.providers.media.R;
import com.android.providers.media.photopicker.viewmodel.PickerViewModel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class PreviewMultiSelectLongPressTest extends PhotoPickerBaseTest {
    private static final int ICON_THUMBNAIL_ID = R.id.icon_thumbnail;

    @Rule
    public ActivityScenarioRule<PhotoPickerTestActivity> mRule
            = new ActivityScenarioRule<>(PhotoPickerBaseTest.getMultiSelectionIntent());

    @Test
    public void testPreview_multiSelect_longPress_image() {
        onView(withId(PICKER_TAB_RECYCLERVIEW_ID)).check(matches(isDisplayed()));

        // Navigate to preview
        longClickItem(PICKER_TAB_RECYCLERVIEW_ID, /* position */ 1, ICON_THUMBNAIL_ID);

        registerIdlingResourceAndWaitForIdle();

        // Verify image is previewed
        assertMultiSelectLongPressCommonLayoutMatches();
        onView(withId(R.id.preview_imageView)).check(matches(isDisplayed()));

        // Navigate back to Photo grid
        onView(withContentDescription("Navigate up")).perform(click());

        onView(withId(PICKER_TAB_RECYCLERVIEW_ID)).check(matches(isDisplayed()));
    }

    @Test
    public void testPreview_multiSelect_longPress_video() {
        onView(withId(PICKER_TAB_RECYCLERVIEW_ID)).check(matches(isDisplayed()));

        // Navigate to preview
        longClickItem(PICKER_TAB_RECYCLERVIEW_ID, /* position */ 3, ICON_THUMBNAIL_ID);

        registerIdlingResourceAndWaitForIdle();

        // Since there is no video in the video file, we get an error.
        onView(withText(android.R.string.ok)).perform(click());

        // Verify videoView is displayed
        assertMultiSelectLongPressCommonLayoutMatches();
        onView(withId(R.id.preview_videoView)).check(matches(isDisplayed()));
    }

    @Test
    public void testPreview_multiSelect_longPress_gif() {
        onView(withId(PICKER_TAB_RECYCLERVIEW_ID)).check(matches(isDisplayed()));

        // Navigate to preview
        longClickItem(PICKER_TAB_RECYCLERVIEW_ID, /* position */ 2, ICON_THUMBNAIL_ID);

        registerIdlingResourceAndWaitForIdle();

        // Verify imageView is displayed for gif preview
        assertMultiSelectLongPressCommonLayoutMatches();
        onView(withId(R.id.preview_imageView)).check(matches(isDisplayed()));
    }

    @Test
    public void testPreview_multiSelect_longPress_select() {
        onView(withId(PICKER_TAB_RECYCLERVIEW_ID)).check(matches(isDisplayed()));

        final int position = 1;
        // Navigate to preview
        longClickItem(PICKER_TAB_RECYCLERVIEW_ID, position, ICON_THUMBNAIL_ID);

        registerIdlingResourceAndWaitForIdle();

        final int selectButtonId = R.id.preview_add_or_select_button;
        // Select the item within Preview
        onView(withId(selectButtonId)).perform(click());
        // Check that button text is changed to "deselect"
        onView(withId(selectButtonId)).check(matches(withText(R.string.deselect)));

        // Navigate back to PhotoGrid and check that item is selected
        onView(withContentDescription("Navigate up")).perform(click());

        final int iconCheckId = R.id.icon_check;
        assertItemSelected(PICKER_TAB_RECYCLERVIEW_ID, position, iconCheckId);

        // Navigate to Preview and check the select button text
        longClickItem(PICKER_TAB_RECYCLERVIEW_ID, position, ICON_THUMBNAIL_ID);

        registerIdlingResourceAndWaitForIdle();

        // Check that button text is set to "deselect" and common layout matches
        assertMultiSelectLongPressCommonLayoutMatches(/* isSelected */ true);

        // Click on "Deselect" and verify text changes to "Select"
        onView(withId(selectButtonId)).perform(click());
        // Check that button text is changed to "select"
        onView(withId(selectButtonId)).check(matches(withText(R.string.select)));

        // Navigate back to Photo grid and verify the item is not selected
        onView(withContentDescription("Navigate up")).perform(click());

        assertItemNotSelected(PICKER_TAB_RECYCLERVIEW_ID, position, iconCheckId);
    }

    private void registerIdlingResourceAndWaitForIdle() {
        mRule.getScenario().onActivity((activity -> IdlingRegistry.getInstance().register(
                new ViewPager2IdlingResource(activity.findViewById(R.id.preview_viewPager)))));
        Espresso.onIdle();
    }

    private void assertMultiSelectLongPressCommonLayoutMatches() {
        assertMultiSelectLongPressCommonLayoutMatches(/* isSelected */ false);
    }

    private void assertMultiSelectLongPressCommonLayoutMatches(boolean isSelected) {
        onView(withId(R.id.preview_viewPager)).check(matches(isDisplayed()));
        onView(withId(R.id.preview_select_check_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.preview_add_or_select_button)).check(matches(isDisplayed()));
        // Verify that the text in AddOrSelect button
        if (isSelected) {
            onView(withId(R.id.preview_add_or_select_button)).check(
                    matches(withText(R.string.deselect)));
        } else {
            onView(withId(R.id.preview_add_or_select_button)).check(
                    matches(withText(R.string.select)));
        }
    }
}