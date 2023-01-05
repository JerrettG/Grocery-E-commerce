import BaseClass from "../util/baseClass";
import OrderServiceClient from "../api/orderServiceClient";


export default class OrderPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderPage','requestCancellation'], this);
    }


    async mount() {
        this.orderServiceClient = new OrderServiceClient();
        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);

        this.renderPage();
    }

    async renderPage() {
        const orderInfo = document.querySelector('.order-info');
        const orderContainer = document.querySelector('.order');
        this.showLoading(orderInfo);
        let order = await this.orderServiceClient.getOrderByOrderId(orderId, userId, this.errorHandler);
        if (order) {
            let orderInfohtml =
                `
                <div>
                  <div>
                    <span>Order Status:</span>
                    <span>${order.status}</span>
                  </div>
                  <div>
                    <span>Order Placed:</span>
                    <span>${this.formatDate(order.createdDate)}</span>
                  </div>
                </div>
                
                <div class="order-info-shipping">
                    <span>Ship to:</span>
                    <span>Bill to:</span>
                </div>
                `;
            let orderDetailsHtml = '';
            for (let item of order.orderItems) {
                orderDetailsHtml +=
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
            orderInfo.innerHTML = orderInfohtml;
            orderContainer.innerHTML += orderDetailsHtml;
        } else {
            orderInfo.innerHTML = '<h1>There was an issue loading your order</h1>';
        }

    }


    async requestCancellation() {

    }
}

const main = async () => {
    const orderPage = new OrderPage();
    await orderPage.mount();
}

window.addEventListener('DOMContentLoaded', main);