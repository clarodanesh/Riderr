package com.riderrapp.riderr;

import android.content.Intent;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

//test for the found rides activity called as an intent from the search activity
//so because of this need to mock some intent data
public class FoundRidesActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<FoundRidesActivity> currentActivity =
            new ActivityTestRule<>(FoundRidesActivity.class, false, false);

    //Need to test joining a ride, this activity is opened using an intent with data from the previous activity
    //so for this reason need to mock the intent data and then open the activity
    @Test
    public void TestJoiningARide() {
        Intent foundRidesIntent = new Intent();
        foundRidesIntent.putExtra(FoundRidesActivity.SEARCH_PLACE, "Belfast");
        foundRidesIntent.putExtra(FoundRidesActivity.SEARCH_DATE, "30/4/2020");
        foundRidesIntent.putExtra(FoundRidesActivity.SEARCH_TIME, "18:30");
        currentActivity.launchActivity(foundRidesIntent);

        try{
            Thread.sleep(5000);
            onView(withId(R.id.joinRideBtn))
                    .perform(click());
        }catch(Exception e){
            System.out.println(e);
        }
    }
}