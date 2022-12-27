const path = require('path');
const TerserPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const WarningsToErrorsPlugin = require('warnings-to-errors-webpack-plugin');
const {auto} = require("@popperjs/core");


module.exports = (env, argv) => ({
  entry: [
      './src/main/resources/js/app.js',
    './src/main/resources/js/pages/shoppingCart.js',
    './src/main/resources/js/util/baseClass.js',
    './src/main/resources/js/util/DataStore.js',
      './src/main/resources/js/api/cartServiceClient.js'
  ],
  output: {
    path: path.resolve(__dirname, './target/classes/static'),
    filename: 'js/app.js'
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
    new WarningsToErrorsPlugin()
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
      '/': {
        target: 'http://localhost:8074'
      },
      '/api/v1': {
        target: 'http://localhost:8090',
      }
    },
    watchFiles: [
        'src/main/resources/templates/**/*.html',
        'src/main/resources/js/**/*.js',
        'src/main/resources/static/css/*.css'
    ],
  }
});

