import BaseClass from "../../js/util/baseClass.js";
import CartServiceClient from "../../js/api/cartServiceClient.js";
import DataStore from "../../js/util/DataStore.js";
import ProductServiceClient from "../api/productServiceClient";

/**
 * Logic needed for the shopping cart page
 */
class IndexPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderProducts', 'onGetAllProducts',
            'onAddItemToCart', 'refreshSearch', 'fuzzyMatch',
            'onGetProductsByCategory', 'toggleDropdown'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.productServiceClient = new ProductServiceClient();
        this.cartServiceClient = new CartServiceClient();

        //Controls navbar dropdowns
        document.querySelectorAll('.toggle-dropdwn-button')
            .forEach((element) => element.addEventListener("click", this.toggleDropdown));
        document.querySelector('.menu-icon').addEventListener('click', this.openNav);
        document.querySelector('.closebtn').addEventListener('click', this.closeNav);


        // TODO make the search bar take the user to the products page on enter
        document.getElementById("search-bar").addEventListener("keyup", this.refreshSearch);
        let categoryLinks = document.querySelectorAll('.category-link');
        for (let categoryLink of categoryLinks) {
            categoryLink.addEventListener('click', this.onGetProductsByCategory);
        }

        this.dataStore.addChangeListener(this.renderProducts);
        await this.onGetAllProducts();

    }

    async renderProducts() {
        let productCatalogArea = document.getElementById('product-catalog');
        const products = this.dataStore.get('products');
        let html = '';
        if (products && products.length > 0) {
            for (let product of products) {
                html +=
                    `
                     <div class="product-catalog-product-card" id="${product.productId}">
                        <li>
                            <a href="/product/${product.name}">
                                <img src="${product.imageUrl}" alt="${product.name}" class="product-catalog-product-image">
                                <div class="product-name-container">
                                    <h3 class="product-name">${product.name}</h3>
                                </div>
                                <div class="product-price-unit-description">
                                    <span style="font-family: Arial"><strong>${this.formatCurrency(product.price)}</strong></span><span> / </span><span style="font-family: Arial"><strong>${product.unitMeasurement}</strong></span>
                                </div>
                            </a>
                            <button class="add-to-cart-button" type="button">Add to cart
                           
                            </button>
                        </li>
                        <form class="add-to-cart-form">
                                <input type="hidden" name="productId" value="${product.productId}">
                                <input type="hidden" name="price" value="${product.price}">
                                <input type="hidden" name="productImageUrl" value="${product.imageUrl}">
                                <input type="hidden" name="name" value="${product.name}">
                            </form>
                     </div>
                    `
                productCatalogArea.innerHTML = html;
                const addToCartButtons = document.querySelectorAll('.add-to-cart-button');
                for (let addToCartButton of addToCartButtons) {
                    addToCartButton.addEventListener('click', this.onAddItemToCart);
                }

            }
        } else {
            productCatalogArea.innerHTML = '<li><h1>No results</h1></li>';
        }
    }

    async onGetAllProducts(event) {
        this.showLoading(document.getElementById("product-catalog"), 15);
        let result = await this.productServiceClient.getAllProducts(this.errorHandler);
        this.dataStore.set('products', result);
        if (result) {
            // this.showMessage(`Product catalog loaded successfully`);
        } else {
            this.errorHandler("Error getting product catalog. Try again...");
        }
    }

    async onGetProductsByCategory(event) {
        this.showLoading(document.getElementById("product-catalog"), 15);
        let category = event.srcElement.innerHTML;
        category = category.toUpperCase();
        category = category.replaceAll(/ /g,"_");
        category = category.replaceAll("&", "AND");
        category = category.replaceAll("AMP;", "");
        category = category.replaceAll(",", "");
        category = category.trim();

        let result  = await this.productServiceClient.getProductsByCategory(category, this.errorHandler);
        this.dataStore.set("products", result);
        if (result) {
            // this.showMessage(`Product catalog loaded successfully`);
        } else {
            this.errorHandler("Error getting product catalog. Try again...");
        }
    }
    
    async onAddItemToCart(event) {
        const addButton = event.srcElement;
        let originalInnerHtml = addButton.innerHTML;
        this.showLoading(addButton);
        if (!userId) {
            window.location.href = "http://localhost:8084/oauth2/authorization/auth0";
        } else {
            const addToCartForm = addButton.closest("li").nextElementSibling;
            let formData = new FormData(addToCartForm);
            let productId = formData.get("productId");
            let name = formData.get("name");
            let price = formData.get("price");
            let quantity = 1;
            let productImageUrl = formData.get("productImageUrl");

            const result = await this.cartServiceClient.addCartItem(userId, quantity, productId, name, productImageUrl, price, this.errorHandler);
            if (result) {
                addButton.querySelector('.loading-spinner').remove();
                addButton.innerHTML +=
                    `
                    <div class="success-animation">
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

    fuzzyMatch(text, searchTerm) {
        searchTerm = searchTerm.replace(/\ /g, '').toLowerCase();
        return text.toLowerCase().includes(searchTerm);
    }

    refreshSearch() {
        let searchTerm = document.getElementById("search-bar").value;
        let results = [];
        const products = this.dataStore.get('products');
        if (products && products.length > 0) {
            for (let product of products) {
                let match = this.fuzzyMatch(product.name, searchTerm);
                if (match) {
                    results.push(product);
                }
            }
            this.dataStore.set('products', results);
        } //TODO this should not change the list of products but only change the shown products
    }
}


const main = async () => {
    const indexPage = new IndexPage();
    await indexPage.mount();
}

window.addEventListener('DOMContentLoaded', main);
