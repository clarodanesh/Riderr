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

//Test for login activity will try a number of logins, incorrect and correct ones
public class LoginActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<LoginActivity> currentActivity =
            new ActivityTestRule<>(LoginActivity.class);

    //testing the login works for correct logins without any input for this
    @Test
    public void TestCorrectLogin() {
        currentActivity.getActivity().DoLogin("diqbal@uclan.ac.uk", "password");
    }

    //testing the same as before but with incorrect details
    @Test
    public void TestIncorrectLogin() {
        currentActivity.getActivity().DoLogin("diqbal@uclan.ac.uk", "incorrectPassword");
    }

    //testing filling the inputs and checking if the details match
    @Test
    public void FillAndCheckEditTexts() {
        onView(withId(R.id.emailTxtBoxLogin))
                .perform(typeText("diqbal@uclan.ac.uk"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextBoxLogin))
                .perform(typeText("password"), closeSoftKeyboard());

        onView(withId(R.id.emailTxtBoxLogin))
                .check(matches(withText(containsString("diqbal@uclan.ac.uk"))));
        onView(withId(R.id.passwordTextBoxLogin))
                .check(matches(withText(containsString("password"))));
    }

    //testing the login with spoofing input and also spoofing login action
    @Test
    public void TestCorrectLoginWithActions() {
        onView(withId(R.id.emailTxtBoxLogin))
                .perform(typeText("diqbal@uclan.ac.uk"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextBoxLogin))
                .perform(typeText("password"), closeSoftKeyboard());

        onView(withId(R.id.loginBtn))
                .perform(click());
    }

    //testing login with incorrect details too
    @Test
    public void TestIncorrectLoginWithActions() {
        onView(withId(R.id.emailTxtBoxLogin))
                .perform(typeText("diqbal@uclan.ac.uk"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextBoxLogin))
                .perform(typeText("incorrectPassword"), closeSoftKeyboard());

        onView(withId(R.id.loginBtn))
                .perform(click());
    }

    //open register button test
    @Test
    public void TestOpenRegister() {
        onView(withId(R.id.registerBtnLogin))
                .perform(click());
    }
}