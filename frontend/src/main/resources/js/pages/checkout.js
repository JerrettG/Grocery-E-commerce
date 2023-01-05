import BaseClass from "../util/baseClass";
import DataStore from "../util/DataStore";
import CartServiceClient from "../api/cartServiceClient";
import OrderServiceClient from "../api/orderServiceClient";
import CustomerProfileServiceClient from "../api/customerProfileServiceClient";

export default class CheckoutPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'initialize','getCookie',
            'handleOrderSubmit', 'showMessage', 'setLoading', 'handleShippingSubmit',
            'handleBillingSubmit', 'changeShipping', 'changeBilling', 'toggleSameAsShipping'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.cartServiceClient = new CartServiceClient();
        this.orderServiceClient = new OrderServiceClient();
        this.profileServiceClient = new CustomerProfileServiceClient();
        this.SHIPPING_FLAT_RATE = 7.99;
        // This is your test publishable API key.
        this.stripe = Stripe(stripePublicKey);
        // The items the customer wants to buy
        let result = await this.cartServiceClient.getCartForUserId(userId, this.errorHandler);
        if (result && result.cartItems.length > 0) {
            this.items = result.cartItems;
            this.subtotal = result.subtotal;
        } else {
            window.location.href = "/";
        }
        let numItems = this.items.reduce((total, item) => total + item.quantity, 0);

        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);

        document.querySelectorAll('.checkout-form input')
            .forEach(input => {
                input.addEventListener("invalid", function () {input.classList.add("invalidInput");});
                input.addEventListener("input", function () {input.classList.remove("invalidInput")});
            });
        document.getElementById("same-as-shipping").addEventListener("change", this.toggleSameAsShipping);
        document.getElementById("shipping-form").addEventListener("submit", this.handleShippingSubmit);
        document.getElementById("billing-form").addEventListener("submit", this.handleBillingSubmit);
        document
            .querySelector("#payment-form")
            .addEventListener("submit", this.handleOrderSubmit);
        document.getElementById('order-summary-item-count').innerHTML = `Items (${numItems})`;

        this.dataStore.addChangeListener(this.initialize);
    }

     getCookie(name) {
        let cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = cookies[i].trim();
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
// Fetches a payment intent and captures the client secret
    async initialize() {

        const shippingInfo = this.dataStore.get("shippingInfo");
        let billingInfo = this.dataStore.get("billingInfo");
        const sameAsShipping = this.dataStore.get('sameAsShipping');
        if (sameAsShipping) {
            billingInfo = shippingInfo;
        }
        if (shippingInfo && billingInfo) {

            let status = "AWAITING_PAYMENT";
            let subtotal = this.subtotal;
            let orderItems = [];
            for (let item of this.items) {
                orderItems.push(
                    {
                        itemName: item.productName,
                        imageUrl: item.productImageUrl,
                        quantity: item.quantity,
                        unitPrice: item.productPrice
                    }
                )
            }
            let taxRate = 0.0775;
            let tax = Number((subtotal * taxRate).toFixed(2));
            let shippingCost = Number(this.SHIPPING_FLAT_RATE.toFixed(2));
            let total = subtotal + tax + shippingCost;

            const response = await fetch("/payment/create-payment-intent",
                {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                            shippingInfo: shippingInfo,
                            userId: userId,
                            total: total
                        }
                    ),
                });
            const jsonResponse = await response.json();
            const {clientSecret} = jsonResponse;
            const {paymentIntentId} = jsonResponse;

            await this.orderServiceClient.createOrder(userId, paymentIntentId, orderItems, subtotal, shippingCost, total, status, shippingInfo, billingInfo, this.errorHandler);

            const appearance = {
                theme: 'stripe',
            };
            this.elements = this.stripe.elements({appearance, clientSecret});

            const paymentElement = this.elements.create("payment");
            paymentElement.mount("#payment-element");

            document.getElementById("checkout-subtotal").innerHTML = this.formatCurrency(subtotal);
            document.getElementById("checkout-shipping").innerHTML = this.formatCurrency(shippingCost);
            document.getElementById("checkout-tax").innerHTML = this.formatCurrency(tax);
            document.getElementById("checkout-total").innerHTML = this.formatCurrency(total);
        }
    }


    async handleOrderSubmit(e) {
        e.preventDefault();
        this.setLoading(true);

        let elements = this.elements;

        const { error } = await this.stripe.confirmPayment({
            elements,
            confirmParams: {
                // Make sure to change this to your payment completion page
                return_url: "http://localhost:8084/success",
            },
        });

        // This point will only be reached if there is an immediate error when
        // confirming the payment. Otherwise, your customer will be redirected to
        // your `return_url`. For some payment methods like iDEAL, your customer will
        // be redirected to an intermediate site first to authorize the payment, then
        // redirected to the `return_url`.
        if (error.type === "card_error" || error.type === "validation_error") {
            this.showMessage(error.message);
        } else {
            this.showMessage("An unexpected error occurred.");
        }
        // await this.orderServiceClient.createOrder(userId, )
        this.setLoading(false);
    }
    async changeShipping(event) {
        document.getElementById('shipping-form').classList.remove('hidden');
        document.getElementById('change-shipping-info').classList.add('hidden');
        document.getElementById('shipping-info-header').classList.remove('inline');
    }
    async changeBilling(event) {
        document.getElementById('billing-form').classList.remove('hidden');
        document.getElementById('change-billing-info').classList.add('hidden');
        document.getElementById('billing-info-header').classList.remove('inline');
    }

    async toggleSameAsShipping(event) {
        const checkbox = event.srcElement;
        document.getElementById('billing-collapsible').disabled = checkbox.checked;

    }

    async handleBillingSubmit(event) {
        event.preventDefault();

        let billingForm = document.getElementById("billing-form");
        let billingFormData = new FormData(billingForm);
        let sameAsShipping = billingFormData.get('sameAsShipping');

        if (sameAsShipping) {
            this.dataStore.set("sameAsShipping", true);
        } else {
            this.dataStore.set("sameAsShipping", false);
            let billingInfo = {
                firstName: billingFormData.get('firstName'),
                lastName: billingFormData.get('lastName'),
                addressFirstLine: billingFormData.get('addressFirstLine'),
                addressSecondLine: billingFormData.get('addressSecondLine'),
                city: billingFormData.get('city'),
                state: billingFormData.get('state'),
                zipCode: billingFormData.get('zipCode')
            }
            this.dataStore.set("billingInfo", billingInfo);
        }
        document.getElementById('billing-info-header').classList.add('inline');
        billingForm.classList.add('hidden');
        const changeBilling = document.getElementById('change-billing-info');
        changeBilling.classList.remove('hidden');
        changeBilling.addEventListener("click", this.changeBilling);

    }
    async handleShippingSubmit(event) {
        event.preventDefault();

        let shippingForm = document.getElementById("shipping-form");
        let shippingFormData = new FormData(shippingForm);

        let shippingInfo = {
            firstName: shippingFormData.get('firstName'),
            lastName: shippingFormData.get('lastName'),
            addressFirstLine: shippingFormData.get('addressFirstLine'),
            addressSecondLine: shippingFormData.get('addressSecondLine'),
            city: shippingFormData.get('city'),
            state: shippingFormData.get('state'),
            zipCode: shippingFormData.get('zipCode')
        }

        if (shippingFormData.get("saveDefault")) {
            this.profileServiceClient.updateCustomerProfile(userId, null, null, null, shippingInfo, this.errorHandler);
        }

        this.dataStore.set("shippingInfo", shippingInfo);
        document.getElementById('shipping-info-header').classList.add('inline');
        shippingForm.classList.add('hidden');
        const changeButton =  document.getElementById('change-shipping-info');
        changeButton.classList.remove('hidden');
        changeButton.addEventListener('click', this.changeShipping);

        // TODO if save as default save shipping address to preferred for profile
    }

// ------- UI helpers -------

     showMessage(messageText) {
        const messageContainer = document.querySelector("#payment-message");

        messageContainer.classList.remove("hidden");
        messageContainer.textContent = messageText;

        setTimeout(function () {
            messageContainer.classList.add("hidden");
            messageText.textContent = "";
        }, 4000);
    }

// Show a spinner on payment submission
     setLoading(isLoading) {
        if (isLoading) {
            // Disable the button and show a spinner
            document.querySelector("#submit").disabled = true;
            document.querySelector("#spinner").classList.remove("hidden");
            document.querySelector("#button-text").classList.add("hidden");
        } else {
            document.querySelector("#submit").disabled = false;
            document.querySelector("#spinner").classList.add("hidden");
            document.querySelector("#button-text").classList.remove("hidden");
        }
    }
}

const main = async () => {
    const checkoutPage = new CheckoutPage();
    await checkoutPage.mount();
}

window.addEventListener('DOMContentLoaded', main);

