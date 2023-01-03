import BaseClass from "../../js/util/baseClass.js";

import axios from "axios";

/**
 * Client to call the productService
 *
 *
 */
export default class ProductServiceClient extends BaseClass {

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
    async getAllProducts(errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/productService/product/all`);
            return response.data;
        } catch (error) {
            this.handleError('getAllProducts', error, errorCallback)
        }
    }

    async getProductsByCategory(category, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/productService/product/all?category=${category}`);
            return response.data;
        } catch (error) {
            this.handleError('getProductsByCategory', error, errorCallback)
        }
    }

    async getProductWithProductName(productName, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/productService/product/${productName}`);
            return response.data;
        } catch (error) {
            this.handleError('getProductWithProductName', error, errorCallback);
        }
    }

    async createProduct(name, price, unitMeasurement, description, category, imageUrl, errorCallback) {
        try {
            const response = await this.client.post(`/api/v1/productService/product`,
                    {
                        name: name,
                        price: price,
                        unitMeasurement: unitMeasurement,
                        description: description,
                        category: category,
                        imageUrl: imageUrl
                    }
                );
            return response.data;
        } catch (error) {
            this.handleError('createProduct', error, errorCallback)
        }
    }

    async updateProduct(productId, name, price, unitMeasurement,
                                description, category, imageUrl, rating, errorCallback) {
        try {
            const response = await this.client.put(`/product`,
                {
                    productId: productId,
                    name: name,
                    price: price,
                    unitMeasurement: unitMeasurement,
                    description: description,
                    category: category,
                    imageUrl: imageUrl,
                    rating: rating
                }
            );
            return response.data;
        }
         catch (error) {
            this.handleError('updateProduct', error, errorCallback)
        }
    }

    async deleteProduct(productName, errorCallback) {
        try {
            const response = await this.client.delete(`/api/v1/productService/product/${productName}`)
            return response.data;
        } catch (error) {
            this.handleError('deleteProduct', error, errorCallback)
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