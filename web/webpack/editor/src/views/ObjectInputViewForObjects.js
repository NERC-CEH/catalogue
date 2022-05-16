import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import $ from 'jquery'

export default ObjectInputView.extend({

  // Copes with an object made up of objects
  // So, one further level of objects than ObjectInputView
  // The template <input data-name="objectName.attributeName" …
  modify (event) {
    const $target = $(event.target)
    const [objectName, attributeName] = Array.from($target.data('name').split('.'))
    const value = $target.val()
    this._setObject(objectName, attributeName, value)
    return false
  }, // disable bubbling

  _setObject (objectName, attributeName, value) {
    if (!value) {
      return this.model.unset(objectName)
    } else {
      if (!_.isUndefined(attributeName)) {
        const obj = _.extend({}, this.model.get(objectName))
        obj[attributeName] = value
        return this.model.set(objectName, obj)
      } else {
        return this.model.set(objectName, value)
      }
    }
  }
})