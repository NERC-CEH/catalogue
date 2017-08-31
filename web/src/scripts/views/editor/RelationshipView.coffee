define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Relationship.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template:  template

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>')

  initialize: (options) ->
    @options = options.options
    ObjectInputView.prototype.initialize.call @, options

  render: ->
    ObjectInputView.prototype.render.apply @
    $list = @$('datalist')
    @options.forEach (option) =>
      $list.append @optionTemplate option
    @