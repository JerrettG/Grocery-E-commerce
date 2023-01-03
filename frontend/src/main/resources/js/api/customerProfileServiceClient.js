import BaseClass from "../../js/util/baseClass.js";

import axios from "axios";

/**
 * Client to call the customerProfileService
 *
 *
 */
export default class CustomerProfileServiceClient extends BaseClass {

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

    async getCustomerProfileByUserId(userId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/customerProfileService/user/${userId}`);
            return response.data;
        } catch (error) {
            this.handleError('getCustomerProfileByUserId', error, errorCallback);
        }
    }

    async createCustomerProfile(userId, email, firstName, lastName, shippingAddress, errorCallback) {
        try {
            const response = await this.client.post(`/api/v1/customerProfileService/user`,
                    {
                        userId: userId,
                        email: email,
                        firstName: firstName,
                        lastName: lastName,
                        shippingAddress: shippingAddress
                    }
                );
            return response.data;
        } catch (error) {
            this.handleError('createCustomerProfile', error, errorCallback)
        }
    }

    async updateCustomerProfile(userId, email, firstName, lastName, shippingAddress, errorCallback) {
        try {
            const response = await this.client.put(`/user`,
                {
                    userId: userId,
                    email: email,
                    firstName: firstName,
                    lastName: lastName,
                    shippingAddress: shippingAddress
                }
            );
            return response.data;
        }
         catch (error) {
            this.handleError('updateCustomerProfile', error, errorCallback)
        }
    }

    async deleteCustomerProfile(userId, errorCallback) {
        try {
            const response = await this.client.delete(`/api/v1/customerProfileService/user/${userId}`)
            return response.data;
        } catch (error) {
            this.handleError('deleteCustomerProfile', error, errorCallback)
        }
    }
    async deactivateCustomerProfile(userId, errorCallback) {
        try {
            const response = await this.client.delete(`/api/v1/customerProfileService/user/${userId}?eraseData=true`)
            return response.data;
        } catch (error) {
            this.handleError('deactivateCustomerProfile', error, errorCallback)
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