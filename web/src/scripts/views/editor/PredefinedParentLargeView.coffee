define [
  'underscore'
  'cs!views/editor/ParentLargeView'
  'tpl!templates/editor/PredefinedParent.tpl'
  'tpl!templates/editor/PredefinedParentDropdown.tpl'
], (_, ParentLargeView, template, dropdownTemplate) -> ParentLargeView.extend

  events:
    'click .dropdown-menu': 'setPredefined'

  render: ->
    ParentLargeView.prototype.render.apply @
    @$('button.add').replaceWith template data: @data
    @$('button').prop @data.disabled, @data.disabled
    $dropdown = @$('ul.dropdown-menu')
    _.chain(@data.predefined)
    .keys()
    .each (item) ->
      $dropdown.append dropdownTemplate predefined: item
    @

  setPredefined: (event) ->
    do event.preventDefault
    value = $(event.target).text()
    selected = {}

    if value != 'Custom'
      selected = @data.predefined[value]

    @collection.add selected
