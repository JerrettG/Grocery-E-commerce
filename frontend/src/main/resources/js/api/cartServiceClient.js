import BaseClass from "../../js/util/baseClass.js";

import axios from "axios";

/**
 * Client to call the cartService
 *
 *
 */
export default class CartServiceClient extends BaseClass {

    constructor(props = {}) {
        super();
        const methodsToBind = []
        this.bindClassMethods(methodsToBind);
        this.props = props;
        this.clientLoaded(axios);
    }

    /**
     * Run any functions that are supposed to be called once the client has loaded successfully.
     */
    clientLoaded(client) {
        this.client = client;
        if (this.props.hasOwnProperty("onReady")){
            this.props.onReady();
        }
    }

    async getCartForUserId(userId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/cartService/cart/${userId}`);
            return response.data;
        } catch (error) {
            this.handleError('getCartForUserId', error, errorCallback);
        }
    }

    async addCartItem(userId, quantity, productId, productName, productImageUrl, productPrice, errorCallback) {
        try {
            const response = await this.client.post(`/api/v1/cartService/cart/${userId}/cartItem`,
                    {
                        userId: userId,
                        quantity: quantity,
                        productId: productId,
                        productName: productName,
                        productImageUrl: productImageUrl,
                        productPrice: productPrice
                    }
                )
            return response.data;
        } catch (error) {
            this.handleError('addCartItem', error, errorCallback)
        }
    }

    async removeItemFromCart(userId, cartItemId, errorCallback) {
        try {
            const response = this.client.delete(`/api/v1/cartService/cart/${userId}/cartItem/${cartItemId}`)
            return response;
        } catch (error) {
            this.handleError('removeItemFromCart', error, errorCallback)
        }
    }

    async updateItemQuantity(userId, cartItem, updatedQuantity, errorCallback) {
        try {

        } catch (error) {
            this.handleError('updateItemQuantity', error, errorCallback)
        }
    }

    async clearCart(userId, errorCallback) {
        try{
            const response = this.client.delete(`/api/v1/cartService/cart/${userId}`);
            return response.data;
        } catch (error) {
            this.handleError('clearCart', error, errorCallback);
        }
    }


    /**
     * Helper method to log the error and run any error functions.
     * @param error The error received from the server.
     * @param errorCallback (Optional) A function to execute if the call fails.
     */
    handleError(method, error, errorCallback) {
        console.error(method + " failed - " + error);
        if (error.response.data.message !== undefined) {
            console.error(error.response.data.message);
        }
        if (errorCallback) {
            errorCallback(method + " failed - " + error);
        }
    }
}