const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyPlugin = require("copy-webpack-plugin");
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const Dotenv = require('dotenv-webpack');

module.exports = {
    optimization: {
        usedExports: true
    },
    context: __dirname + '/src',
    entry: {
        index: path.resolve('main', 'resources', 'static', 'js', 'index.js'),
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js',
    },
    devServer: {
        https: false,
        port: 8084,
        open: true,
        openPage: 'http://localhost:8084',
        // diableHostChecks, otherwise we get an error about headers and the page won't render
        disableHostCheck: true,
        contentBase: 'packaging_additional_published_artifacts',
        // overlay shows a full-screen overlay in the browser when there are compiler errors or warnings
        overlay: true,
        proxy: [
            {
                context: [
                    '/api/v1/reviewMyTeacher/'
                ],
                target: 'http://localhost:5001'
            }
        ]
    },

    plugins: [
        new Dotenv(),
        // new CopyPlugin({
        //     patterns: [
        //         {
        //             from: path.resolve('src/css'),
        //             to: path.resolve("dist/css")
        //         }
        //     ]
        // }),
        new CleanWebpackPlugin()
    ]
}
