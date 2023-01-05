import BaseClass from "../util/baseClass";
import DataStore from "../util/DataStore";
import OrderServiceClient from "../api/orderServiceClient";
import CartServiceClient from "../api/cartServiceClient";

export default class SuccessPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'checkStatus', 'renderOrderSummary'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.orderServiceClient = new OrderServiceClient();
        this.cartServiceClient = new CartServiceClient();
        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);
        // This is your test publishable API key.
        this.stripe = Stripe(stripePublicKey);
        // The items the customer wants to buy
        this.dataStore.addChangeListener(this.renderOrderSummary);

        this.checkStatus();
    }

    async renderOrderSummary() {
        const order = this.dataStore.get("order");
        let billingAddress = '';
        let shippingAddress = '';
        if (order.billingInfo.addressSecondLine) {
            billingAddress = `${order.billingInfo.addressFirstLine}, ${order.billingInfo.addressSecondLine}, ${order.billingInfo.city}, ${order.billingInfo.state}, ${order.billingInfo.zipCode}, USA`
        } else {
            billingAddress = `${order.billingInfo.addressFirstLine}, ${order.billingInfo.city}, ${order.billingInfo.state}, ${order.billingInfo.zipCode}, USA`
        }
        if (order.shippingInfo.addressSecondLine) {
            shippingAddress = `${order.shippingInfo.addressFirstLine}, ${order.shippingInfo.addressSecondLine}, ${order.shippingInfo.city}, ${order.shippingInfo.state}, ${order.shippingInfo.zipCode}, USA`
        } else {
            shippingAddress = `${order.shippingInfo.addressFirstLine}, ${order.shippingInfo.city}, ${order.shippingInfo.state}, ${order.shippingInfo.zipCode}, USA`
        }
        const thankYouContainer = document.querySelector('.thank-you-container');
        thankYouContainer.innerHTML =
            `
            <h1 style="font-size: xxx-large" xmlns="http://www.w3.org/1999/html">Thank you for your order!</h1>
            <h1>Order summary</h1>
            <div class="order-summary">
                <div>
                    <span>Order no. <a href="/order/${order.id}">${order.id}</a></span>
                </div>
                <div class="order-summary-billing">
                    <h3>Billing info:</h3>
                    <p>${order.billingInfo.firstName} ${order.billingInfo.lastName}</p>
                    <p>${billingAddress}</p>
                </div>
                <div class="order-summary-shipping">
                    <h3>Shipping info:</h3>
                    <p>${order.shippingInfo.firstName} ${order.shippingInfo.lastName}</p>
                    <p>${shippingAddress}</p>
                </div>
                <p>Items (${order.orderItems.length})</p>
                <div class="order-summary-cost-breakdown" style="width: 50%;">
                    <div class="order-summary-cost">
                        <span>Subtotal</span><span>${this.formatCurrency(order.subtotal)}</span>
                    </div>
                    <div class="order-summary-cost">
                        <span>Tax</span><span>${this.formatCurrency(order.tax)}</span>
                    </div>
                    <div class="order-summary-cost">
                        <span>Shipping</span><span>${this.formatCurrency(order.shippingCost)}</span>
                    </div>
                    <hr>
                    <div class="order-summary-cost">
                        <span>Total</span><span>${this.formatCurrency(order.total)}</span>
                    </div>
                </div>
            </div>
            `;
    }


    // Fetches the payment intent status after payment submission
    async checkStatus() {
        const clientSecret = new URLSearchParams(window.location.search).get(
            "payment_intent_client_secret"
        );

        if (!clientSecret) {
            return;
        }
        const paymentIntentId = new URLSearchParams(window.location.search).get(
            "payment_intent"
        );
        window.history.replaceState({}, document.title, "/success");
        const { paymentIntent } = await this.stripe.retrievePaymentIntent(clientSecret);
        console.log(paymentIntent);
        switch (paymentIntent.status) {
            case "succeeded":
                let result = await this.orderServiceClient.getOrderByPaymentId(userId, paymentIntentId, this.errorHandler);
                if (result) {
                    await this.cartServiceClient.clearCart(result.userId, this.errorHandler);
                    this.orderServiceClient.updateOrder(result.id, result.userId, result.shippingInfo, result.billingInfo, "PROCESSING", result.orderItems, this.errorHandler);
                    this.dataStore.set("order", result);
                }
                this.showMessage("Payment succeeded!");
                break;
            case "processing":
                this.showMessage("Your payment is processing.");
                break;
            case "requires_payment_method":
                this.showMessage("Your payment was not successful, please try again.");
                break;
            default:
                this.showMessage("Something went wrong.");
                break;
        }
    }

}
/**
 * Main method to run when the page contents have loaded.
 */
const main = async () => {
    const successPage = new SuccessPage();
    await successPage.mount();
};

window.addEventListener('DOMContentLoaded', main);