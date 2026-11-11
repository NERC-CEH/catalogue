import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/author'
import { createOrgAutocomplete } from '../../utils'
import { normalisePersonIdentifier } from '../../utils/orcid'
import { createPersonLookup } from '../../utils/peopleUtils'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    createOrgAutocomplete('.orgAutocomplete', this)
    createPersonLookup(this)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.honorificPrefix').val(this.model.get('honorificPrefix'))
    return this
  },

  modify (event) {
    // An iD pasted bare, or from http://, is kept as https://orcid.org/… (dri-one #534)
    if (event.target.dataset.name === 'nameIdentifier') {
      event.target.value = normalisePersonIdentifier(event.target.value)
    }
    ObjectInputView.prototype.modify.call(this, event)
    this.model.set('role', 'author')
  }
})
