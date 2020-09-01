package com.riderrapp.riderr;

import android.content.Intent;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

//Payment activity test mocking intent data and then trys payment
public class PaymentActivityTest {

    //Need to get the activity this test is for in case it is needed for context later
    @Rule
    public ActivityTestRule<PaymentActivity> currentActivity =
            new ActivityTestRule<>(PaymentActivity.class, false, false);

    @Test
    public void TestBraintreePayment() {
        Intent paymentIntent = new Intent();
        paymentIntent.putExtra(PaymentActivity.LONGITUDE, "-2.255");
        paymentIntent.putExtra(PaymentActivity.LATITUDE, "5.255");
        paymentIntent.putExtra(PaymentActivity.RATING, 2L);
        paymentIntent.putExtra(PaymentActivity.AMOUNT_OF_RATINGS, 2L);
        paymentIntent.putExtra(PaymentActivity.RIDE_ID, "MyhR9jpgaHk2JaAIlPLY");
        paymentIntent.putExtra(PaymentActivity.PRICE, "19.99");
        paymentIntent.putExtra(PaymentActivity.TIME, "12:0");
        paymentIntent.putExtra(PaymentActivity.DATE, "30/4/2020");
        paymentIntent.putExtra(PaymentActivity.DEST, "Belfast");
        currentActivity.launchActivity(paymentIntent);

        try{
            Thread.sleep(5000);
            onView(withId(R.id.payBtn))
                    .perform(click());
        }catch(Exception e){
            System.out.println(e);
        }
    }
}