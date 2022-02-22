import Backbone from 'backbone'
import File from './File'

export default Backbone.Collection.extend({
  model: File,

  comparator: 'name',

  initialize (options) {
    this.url = options.url
  }
})
