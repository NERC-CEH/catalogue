define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/service-agreement/EndUserLicence.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  className: 'col-sm-3'

  events:
    'change .ogl': 'setOGL'
    'change .other': 'setOther'
    'change .value': 'setValue'
    'change .uri': 'setUri'

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, options

    if @model.attributes.value? && @model.attributes.value != 'This resource is available under the terms of the Open Government Licence'
      @licence =
        value:@model.attributes.value,
        code:'license',
        uri:''
      @$('#other').prop('checked')
      @$('#ogl').prop 'checked', false
      @$('#resourceConstraint').removeClass('hidden')
      @$( ".value" ).append(@licence.value)
    else
      @licence =
        value:'This resource is available under the terms of the Open Government Licence',
        code:'license',
        uri:'https://eidc.ceh.ac.uk/licences/OGL/plain'
      @$('#other').prop 'checked', false
      @$('#ogl').prop('checked')
      @setOGL()

  render: ->
    ObjectInputView.prototype.render.apply @
    @

  setOGL: ->
    @$('#resourceConstraint').addClass('hidden')
    @licence.value = 'This resource is available under the terms of the Open Government Licence'
    @licence.uri = 'https://eidc.ceh.ac.uk/licences/OGL/plain'
    @model.set @licence

  setOther: ->
    @$('#resourceConstraint').removeClass('hidden')
    @licence.value = ''
    @licence.uri = ''

  other: ->
    @$('#resourceConstraint').removeClass('hidden')

  setValue:(event) ->
    @licence.value = event.target.value
    @model.set(@licence)

  setUri:(event) ->
    @licence.uri = event.target.value
    @model.set(@licence)

