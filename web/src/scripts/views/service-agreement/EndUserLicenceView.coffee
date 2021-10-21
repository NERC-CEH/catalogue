define [
  'cs!views/editor/ObjectInputView'
  'cs!views/editor/ResourceConstraintView'
  'tpl!templates/service-agreement/EndUserLicence.tpl'
], (ObjectInputView, ResourceConstraintView, template) -> ObjectInputView.extend

  template: template

  className: 'col-sm-3'

  events:
    'change .ogl': 'setOGL'
    'change .other': 'setOther'
    'change .value': 'setValue'
    'change .uri': 'setUri'

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, options
    @licence =
      value:'This resource is available under the terms of the Open Government Licence',
      code:'license',
      uri:'https://eidc.ceh.ac.uk/licences/OGL/plain'
    @setOGL()

  render: ->
    ObjectInputView.prototype.render.apply @
    @

  setOGL: ->
    @$('#resourceConstraint').addClass('hidden')
    @model.set @licence

  setOther: ->
    @$('#resourceConstraint').removeClass('hidden')

  setValue:(event) ->
    @licence.value = event.target.value
    @model.set(@licence)

  setUri:(event) ->
    @licence.uri = event.target.value
    @model.set(@licence)

