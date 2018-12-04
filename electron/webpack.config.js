var webpack = require('webpack')
var path = require('path')

const { CheckerPlugin } = require('awesome-typescript-loader')
const HtmlWebpackPlugin = require('html-webpack-plugin')

function root(args) {
    args = Array.prototype.slice.call(arguments, 0)
    return path.join.apply(path, [__dirname].concat(args))
}

module.exports = {
    resolve: {
        extensions: ['.ts', '.js', '.html']
    },
    module: {
        rules: [{
            test: /\.js$/,
            enforce: 'pre'
        }, {
            test: /\.ts$/,
            loader: 'awesome-typescript-loader'
        }]
    },

    entry: {
        'app': './src/main.ts'
    },

    devServer: {
        watchOptions: {
            poll: true
        },
        stats: {
            modules: false,
            cached: false,
            colors: true,
            chunks: false
        }
    },

    output: {
        path: root('dist'),
        filename: '[name].[hash].js',
        chunkFilename: '[id].[hash].chunk.js'
    },

    plugins: [
        // fix the warning in ./~/@angular/core/src/linker/system_js_ng_module_factory_loader.js
        new webpack.ContextReplacementPlugin(
            /angular(\\|\/)core(\\|\/)(esm(\\|\/)src|src)(\\|\/)linker/,
            root('./src')
        ),

        new webpack.ProvidePlugin({
            'window.Quill': 'quill/dist/quill.js'
        }),

        new HtmlWebpackPlugin({
            template: root('index.html'),
            chunksSortMode: 'dependency'
        }),

        new webpack.optimize.OccurrenceOrderPlugin(true),

        new CheckerPlugin()
    ]
}