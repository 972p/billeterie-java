import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;

public class test_stripe {
    public static void main(String[] args) {
        Stripe.apiKey = "sk_test_51TR6KHEyZGMoLgHxe1Jyre6ZiFNKw1OFrNL2cRe5KbwKAiCNdVCazBOLfCE4gZJH4816LHU7B6lVng178CIU6VYL00EQ9W0jBl";
        try {
            PaymentMethodCreateParams pmParams = PaymentMethodCreateParams.builder()
                    .setType(PaymentMethodCreateParams.Type.CARD)
                    .setCard(PaymentMethodCreateParams.CardDetails.builder()
                            .setNumber("4242424242424242")
                            .setExpMonth(12L)
                            .setExpYear(2026L)
                            .setCvc("123")
                            .build())
                    .build();

            PaymentMethod paymentMethod = PaymentMethod.create(pmParams);

            PaymentIntentCreateParams piParams = PaymentIntentCreateParams.builder()
                    .setAmount(1500L)
                    .setCurrency("eur")
                    .setPaymentMethod(paymentMethod.getId())
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
