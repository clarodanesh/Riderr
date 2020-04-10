package com.riderrapp.riderr;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

//Testing for the main activity, the home of the app
public class MainActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<MainActivity> currentActivity =
            new ActivityTestRule<>(MainActivity.class);

    //Opens the intent for the offerride activity
    @Test
    public void OfferRideOpenTest(){
        onView(withId(R.id.offerRideButton))
                .perform(click());
    }

    //Opens the intent for the search ride activity
    @Test
    public void SearchRideOpenTest(){
        onView(withId(R.id.searchButton))
                .perform(click());
    }

    //Main activity has a nav drawer so need to test it opens
    @Test
    public void TestNavDrawerSlideOpen() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());
    }

    //test the find a ride link on the nav drawer
    @Test
    public void TestNavDrawerFindRide() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());

        onView(withId(R.id.navDrawerView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_far));
    }

    //test the offer a ride link on the nav drawer
    @Test
    public void TestNavDrawerOfferRide() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());

        onView(withId(R.id.navDrawerView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_oar));
    }

    //test the view rides link on the nav drawer
    @Test
    public void TestNavDrawerViewRides() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());

        onView(withId(R.id.navDrawerView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_vr));
    }

    //test the profile link on the nav drawer
    @Test
    public void TestNavDrawerProfile() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());

        onView(withId(R.id.navDrawerView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_profile));
    }

    //test the logout link on the nav drawer
    @Test
    public void TestNavDrawerHelp() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());

        onView(withId(R.id.navDrawerView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_help));
    }

    //test the logout link on the nav drawer
    @Test
    public void TestNavDrawerLogout() {
        onView(withId(R.id.mainActivityNavDrawerLayout)).perform(DrawerActions.open());

        onView(withId(R.id.navDrawerView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_logout));
    }
}