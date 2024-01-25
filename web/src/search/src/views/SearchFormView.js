import _ from 'underscore'
import Backbone from 'backbone'

export default Backbone.View.extend({

  events: {
    "keyup  [name='term']": 'handleTyping',
    "change [name='term']": 'handleTyping',
    "paste  [name='term']": 'handleTyping',
    "input  [name='term']": 'handleTyping',
    submit: 'handleSubmit'
  },

  initialize () {
    this.listenTo(this.model, 'change:term', this.updateDisplayedTerm)
  },

  /*
     * Event listener for changed input in the search term box. This will instantly
     * clear the results (as these will now be dirty). After input has stopped, the
     * term will be set on the model
     */
  handleTyping () {
    if (this.getDisplayedTerm() !== this.model.get('term')) {
      this.model.clearResults()
      _.debounce(this.updateTermOnModel(), 500)
    }
  },

  /*
     * The search form has been submitted. We can update the search term right away
     */
  handleSubmit (e) {
    this.updateTermOnModel()
    e.preventDefault()
  },

  /*
     * Reads the current term from the search box and sets it onto the model
     */
  updateTermOnModel () {
    this.model.set('term', this.getDisplayedTerm())
  },

  /*
     * Obtains the current term from the search box
     */
  getDisplayedTerm () {
    return this.$("[name='term']").val()
  },

  /*
     * Update the term box based upon the content in the model
     */
  updateDisplayedTerm () {
    const term = this.model.get('term')
    const displayed = this.$("[name='term']")
    if (term !== displayed) {
      this.$("[name='term']")
    }
  }
})
