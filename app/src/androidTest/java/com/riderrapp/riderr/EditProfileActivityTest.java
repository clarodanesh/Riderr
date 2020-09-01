package com.riderrapp.riderr;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

//Edit profile activity tests, will edit the whole profile and then try to save it aswell as update the location
@RunWith(AndroidJUnit4.class)
public class EditProfileActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<EditProfileActivity> currentActivity =
            new ActivityTestRule<>(EditProfileActivity.class);

    //Testing the location update feature as part of editing the details
    @Test
    public void UpdateLocationTest(){
        onView(withId(R.id.updateLocationBtn))
                .perform(click());
    }

    //testing all the inputs on the edit profile page and seeing if they hold the correct values
    @Test
    public void MakeChangesAndCheckIfStringTest(){
        onView(withId(R.id.editCarMake))
                .perform(typeText("Honda"), closeSoftKeyboard());
        onView(withId(R.id.editCarReg))
                .perform(typeText("FF55 LMN"), closeSoftKeyboard());
        onView(withId(R.id.editCarSeats))
                .perform(typeText("3"), closeSoftKeyboard());
        onView(withId(R.id.editPrice))
                .perform(typeText("15"), closeSoftKeyboard());

        onView(withId(R.id.editCarMake))
                .check(matches(withText(containsString("Honda"))));
        onView(withId(R.id.editCarReg))
                .check(matches(withText(containsString("FF55 LMN"))));
        onView(withId(R.id.editCarSeats))
                .check(matches(withText(containsString("3"))));
        onView(withId(R.id.editPrice))
                .check(matches(withText(containsString("15"))));
    }

    //Testing the same as before but this time saving the changes to test if changes save
    @Test
    public void MakeThenSaveProfileChangesTest(){
        onView(withId(R.id.editCarMake))
                .perform(typeText("Honda"), closeSoftKeyboard());
        onView(withId(R.id.editCarReg))
                .perform(typeText("FF55 LMN"), closeSoftKeyboard());
        onView(withId(R.id.editCarSeats))
                .perform(typeText("3"), closeSoftKeyboard());
        onView(withId(R.id.editPrice))
                .perform(typeText("15"), closeSoftKeyboard());

        onView(withId(R.id.SaveProfileChangesBtn))
                .perform(click());
    }
}