package com.riderrapp.riderr;

import android.view.View;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

//test for the view ride activity where users can view active rides, cancel and driver can start nav
public class ViewRidesActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<ViewRidesActivity> currentActivity =
            new ActivityTestRule<>(ViewRidesActivity.class);

    @Test
    public void TestStartNavigation(){
        try{
            Thread.sleep(5000);
            onView(withId(R.id.startNavButton))
                    .perform(click());
            Thread.sleep(3000);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    @Test
    public void TestStrings(){
        try{
            Thread.sleep(5000);
            onView(withId(R.id.driverText))
                    .check(matches(withText(containsString("Driver: Danesh Iqbal -- Rating: 5/5"))));
            onView(withId(R.id.timeText))
                    .check(matches(withText(containsString("Time: 18:30"))));
            onView(withId(R.id.destText))
                    .check(matches(withText(containsString("Destination: Blackburn College"))));
            onView(withId(R.id.carText))
                    .check(matches(withText(containsString("Car Details: Honda -- FF55 LMN"))));
        }catch(Exception e){
            System.out.println(e);
        }
    }

    @Test
    public void TestCancelNavigation(){
        try{
            Thread.sleep(3000);
            onView(withId(R.id.cancelRideButton))
                    .perform(click());
            Thread.sleep(3000);
        }catch(Exception e){
            System.out.println(e);
        }
    }
}