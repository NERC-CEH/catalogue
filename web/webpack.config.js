const path = require('path');

module.exports = {
  entry: {
    search: './src/scripts/routers/SearchRouter.js'
  },
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: '[name].bundle.js'
  },
  mode:  'development',
};