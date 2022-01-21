import Backbone from 'backbone'

export var IdentityPermission = Backbone.Model.extend ({
    idAttribute: "identity",

    defaults: {
        canView: false,
        canEdit: false,
        canDelete: false,
        canUpload: false
    }
})