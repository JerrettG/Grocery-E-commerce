import BaseClass from "../util/baseClass.js";
import CartServiceClient from "../api/cartServiceClient.js";
import DataStore from "../util/DataStore.js";

/**
 * Logic needed for the shopping cart page
 */
class ShoppingCartPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderShoppingCart',
            'onGetShoppingCartByUserId',
            'onRemoveItemFromCart', 'onUpdateItemQuantity'
        ])
        this.dataStore = new DataStore();
    }
    async mount() {
        const removeButtons = document.querySelectorAll('.remove-item-button');
        for (let removeItemButton of removeButtons) {
            removeItemButton.addEventListener('click', this.onRemoveItemFromCart);
        }
        this.client = new CartServiceClient();
        if (document.title === 'Grocery E-commerce Shopping Cart') {
        this.dataStore.addChangeListener(this.renderShoppingCart);
        }
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
                                <span><a href="/product/${cartItem.productName}}">${cartItem.productName}</a></span>
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
                document.getElementById('shopping-cart-subtotal').innerHTML = this.formatCurrency(cart.subtotal);
                document.getElementById('checkout-button').disabled = false;
            }
        } else {
            shoppingCartArea.innerHTML = '<li><h1>Your shopping cart is empty</h1></li>';
            document.getElementById('shopping-cart-subtotal').innerHTML = this.formatCurrency(0);
            document.getElementById('checkout-button').disabled = true;
        }
    }

    async onGetShoppingCartByUserId(event) {
        let result = await this.client.getCartForUserId(userId, this.errorHandler);
        this.dataStore.set('cart', result);
        if (result) {
            this.showMessage(`Shopping cart loaded successfully`);
        } else {
            this.errorHandler("Error doing GET! Try again...");
        }
    }

    async onRemoveItemFromCart(event) {
        const removeItemForm = event.srcElement.closest('.remove-from-cart-form');
        let formData = new FormData(removeItemForm);
        let userId = formData.get('userId');
        let cartItemId = formData.get('id');
        let result = await this.client.removeItemFromCart(userId, cartItemId, this.errorHandler);

        if (result) {
            await this.onGetShoppingCartByUserId();
            this.showMessage(`Successfully removed ${cartItemId} from ${userId}'s cart`);
        } else {
            this.errorHandler("Error doing GET! Try again...");
        }
    }
}

//
// function checkout(cart) {
//     document.getElementById("cart").value=JSON.stringify(cart);
//
//     document.getElementById("checkout-form").submit();
// }
//
//
// async function removeFromCart(id, userId) {
//     const request = {
//         id: id,
//         userId: userId
//     }
//
//     const response = await fetch(`/api/v1/cartService/cart/${userId}/cartItem`, {
//         method: "POST",
//         headers: {"Content-Type": "application/json"},
//         body: JSON.stringify(request)
//     }).catch(function (){
//         console.log("error");
//     })
//
//     let updatedCartHtml = '';
//
//     if (response.status === 202) {
//
//         let formatter = new Intl.NumberFormat('en-US', {
//             style: 'currency',
//             currency: 'USD'
//         });
//         let updatedSubtotal = 0;
//         const updatedCartItems = [];
//
//         for (let item of cart.cartItems) {
//             if (item.id !== id) {
//                 let price = formatter.format(item.productPrice);
//                 updatedSubtotal += item.productPrice*item.quantity;
//                 updatedCartItems.push(item);
//                 updatedCartHtml +=
//                     `
//                     <li class="shopping-cart-item">
//                         <div class="shopping-cart-item-div">
//                             <div class="order-product-image">
//                                 <img src="${item.productImageUrl}" alt="${item.productName}">
//                             </div>
//                             <div class="order-product-name">
//                                 <span><a href="/product/${item.productName}">${item.productName}</a></span>
//                             </div>
//                             <div class="order-product-quantity">
//                                 <span>${item.quantity} Qty.</span>
//                             </div>
//                             <div class="order-product-unit-price">
//                                 <span>${price}</span>
//                             </div>
//                             <div class="remove-item">
//                                 <button class="remove-item-button" type="button" onclick="removeFromCart('${item.id}', '${item.userId}')"  form="remove-from-cart"><span class="remove-item-icon"></span></button>
//                             </div>
//                         </div>
//                     </li>
//                     `
//             }
//         }
//
//         cart.cartItems = updatedCartItems;
//
//         if (cart.cartItems.length === 0) {
//             document.getElementById("shopping-cart").innerHTML =
//                 `
//                 <li><h1>Your shopping cart is empty</h1></li>
//                 `;
//             document.getElementById("checkout-button").style.display = "none";
//             document.getElementById("shopping-cart-subtotal").innerHTML = "$0.00";
//         }
//         else {
//         updatedSubtotal = formatter.format(updatedSubtotal);
//         document.getElementById("shopping-cart-subtotal").innerHTML = `${updatedSubtotal}`
//         document.getElementById("shopping-cart").innerHTML = updatedCartHtml;
//         }
//     }
// }

/**
 * Main method to run when the page contents have loaded.
 */
const main = async () => {
    const shoppingCartPage = new ShoppingCartPage();
    await shoppingCartPage.mount();
};

window.addEventListener('DOMContentLoaded', main);