const path = require('path');
const TerserPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const WarningsToErrorsPlugin = require('warnings-to-errors-webpack-plugin');


module.exports = (env, argv) => ({
  entry: [
      './src/main/resources/js/app.js',
    // './src/main/resources/static/api/cartServiceClient.js',
    // './src/main/resources/static/util/baseClass.js',
    // './src/main/resources/static/util/DataStore.js'
  ],
  output: {
    path: path.resolve(__dirname, './target/classes/static'),
    filename: 'js/bundle.js'
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
        include: path.resolve(__dirname, './src/main/resources/js'),
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env'],
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
      path.resolve(__dirname, './src/main/resources'),
      'node_modules'
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
