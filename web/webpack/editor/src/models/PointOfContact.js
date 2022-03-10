import _ from 'underscore'
import Contact from './Contact'

export default Contact.extend({ defaults: _.extend({}, Contact.prototype.defaults, { role: 'pointOfContact' }) })

({
  intialize () {
    return Contact.prototype.initialize.apply(this)
  }
})
