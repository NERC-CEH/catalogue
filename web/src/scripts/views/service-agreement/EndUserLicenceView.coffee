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
    'change .code': 'setCode'
    'change .input': 'setUri'

  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    @listenTo(@model, 'sync', @remove)
    @listenTo(@model, 'change', @render)

  setOGL: ->
    @$('#resourceConstraint').addClass('hidden')
    model.set( @modelAttribute,
      value:'This resource is available under the terms of the Open Government Licence',
      code:'https://eidc.ceh.ac.uk/licences/OGL/plain',
      uri:'license')

  setOther: ->
    @$('#resourceConstraint').removeClass('hidden')

  setValue:(event) ->
    model.set(@modelAttribute,
      {value:event.target})

  setCode:(event) ->
    model.set(@modelAttribute,
      {code:event.target})

  setUri:(event) ->
    model.set(@modelAttribute,
      {uri:event.target})

