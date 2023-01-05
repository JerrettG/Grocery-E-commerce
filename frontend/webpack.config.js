const path = require('path');
const TerserPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const WarningsToErrorsPlugin = require('warnings-to-errors-webpack-plugin');
const {auto} = require("@popperjs/core");

module.exports = (env, argv) => ({
  entry: {
    app: './src/main/resources/js/app.js',
    index: './src/main/resources/js/pages/index.js',
    shoppingCart: './src/main/resources/js/pages/shoppingCart.js',
    checkout: './src/main/resources/js/pages/checkout.js',
    product: './src/main/resources/js/pages/product.js',
    success: './src/main/resources/js/pages/success.js',
    profile: './src/main/resources/js/pages/profile.js',
    order: './src/main/resources/js/pages/order.js',
    baseClass: './src/main/resources/js/util/baseClass.js',
    dataStore:'./src/main/resources/js/util/DataStore.js',
    cartServiceClient: './src/main/resources/js/api/cartServiceClient.js',
    customerProfileServiceClient: './src/main/resources/js/api/customerProfileServiceClient.js',
    orderServiceClient: './src/main/resources/js/api/orderServiceClient.js',
    productServiceClient: './src/main/resources/js/api/productServiceClient.js'
},
  output: {
    path: path.resolve(__dirname, './target/classes/static'),
    filename: 'js/[name].bundle.js'
  },
  devtool: argv.mode === 'production' ? false : 'eval-source-map',
  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin(),
      new CssMinimizerPlugin()
    ]
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: "css/bundle.css"
    }),
    new WarningsToErrorsPlugin(),
  ],
  module: {
    rules: [
      {
        test: /\.js$/,
        include: [
            path.resolve(__dirname, './src/main/resources/js'),
            path.resolve(__dirname, './src/main/resources/js/util'),
            path.resolve(__dirname, './src/main/resources/js/pages'),
            path.resolve(__dirname, './src/main/resources/js/api'),
        ],
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env'],
            plugins: ["@babel/plugin-transform-modules-commonjs"]
          },
        },
      },
      {
        test: /\.scss$/,
        include: path.resolve(__dirname, './src/main/resources/scss'),
        use: [
          argv.mode === 'production' ? MiniCssExtractPlugin.loader : 'style-loader',
          {
            loader: 'css-loader',
            options: {
              importLoaders: 1,
              sourceMap: true
            }
          },
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                plugins: [
                  require('autoprefixer'),
                ]
              },
              sourceMap: true
            }
          },
          {
            loader: 'sass-loader',
            options: { sourceMap: true }
          }
        ]
      }
    ]
  },
  resolve: {
    modules: [
      'node_modules',
    ],
  },
  devServer: {
    https: false,
    port: 8084,
    open: true,

    // diableHostChecks, otherwise we get an error about headers and the page won't render


    // overlay shows a full-screen overlay in the browser when there are compiler errors or warnings

    compress: true,
    proxy: {
      '/api': 'http://localhost:8090',
      '/': 'http://localhost:8074'
    },
    watchFiles: [
        'src/main/resources/js/**/*.js',
    ],
  }
});

