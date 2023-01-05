import BaseClass from "../../js/util/baseClass.js";
import DataStore from "../../js/util/DataStore.js";
import OrderServiceClient from "../api/orderServiceClient";
import CustomerProfileServiceClient from "../api/customerProfileServiceClient";

/**
 * Logic needed for the profile page
 */
class ProfilePage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderProfile','renderOrders', 'getAllOrdersForUserId', 'getCustomerProfileByUserId'], this);
        this.dataStore = new DataStore();
    }


    async mount() {
        this.orderServiceClient = new OrderServiceClient();
        this.profileServiceClient = new CustomerProfileServiceClient();

        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);

        this.dataStore.addChangeListener(this.renderOrders);

        this.getCustomerProfileByUserId();
        this.getAllOrdersForUserId();

    }

    async renderProfile(profile) {
        const accountInfo = document.querySelector('.account-info');
        let addressSecondLine ='';
        if (profile.shippingInfo.addressSecondLine)
            addressSecondLine = `${profile.shippingInfo.addressSecondLine},`;

        accountInfo.innerHTML =
            `
            <h4>Email</h4>
            <p id="account-email">${profile.email}</p>
            <h4>First Name</h4>
            <p id="account-first-name">${profile.firstName}</p>
            <h4>Last Name</h4>
            <p id="account-last-name">${profile.lastName}</p>
            <h4>Preferred Shipping Address</h4>
            <p class="account-shipping-address">${profile.shippingInfo.firstName} ${profile.shippingInfo.lastName}</p>
            <p class="account-shipping-address">${profile.shippingInfo.addressFirstLine}, ${addressSecondLine}</p>
            <p class="account-shipping-address">${profile.shippingInfo.city}, ${profile.shippingInfo.state}, ${profile.shippingInfo.zipCode}, USA</p>
            `
    }

    async renderOrders() {
        const ordersDiv = document.querySelector(".orders");
        let html = '';

        const orders = this.dataStore.get("orders");
        if (orders && orders.length > 0) {
            for (let order of orders) {
                html +=
                    `
                    <div class="order">
                        <div class="order-info">
                            <div class="info-column">
                                <div class="info-column-description">
                                    <span>Order Placed:</span>
                                </div>
                                <div class="info-column-data">
                                    <span>${this.formatDate(order.createdDate)}</span>
                                </div>
                            </div>
                            <div class="info-column">
                                <div class="info-column-description">
                                    <span>Order status:</span>
                                </div>
                                <div class="info-column-data">
                                    <span>${order.status}</span>
                                </div>
                            </div>
                            <div class="info-column">
                                <div class="info-column-description">
                                    <span>Order total:</span>
                                </div>
                                <div class="info-column-data">
                                    <span>${this.formatCurrency(order.total)}</span>
                                </div>
                            </div>
                            <div class="info-column">
                                <div class="info-column-description">
                                    <span>Order number:</span>
                                </div>
                                <div class="info-column-data">
                                    <a href="/order?orderId=${order.id}"><span>${order.id}</span></a> 
                                </div>
                            </div>
                        </div>
                    `;
                for (let item of order.orderItems) {
                    html +=
                        `
                        <div class="order-details">
                            <div class="order-product-image">
                                <img src="${item.imageUrl}" alt="${item.itemName}">
                            </div>
                            <div class="order-product-name">
                                <span><a href="/product/${item.itemName}">${item.itemName}</a></span>
                            </div>
                            <div class="order-product-quantity">
                                <span>${item.quantity} Qty.</span>
                            </div>
                            <div class="order-product-unit-price">
                                <span>${this.formatCurrency(item.unitPrice)}</span>
                            </div>
                        </div>
                        `;
                }
                html += `</div>`;
            }

            ordersDiv.innerHTML = html;
        } else {
            document.querySelector('.orders-container').innerHTML = "<h1>You don't have any orders yet</h1>";
        }
    }

    async getAllOrdersForUserId(event) {
        this.showLoading(document.querySelector(".orders"), 15);
        const result = await this.orderServiceClient.getAllOrdersForUserId(userId, this.errorHandler);
        if (result) {
            this.dataStore.set("orders", result);
            this.showMessage(`Retrieved orders for ${userId}`);
        } else {
            this.showMessage(`Error getting orders for ${userId}`);
        }
    }

    async getCustomerProfileByUserId(event) {
        let result = await this.profileServiceClient.getCustomerProfileByUserId(userId, this.errorHandler);
        if (result) {
            this.renderProfile(result);
        } else {
            this.showMessage("There was an error loading your account info");
        }

    }

}

const main = async () => {
    const profilePage = new ProfilePage();
    await profilePage.mount();
}

window.addEventListener('DOMContentLoaded', main);