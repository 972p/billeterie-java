import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class test_stripe2 {
    public static void main(String[] args) {
        Stripe.apiKey = "sk_test_51TR6KHEyZGMoLgHxe1Jyre6ZiFNKw1OFrNL2cRe5KbwKAiCNdVCazBOLfCE4gZJH4816LHU7B6lVng178CIU6VYL00EQ9W0jBl";
        try {
            PaymentIntentCreateParams piParams = PaymentIntentCreateParams.builder()
                    .setAmount(1500L)
                    .setCurrency("eur")
                    .setPaymentMethod("pm_card_visa") // Use a test payment method directly
                    .setConfirm(true)
                    .setReturnUrl("https://example.com/return")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(piParams);
            System.out.println("Status: " + paymentIntent.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
