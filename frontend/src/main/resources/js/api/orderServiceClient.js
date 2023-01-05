import BaseClass from "../../js/util/baseClass.js";

import axios from "axios";

/**
 * Client to call the orderService
 *
 *
 */
export default class OrderServiceClient extends BaseClass {

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
    async getAllOrdersForUserId(userId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/orderService//order/all/user/${userId}`);
            return response.data;
        } catch (error) {
            this.handleError('getAllOrdersForUserId', error, errorCallback)
        }
    }

    async getOrderByOrderId(orderId, userId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/orderService/order/${orderId}/user/${userId}`);
            return response.data;
        } catch (error) {
            this.handleError('getOrderByOrderId', error, errorCallback);
        }
    }

    async getOrderByPaymentId(userId, paymentIntentId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/orderService/order/user/${userId}/paymentIntent/${paymentIntentId}`);
            return response.data;
        } catch (error) {
            this.handleError('getOrderByPaymentIntentId', error, errorCallback);
        }
    }

    async createOrder(userId, paymentIntentId, orderItems, subtotal, shippingCost, total, status, shippingInfo, billingInfo, errorCallback) {
        try {
            const response = await this.client.post(`/api/v1/orderService/order`,
                    {
                        userId: userId,
                        paymentIntentId: paymentIntentId,
                        orderItems: orderItems,
                        subtotal: subtotal,
                        shippingCost: shippingCost,
                        total: total,
                        status: status,
                        shippingInfo: shippingInfo,
                        billingInfo: billingInfo
                    }
                );
            return response.data;
        } catch (error) {
            this.handleError('createOrder', error, errorCallback)
        }
    }

    async updateOrder(id, userId, shippingInfo, billingInfo, status, orderItems, errorCallback) {
        try {
            const response = await this.client.put(`/api/v1/orderService/order`,
                {
                    userId: userId,
                    id: id,
                    shippingInfo: shippingInfo,
                    billingInfo: billingInfo,
                    status: status,
                    orderItems: orderItems
                }
            );
            return response.data;
        }
         catch (error) {
            this.handleError('updateOrder', error, errorCallback)
        }
    }

    async deleteOrder(id, userId, errorCallback) {
        try {
            const response = await this.client.delete(`/api/v1/orderService/order/${id}/user/${userId}`)
            return response.data;
        } catch (error) {
            this.handleError('deleteOrder', error, errorCallback)
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