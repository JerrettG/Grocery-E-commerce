import BaseClass from "../util/baseClass";
import DataStore from "../util/DataStore";
import CartServiceClient from "../api/cartServiceClient";
import OrderServiceClient from "../api/orderServiceClient";
import CustomerProfileServiceClient from "../api/customerProfileServiceClient";

export default class CheckoutPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'initialize','getCookie', 'setCookie',
            'handleOrderSubmit', 'showMessage', 'setLoading', 'handleShippingSubmit',
            'handleBillingSubmit', 'changeShipping', 'changeBilling', 'toggleSameAsShipping'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.cartServiceClient = new CartServiceClient();
        this.orderServiceClient = new OrderServiceClient();
        this.profileServiceClient = new CustomerProfileServiceClient();
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

        document
            .querySelector("#payment-form")
            .addEventListener("submit", this.handleOrderSubmit);
        document.getElementById('order-summary-item-count').innerHTML = `Items (${numItems})`;
        this.deleteCookie("paymentIntentId");
        this.initialize();
    }



// Fetches a payment intent and captures the client secret
    async initialize() {


        let profile = await this.profileServiceClient.getCustomerProfileByUserId(userId);
        if (profile) {
            this.dataStore.set("profile", profile);
        }

        let name = `${profile.firstName} ${profile.lastName}`;
        let defaultAddressLine1 = profile.shippingInfo.addressFirstLine;
        let defaultAddressLine2 = profile.shippingInfo.addressSecondLine;
        let defaultAddressCity = profile.shippingInfo.city;
        let defaultAddressState = profile.shippingInfo.state;
        let defaultAddressZipCode = profile.shippingInfo.zipCode;


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
        let shippingCost = 7.99;
        let total = subtotal + tax + shippingCost;

        this.dataStore.set("subtotal", subtotal);
        this.dataStore.set("shippingCost", shippingCost);
        this.dataStore.set("tax", tax);
        this.dataStore.set("orderItems", orderItems);
        this.dataStore.set("total", total);


        let paymentIntentId = this.getCookie('paymentIntentId');
        let clientSecret = this.getCookie('clientSecret');

        if (!clientSecret || !paymentIntentId) {
            const response = await fetch("/payment/create-payment-intent",
                {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                            userId: userId,
                        //TODO make the name of the user come from shipping if profile fields are null
                            name: name,
                            email: profile.email,
                            total: total
                        }
                    ),
                });
            const jsonResponse = await response.json();
            clientSecret = jsonResponse.clientSecret;
            paymentIntentId = jsonResponse.paymentIntentId;

            this.setCookie('clientSecret', clientSecret);
            this.setCookie('paymentIntentId', paymentIntentId);
        }

        //TODO make paymentIntent update with cart total every time the user goes to checkout
        // const paymentIntent = await this.stripe.updatePaymentIntent(paymentIntentId, {amount: total*100})



        const appearance = {
            theme: 'stripe',
        };

        this.elements = this.stripe.elements({appearance, clientSecret});

        const addressElement = this.elements.create("address", {
            mode: "shipping",
            defaultValues: {
                name: name,
                address: {
                    line1: defaultAddressLine1,
                    line2: defaultAddressLine2,
                    city: defaultAddressCity,
                    state: defaultAddressState,
                    postal_code: defaultAddressZipCode,
                    country: 'US',
                }
            },
            allowedCountries: ['US'],
            blockPoBox: true,
            fields: {
                phone: 'never',
            },
        });
        addressElement.mount("#address-element");
        const paymentElement = this.elements.create("payment");
        document.querySelector('.payment-form-container .checkout-input').style.display = 'block';
        paymentElement.mount("#payment-element");

        addressElement.on('change', (event) => {
            let name = event.value.name.split(' ');
            let shippingInfo = {
                firstName: name[0],
                lastName: name[1],
                addressFirstLine: event.value.address.line1,
                addressSecondLine: event.value.address.line2,
                city: event.value.address.city,
                state: event.value.address.state,
                zipCode: event.value.address.postal_code,
                country: event.value.address.country
            }
            this.dataStore.set("shippingInfo", shippingInfo);
        });

        document.getElementById("checkout-subtotal").innerHTML = this.formatCurrency(subtotal);
        document.getElementById("checkout-shipping").innerHTML = this.formatCurrency(shippingCost);
        document.getElementById("checkout-tax").innerHTML = this.formatCurrency(tax);
        document.getElementById("checkout-total").innerHTML = this.formatCurrency(total);
    }


    async handleOrderSubmit(e) {
        e.preventDefault();
        this.setLoading(true);

        let elements = this.elements;

        let status = "AWAITING_PAYMENT";
        let paymentIntentId = this.getCookie("paymentIntentId");
        let orderItems = this.dataStore.get("orderItems");
        let subtotal = this.dataStore.get("subtotal");
        let shippingCost = this.dataStore.get("shippingCost");
        let tax = this.dataStore.get("tax");
        let total = this.dataStore.get("total");
        let shippingInfo = this.dataStore.get("shippingInfo");

        // TODO find a way to get billingInfo from stripe
        let billingInfo = shippingInfo;

        if (document.getElementById('saveAsDefault').checked) {
            this.profileServiceClient.updateCustomerProfile(userId, null, null, null, shippingInfo, this.errorHandler);
        }

        await this.orderServiceClient.createOrder(userId, paymentIntentId, orderItems, subtotal, shippingCost, tax, total, status, shippingInfo, billingInfo, this.errorHandler);

        const { error } = await this.stripe.confirmPayment({
            elements,
            confirmParams: {
                // Make sure to change this to your payment completion page
                return_url: "http://localhost:8084/success",
                receipt_email: document.getElementById("email-input").value,
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
        this.dataStore.set("shippingInfo", shippingInfo);
        document.getElementById('shipping-info-header').classList.add('inline');
        shippingForm.classList.add('hidden');
        const changeButton =  document.getElementById('change-shipping-info');
        changeButton.classList.remove('hidden');
        changeButton.addEventListener('click', this.changeShipping);


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

