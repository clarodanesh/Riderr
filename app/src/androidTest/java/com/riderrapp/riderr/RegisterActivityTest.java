package com.riderrapp.riderr;

import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

//register acivity test will create a user with correct and then try to register with incorrect details
public class RegisterActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<RegisterActivity> currentActivity =
            new ActivityTestRule<>(RegisterActivity.class);

    //filling the inputs to see if the input matches the correct details
    @Test
    public void FillAndCheckEditTexts() {
        onView(withId(R.id.emailTxtBoxRegister))
                .perform(typeText("diqbal@uclan.ac.uk"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextBoxRegister))
                .perform(typeText("password"), closeSoftKeyboard());

        onView(withId(R.id.emailTxtBoxRegister))
                .check(matches(withText(containsString("diqbal@uclan.ac.uk"))));
        onView(withId(R.id.passwordTextBoxRegister))
                .check(matches(withText(containsString("password"))));
    }

    //testing the registration with correct details first
    @Test
    public void TestCorrectRegisterWithActions() {
        onView(withId(R.id.emailTxtBoxRegister))
                .perform(typeText("diqbal@uclan.ac.uk"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextBoxRegister))
                .perform(typeText("password"), closeSoftKeyboard());

        onView(withId(R.id.RegisterBtnRegister))
                .perform(click());
    }

    //testing registration with incorrect details
    @Test
    public void TestIncorrectRegisterWithActions() {
        onView(withId(R.id.emailTxtBoxRegister))
                .perform(typeText("diqbal@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextBoxRegister))
                .perform(typeText("password"), closeSoftKeyboard());

        onView(withId(R.id.RegisterBtnRegister))
                .perform(click());
    }

    //testing the opening of the login activity from here
    @Test
    public void TestOpenLogin() {
        onView(withId(R.id.loginBtnRegister))
                .perform(click());
    }
}