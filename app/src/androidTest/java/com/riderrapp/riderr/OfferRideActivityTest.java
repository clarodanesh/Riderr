package com.riderrapp.riderr;

import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;

//offer ride test, need to test offering a ride, mocking offer ride details
public class OfferRideActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<OfferRideActivity> currentActivity =
            new ActivityTestRule<>(OfferRideActivity.class);

    //testing offering a ride with the correct details
    @Test
    public void TestCorrectData(){
        currentActivity.getActivity().destination = "UCLan Preston Sports Arena";

        onView(withId(R.id.offerRideTxtBox))
                .perform(InjectText("UCLan Preston Sports Arena"));

        onView(withId(R.id.offerRideTimeBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())));
        SetCorrectTime();

        onView(withId(R.id.offerRideDateBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())));
        SetCorrectDate();

        onView(withId(R.id.offerRideBtn))
                .perform(click());
    }

    //testing offering a ride feature with a missing text input
    @Test
    public void TestIncorrectTextVal(){
        currentActivity.getActivity().destination = "";

        onView(withId(R.id.offerRideTimeBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())));
        SetCorrectTime();

        onView(withId(R.id.offerRideDateBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())));
        SetCorrectDate();

        onView(withId(R.id.offerRideBtn))
                .perform(click());
    }

    //testing offering a ride feature with a date from the past
    @Test
    public void TestIncorrectDate(){
        currentActivity.getActivity().destination = "UCLan Preston Sports Arena";

        onView(withId(R.id.offerRideTxtBox))
                .perform(InjectText("UCLan Preston Sports Arena"));

        onView(withId(R.id.offerRideTimeBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())));
        SetCorrectTime();

        onView(withId(R.id.offerRideDateBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())));
        IncorrectDate();

        onView(withId(R.id.offerRideBtn))
                .perform(click());
    }

    //testing offering ride feature with a time from the past
    //time used for testing is 7 am in the morning
    @Test
    public void TestIncorrectTime(){
        currentActivity.getActivity().destination = "UCLan Preston Sports Arena";

        onView(withId(R.id.offerRideTxtBox))
                .perform(InjectText("UCLan Preston Sports Arena"));

        onView(withId(R.id.offerRideTimeBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())));
        IncorrectTime();

        onView(withId(R.id.offerRideDateBtn))
                .perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())));
        SetCorrectDate();

        onView(withId(R.id.offerRideBtn))
                .perform(click());
    }

    //Need to test the time picker selecting a time in the future
    public void SetCorrectTime() {
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(12, 00));
        onView(withId(android.R.id.button1)).perform(click());
    }

    //need to test the time picker selecting a time in the past
    public void IncorrectTime() {
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(7, 00));
        onView(withId(android.R.id.button1)).perform(click());
    }

    //this makes the datepicker select a date in the future
    public void SetCorrectDate(){
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2021, 1, 1));
        onView(withId(android.R.id.button1)).perform(click());
    }

    //this function will use the date picker to select an incorrect date
    public void IncorrectDate(){
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2019, 1, 1));
        onView(withId(android.R.id.button1)).perform(click());
    }

    //https://stackoverflow.com/a/47216960/9906308
    //This stack overflow answer was used to help inject the text into the text view
    //need to use a ViewAction to inject text into the editText for testing the destination input
    public static ViewAction InjectText(final String val){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TextView.class));
            }

            @Override
            public String getDescription() {
                return "get desc needs to be overrided so Im adding this placeholer since it needs to be returned anyway";
            }

            @Override
            public void perform(UiController uic, View v) {
                ((TextView) v).setText(val);
            }
        };
    }
}