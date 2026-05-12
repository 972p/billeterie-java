package utils;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class StripeService {

    // IMPORTANT : Remplacez par votre vraie clé secrète de test Stripe
    // Elle commence généralement par "sk_test_..."
    private static final String STRIPE_SECRET_KEY = "sk_test_51TR6KHEyZGMoLgHxe1Jyre6ZiFNKw1OFrNL2cRe5KbwKAiCNdVCazBOLfCE4gZJH4816LHU7B6lVng178CIU6VYL00EQ9W0jBl";

    static {
        // Initialisation de la clé Stripe au chargement de la classe
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    /**
     * Crée une intention de paiement (PaymentIntent) pour un montant donné.
     * 
     * @param amount   Montant en centimes (ex: 1000 = 10,00 EUR)
     * @param currency Devise (ex: "eur")
     * @return Le client secret (Client Secret) à utiliser pour finaliser le
     *         paiement côté front.
     */
    public static String createPaymentIntent(long amount, String currency) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            System.out.println("✅ PaymentIntent créé avec succès : " + paymentIntent.getId());

            // Le client secret est nécessaire côté front (ex: JavaFX ou web) pour valider
            // le paiement
            return paymentIntent.getClientSecret();

        } catch (StripeException e) {
            System.err.println("❌ Erreur lors de la création du paiement Stripe : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tente d'effectuer un paiement complet (Création de PaymentMethod + PaymentIntent).
     * @param amount   Montant en euros
     * @param card     Numéro de carte
     * @param expMonth Mois d'expiration
     * @param expYear  Année d'expiration
     * @param cvc      Code de sécurité
     * @return L'identifiant de la transaction Stripe (PaymentIntent ID) si succès, null sinon
     */
    public static String processPayment(double amount, String card, String expMonth, String expYear, String cvc) {
        try {
            long amountInCents = Math.round(amount * 100);

            // Stripe interdit l'envoi direct de numéros de carte (PCI compliance).
            // En mode test, on utilise les identifiants de test officiels (pm_card_visa, etc.)
            String paymentMethodId;
            String cleanCard = card.replaceAll("\\s+", "");

            if (cleanCard.startsWith("4242")) {
                paymentMethodId = "pm_card_visa"; // Carte de test valide
            } else if (cleanCard.startsWith("4000")) {
                paymentMethodId = "pm_card_chargeCustomerFail"; // Carte de test échouée
            } else {
                paymentMethodId = "pm_card_visa"; // Par défaut, on simule une carte valide pour le test
            }

            // 2. Création et confirmation de l'intention de paiement
            PaymentIntentCreateParams piParams = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .setReturnUrl("https://example.com/return") // Obligatoire pour certaines validations
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(piParams);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                System.out.println("✅ Paiement Stripe réussi : " + paymentIntent.getId());
                return paymentIntent.getId();
            } else {
                System.out.println("⚠️ Statut du paiement : " + paymentIntent.getStatus());
                return null;
            }

        } catch (StripeException e) {
            System.err.println("❌ Erreur Stripe : " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("❌ Format de date d'expiration invalide.");
            return null;
        }
    }

    /**
     * Rembourse une transaction Stripe existante.
     * @param paymentIntentId L'identifiant de la transaction (pi_...)
     * @return true si le remboursement a réussi, false sinon
     */
    public static boolean refundPayment(String paymentIntentId) {
        try {
            com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);
            
            if ("succeeded".equals(refund.getStatus())) {
                System.out.println("✅ Remboursement Stripe réussi pour : " + paymentIntentId);
                return true;
            } else {
                System.out.println("⚠️ Statut du remboursement : " + refund.getStatus());
                return false;
            }
        } catch (StripeException e) {
            System.err.println("❌ Erreur lors du remboursement Stripe : " + e.getMessage());
            return false;
        }
    }

    // Méthode `main` pour tester rapidement sans lancer toute l'application
    public static void main(String[] args) {
        System.out.println("Test de création d'un paiement de 15.00 EUR...");

        // 1500 centimes = 15.00 euros
        String clientSecret = createPaymentIntent(1500L, "eur");

        if (clientSecret != null) {
            System.out.println("Client Secret généré : " + clientSecret);
            System.out.println(
                    "-> C'est cette chaîne qui doit être transmise à l'interface de paiement pour finaliser la transaction.");
        }
    }
}
