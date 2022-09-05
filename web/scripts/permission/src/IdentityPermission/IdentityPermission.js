import Backbone from 'backbone'

export default Backbone.Model.extend({
  idAttribute: 'identity',

  defaults: {
    canView: false,
    canEdit: false,
    canDelete: false,
    canUpload: false
  }
})
