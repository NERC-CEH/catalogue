define [
  'cs!views/editor/SingleStringView'
  'cs!views/editor/InputView'
], (SingleStringView, InputView) -> SingleStringView.extend
  modelAttribute: 'title'
  label: 'Title'
  helpText: """
            <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
            <p>Jargon should be avoided so as to provide clarity to a broad audience from various specialisation across the public sector</p>
            <p>The leading letter and proper nouns of the title should be capitalised.</p>
            <p>If it's necessary to include acronyms in the formal title of a data resource, then include both the acronym (in parentheses) and its phrase or word from which it was formed.</p>
            <p>In the event that there are multiple titles, translations of titles (e.g. Welsh), and those with acronyms, these titles should be added as alternative titles</p>
            """

  initialize: ->
    do @render
    new InputView
      el: @$('.dataentry')
      model: @model
      modelAttribute: @modelAttribute
