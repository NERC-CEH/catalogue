define [
  'underscore'
  'cs!views/editor/ParentView'
  'tpl!templates/editor/PredefinedParent.tpl'
  'tpl!templates/editor/PredefinedParentDropdown.tpl'
], (_, ParentView, template, dropdownTemplate) -> ParentView.extend

  events:
    'click .dropdown-menu': 'setPredefined'

  render: ->
    ParentView.prototype.render.apply @
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
