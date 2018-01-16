define [
  'underscore'
  'backbone'
  'tpl!templates/sensor.tpl'
], (
  _
  Backbone
  sensorTpl
) -> Backbone.View.extend
  templates:
    text: '<input id="update" class="form-control" type="text" name="<%= field %>" value="<%= value %>" <%= required %> >'
    link: '<input id="update" class="form-control" type="text" name="<%= field %>" value="<%= value %>" <%= required %> >'
    textarea: '<textarea id="update" class="form-control" name="<%= field %>" <%= required %> ><%= value %></textarea>'
    processType: '<select id="update" class="form-control" name="processType"><option value="Simulation">Simulation</option><option value="Manual">Manual</option><option value="Sensor">Sensor</option><option value="Algorithm">Algorithm</option><option value="Unknown">Unknown</option></select>'

  initialize: ->
    do @initSync
    if $('.new-form').length == 0
      do @saveNew
    else
      do @initForm
    do @initEdit
  
  initSync: ->
    @model.on 'sync', =>
      for key, value of @model.attributes
        $('#' + key + '-value').html(value)
        if @editable and @editable.data('field') == key
          @editable.find('input').attr('disabled', no)
          @editable.find('input').val(value)
          @editable.data('value', value)
          if @editable.data('type') == 'link'
            $('#' + key + '-value').attr('href', value)
          @editable = null
      
      if @editable != null
        $('#' + @editable.data('field') + '-value').html(@editable.data('defatulValue'))
        if @editable.data('type') == 'link'
            $('#' + key + '-value').attr('href', '#')
        @editable.find('input').attr('disabled', no)
        @editable.find('input').val('')
        @editable.data('value', '')
        @editable = null

  saveNew: ->
    $('.search').html(sensorTpl())

  initForm: ->
    $('form').submit (event) =>
      $.ajax(
        type: 'POST'
        url: $('form').attr('action')
        data: $('form').serialize()
        success: =>
          do $('#update').remove
          if @editable
            field = @editable.data('field')
            do $('#form-value-' + field).show
          do @model.fetch
          do @initEdit
      )
      do event.preventDefault

  initEdit: ->
    $('.form-editable').click (evt) =>
      $('.form-editable').unbind 'click'
      @editable = @getEditable(evt.target)
      @editable.find('input').attr('disabled', 'disabled')

      field = @editable.data('field')
      type = @editable.data('type')
      value = @editable.data('value')
      required = @editable.data('isRequired')

      do $('#form-value-' + field).hide

      @editable.append(_.template(@templates[type])(
        field: field
        value: value
        required: required
      ))
      do $('#update').focus
      $('#update').blur ->
        newValue = $('#update').val()
        $('#update').val(value) if newValue == ''
        $('form').submit()
      

  getEditable: (el) ->
    el = $(el)
    if !el.hasClass('form-editable')
      @getEditable(el.parent())
    else
      el