import BaseClass from "../../js/util/baseClass.js";
import CartServiceClient from "../../js/api/cartServiceClient.js";
import DataStore from "../../js/util/DataStore.js";

/**
 * Logic needed for the shopping cart page
 */
class ProductPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'onAddItemToCart', 'increaseQuantity', 'decreaseQuantity'], this);
    }

    async mount() {
        this.cartServiceClient = new CartServiceClient();
        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);

        document.getElementById("add-to-cart-button").addEventListener("click", this.onAddItemToCart);
        document.getElementById("decrement-quantity-button").addEventListener("click", this.decreaseQuantity);
        document.getElementById("increment-quantity-button").addEventListener("click", this.increaseQuantity);
    }

    async onAddItemToCart(event) {
        const addButton = event.srcElement;
        let originalInnerHtml = addButton.innerHTML;
        this.showLoading(addButton);
        if (!isAuthenticated) {
            window.location.href = "http://localhost:8084/oauth2/authorization/auth0";
        } else {
            const addToCartForm = document.getElementById("add-to-cart-form");
            let formData = new FormData(addToCartForm);
            let productId = formData.get("productId");
            let name = formData.get("name");
            let price = formData.get("price");
            let quantity = formData.get("quantity");
            let productImageUrl = formData.get("productImageUrl");

            const result = await this.cartServiceClient.addCartItem(userId, quantity, productId, name, productImageUrl, price, this.errorHandler);
            if (result) {
                addButton.querySelector('.loading-spinner').remove();
                addButton.innerHTML +=
                    `
                    <div class="success-animation" id="success-animation-product-page">
                        <svg class="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                            <circle class="checkmark__circle" cx="26" cy="26" r="25" fill="none" />
                            <path class="checkmark__check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                        </svg>
                    </div>
                    `;
                setTimeout(() => {addButton.innerHTML = originalInnerHtml; }, 2000);
                // this.showMessage(`Item added to cart successfully`);
            } else {
                this.errorHandler("Error doing adding to cart. Try again...");
            }
        }

    }

    increaseQuantity(event) {
        let input = event.srcElement.previousElementSibling;
        let value = parseInt(input.value, 10);
        value = isNaN(value) ? 0 : value;
        value++;
        input.value = value;
    }
    decreaseQuantity(event) {
        let input = event.srcElement.nextElementSibling;
        let value = parseInt(input.value, 10);
        if (value > 1) {
            value = isNaN(value) ? 0 : value;
            value--;
            input.value = value;
        }
    }
}


/**
 * Main method to run when the page contents have loaded.
 */
const main = async () => {
    const productPage = new ProductPage();
    await productPage.mount();
};

window.addEventListener('DOMContentLoaded', main);