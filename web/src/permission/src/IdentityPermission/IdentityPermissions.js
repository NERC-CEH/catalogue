import Backbone from 'backbone'
import IdentityPermission from './IdentityPermission'

export default Backbone.Collection.extend({

    model: IdentityPermission,
    comparator: 'identity'

})
