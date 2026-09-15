import Backbone from 'backbone'

export default Backbone.View.extend({

  events: {
    "keyup  [name='term']": 'handleTyping',
    "change [name='term']": 'handleTyping',
    "paste  [name='term']": 'handleTyping',
    "input  [name='term']": 'handleTyping',
    "change [name='semantic']": 'handleSemanticToggle',
    submit: 'handleSubmit'
  },

  initialize () {
    this.listenTo(this.model, 'change:term', this.updateDisplayedTerm)
    this.listenTo(this.model, 'change:semantic', this.updateSemanticCheckbox)
    this.listenTo(this.model, 'change:semantic', this.updateTermControl)

    // The model may already carry state read out of the query string, so bring the
    // form into line with it before the user touches anything
    this.updateSemanticCheckbox()
    this.updateTermControl()
  },

  /*
     * Is the search in semantic (natural language) mode rather than keyword mode?
     */
  isSemantic () {
    return this.model.get('semantic') === true
  },

  /*
     * Event listener for changed input in the search term box.
     *
     * In keyword mode this instantly clears the results (as these will now be
     * dirty) and sets the term on the model, which starts a (debounced) search.
     *
     * In semantic mode the term is deliberately NOT put on the model: every search
     * costs a Bedrock call to embed the query, so we hold the term back until the
     * user presses the search button. The results already on the page are left
     * alone (blanking them would leave nothing to look at until the button is
     * pressed) and the form is flagged as pending instead.
     */
  handleTyping () {
    if (this.getDisplayedTerm() !== this.model.get('term')) {
      if (this.isSemantic()) {
        this.setTermPending(true)
      } else {
        this.model.clearResults()
        this.updateTermOnModel()
      }
    } else {
      this.setTermPending(false)
    }
  },

  /*
     * The search form has been submitted, either by pressing the search button or
     * by pressing enter in the keyword box. Stop the browser navigating and commit
     * the displayed term to the model, which normally starts a search by way of the
     * change event.
     *
     * When the model already holds the submitted term, Backbone fires no change event
     * and nothing would happen at all, so the search is started explicitly instead.
     * That is not an edge case in semantic mode: the term is withheld until submit, so
     * the button is the only trigger there, and it is dead whenever the term was already
     * committed -- by a previous press, or by the semantic checkbox, which commits the
     * displayed term as it toggles.
     */
  handleSubmit (e) {
    e.preventDefault()
    const alreadyCommitted = this.getDisplayedTerm() === this.model.get('term')
    this.updateTermOnModel()
    if (alreadyCommitted) { this.model.searchNow() }
  },

  /*
     * Reads the current term from the search box and sets it onto the model
     */
  updateTermOnModel () {
    this.setTermPending(false)
    this.model.set('term', this.getDisplayedTerm())
  },

  /*
     * The term control which is currently in use. Both the keyword input and the
     * semantic textarea are in the markup; the one which is not in use is disabled
     * so that it is neither focusable nor serialized into the query string.
     */
  activeTermControl () {
    return this.$("[name='term']").not(':disabled')
  },

  /*
     * Obtains the current term from the search box
     */
  getDisplayedTerm () {
    return this.activeTermControl().val()
  },

  /*
     * Update the term boxes based upon the content in the model. Both controls are
     * written to, so whichever one is shown next is already up to date.
     */
  updateDisplayedTerm () {
    this.setDisplayedTerm(this.model.get('term') || '')
    this.setTermPending(false)
  },

  /*
     * Only write to a control whose value has actually changed. Assigning to the
     * value of a focused input moves the caret to the end, which would fight the
     * user as they type in keyword mode (where every keystroke round trips through
     * the model and back).
     */
  setDisplayedTerm (term) {
    this.$("[name='term']").each((i, control) => {
      if (control.value !== term) { control.value = term }
    })
  },

  /*
     * Move focus to whichever term control is currently in use
     */
  focusTerm () {
    this.activeTermControl().trigger('focus')
  },

  /*
     * A semantic query is a sentence rather than a handful of keywords, so swap the
     * single line input for a multi row textarea (and back again). The term typed
     * so far is carried across so that toggling the checkbox never loses it.
     */
  updateTermControl () {
    const semantic = this.isSemantic()
    const term = this.getDisplayedTerm() || ''

    this.toggleTermControl(this.$("input[name='term']"), !semantic)
    this.toggleTermControl(this.$("textarea[name='term']"), semantic)
    this.setDisplayedTerm(term)

    this.$el.toggleClass('semantic-mode', semantic)
    if (!semantic) { this.setTermPending(false) }
  },

  toggleTermControl ($control, active) {
    $control.prop('disabled', !active).toggleClass('d-none', !active)
  },

  /*
     * Flag that the displayed term has not been searched for yet, so that the
     * search button can be highlighted as the thing to press next
     */
  setTermPending (pending) {
    this.$el.toggleClass('term-pending', pending)
  },

  /*
     * The semantic checkbox has been toggled. Commit the displayed term at the same
     * time so that a semantic query which was typed but never submitted is not
     * silently dropped when the user switches back to keyword search.
     */
  handleSemanticToggle () {
    this.model.set({
      semantic: this.$("[name='semantic']").is(':checked'),
      term: this.getDisplayedTerm()
    })
  },

  updateSemanticCheckbox () {
    this.$("[name='semantic']").prop('checked', this.isSemantic())
  }
})
