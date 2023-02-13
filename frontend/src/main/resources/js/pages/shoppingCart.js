import BaseClass from "../../js/util/baseClass.js";
import CartServiceClient from "../../js/api/cartServiceClient.js";
import DataStore from "../../js/util/DataStore.js";

/**
 * Logic needed for the shopping cart page
 */
class ShoppingCartPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderShoppingCart', 'onGetShoppingCartByUserId',
            'onRemoveItemFromCart'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.cartServiceClient = new CartServiceClient();
        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);

        this.dataStore.addChangeListener(this.renderShoppingCart);
        this.onGetShoppingCartByUserId();

    }

    async renderShoppingCart() {
        let shoppingCartArea = document.getElementById('shopping-cart');
        const cart = await this.dataStore.get('cart');
        let html = '';
        if (cart && cart.cartItems.length > 0) {
            for (let cartItem of cart.cartItems) {
                html +=
                    `
                      <li  class="shopping-cart-item">
                        <form class="remove-from-cart-form">
                           <input type="hidden" value="${cartItem.id}" name="id">
                           <input type="hidden" value="${cartItem.userId}" name="userId">
                           <input type="hidden" value="${cartItem.productId}" name="productId">
                        </form>
                        <div class="shopping-cart-item-div">
                            <div class="order-product-image">
                                <img src="${cartItem.productImageUrl}" alt="${cartItem.productName}">
                            </div>
                            <div class="order-product-name">
                                <span><a href="/product/${cartItem.productName}">${cartItem.productName}</a></span>
                            </div>
                            <div class="order-product-quantity">
                                <span>${cartItem.quantity} Qty.</span>
                            </div>
                            <div class="order-product-unit-price">
                                <span>${this.formatCurrency(cartItem.productPrice)}</span>
                            </div>
                            <div class="remove-item">
                                <button class="remove-item-button" type="button"><span class="remove-item-icon"></span></button>
                            </div>
                        </div>
                    </li>
                    `
                shoppingCartArea.innerHTML = html;
                const removeButtons = document.querySelectorAll('.remove-item-button');
                for (let removeItemButton of removeButtons) {
                    removeItemButton.addEventListener('click', this.onRemoveItemFromCart);
                }
                document.getElementById('shopping-cart-subtotal').innerHTML = this.formatCurrency(cart.subtotal);
                document.getElementById('checkout-button').style.display = 'block';

            }
        } else {
            shoppingCartArea.innerHTML = '<li><h1>Your shopping cart is empty</h1></li>';
            document.getElementById('shopping-cart-subtotal').innerHTML = this.formatCurrency(0);
            document.getElementById('checkout-button').style.display = 'none';
        }
    }

    async onGetShoppingCartByUserId(event) {
        this.showLoading(document.getElementById("shopping-cart"), 15);
        let result = await this.cartServiceClient.getCartForUserId(userId, this.errorHandler);
        this.dataStore.set('cart', result);
        if (result) {
            // this.showMessage(`Shopping cart loaded successfully`);
        } else {
            this.errorHandler("Error doing GET! Try again...");
        }
    }

    async onRemoveItemFromCart(event) {
        const removeItemForm = event.srcElement.closest('.shopping-cart-item').firstChild.nextSibling;
        let formData = new FormData(removeItemForm);
        let userId = formData.get('userId');
        let cartItemId = formData.get('id');
        let result = await this.cartServiceClient.removeItemFromCart(userId, cartItemId, this.errorHandler);

        if (result.status === 202) {
            await this.onGetShoppingCartByUserId();
            this.showMessage(`Successfully removed item from cart`);
        } else {
            this.errorHandler(result);
        }
    }
}

/**
 * Main method to run when the page contents have loaded.
 */
const main = async () => {
    const shoppingCartPage = new ShoppingCartPage();
    await shoppingCartPage.mount();
};

window.addEventListener('DOMContentLoaded', main);