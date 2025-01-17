import Backbone from 'backbone'

export default Backbone.Model.extend({
  defaults: {
    name: '',
    standardName: '',
    longName: '',
    units: ''
  }
})
