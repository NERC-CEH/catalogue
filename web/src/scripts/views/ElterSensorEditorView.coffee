define [
  'underscore'
  'backbone'
  'tpl!templates/sensor.tpl'
], (
  _
  Backbone
  sensorTpl
) -> Backbone.View.extend
  initialize: ->
    do @model.fetch
    console.log 'jere'




  # templates:
  #   text: '<<%= wrapper %>><input class="update form-control" type="text" name="<%= field %>" value="<%= value %>" <%= required %> ></<%= wrapper %>>'
  #   link: '<<%= wrapper %>><input class="update form-control" type="text" name="<%= field %>" value="<%= value %>" <%= required %> ></<%= wrapper %>>'
  #   textarea: '<<%= wrapper %>><textarea class="update form-control" name="<%= field %>" <%= required %> ><%= value %></textarea></<%= wrapper %>>'
  #   processType: '<<%= wrapper %>><select class="update form-control" name="processType"><option value="Simulation">Simulation</option><option value="Manual">Manual</option><option value="Sensor">Sensor</option><option value="Algorithm">Algorithm</option><option value="Unknown">Unknown</option></select></<%= wrapper %>>'
  #   staticlist: '<<%= wrapper %>><% for(var i = 0; i < value.length; i++) { %><input class="update form-control form-list" type="text" name="<%= field %>[<%= i%>][\'<%= key %>\']" value="<%= value[i].value %>" <%= required %> ><% } %><input class="update form-control form-list" type="text" name="<%= field %>[<%= value.length %>][\'<%= key %>\']" <%= required %> ></<%= wrapper %>>'

  # editable: null

  # initialize: ->
  #   do @model.fetch
  #   do @initSync
  #   if $('.new-form').length == 0
  #     do @saveNew
  #   else
  #     do @initForm
  #     do @initDelete
  #   do @initEditable
  #   do @initEdit

  # initEdit: ->
  #   $('.toggle-edit').click ->
  #     $('.form-editable').toggleClass('is-active')
  #     if $('.form-editable').hasClass('is-active')
  #       $('.toggle-edit .edit').hide()
  #       $('.toggle-edit .cancel').show()
  #     else
  #       $('.toggle-edit .edit').show()
  #       $('.toggle-edit .cancel').hide()
  
  # initSync: ->
  #   @model.on 'sync', =>
  #     for key, value of @model.attributes
  #       $('#' + key + '-value').html(value)
  #       if @editable and @editable.data('field') == key
  #         @editable.find('input').attr('disabled', no)
  #         @editable.find('input').val(value)
  #         @editable.data('value', value)
  #         if @editable.data('type') == 'link'
  #           $('#' + key + '-value').attr('href', value)
  #         @editable = null
      
  #     if @editable != null
  #       $('#' + @editable.data('field') + '-value').html(@editable.data('defatulValue'))
  #       if @editable.data('type') == 'link'
  #           $('#' + key + '-value').attr('href', '#')
  #       @editable.find('input').attr('disabled', no)
  #       @editable.find('input').val('')
  #       @editable.data('value', '')
  #       @editable = null

  # saveNew: ->
  #   $('.search').html(sensorTpl())

  # initForm: ->
  #   $('form').submit (event) =>
  #     $.ajax
  #       type: 'POST'
  #       url: $('form').attr('action')
  #       data: $('form').serialize()
  #       success: =>
  #         do $('.update').remove
  #         if @editable
  #           field = @editable.data('field')
  #           do $('#form-value-' + field).show
  #         do @model.fetch
  #         do @initEditable
  #     do event.preventDefault
  
  # initDelete: ->
  #   $('.delete').click =>
  #     @model.destroy
  #       success: ->
  #         Backbone.history.location.replace "/elter/documents"

  # initEditable: ->
  #   $('.form-editable').click (evt) =>
  #     $('.form-editable').unbind 'click'
  #     @editable = @getEditable(evt.target)
  #     @editable.find('input').attr('disabled', 'disabled')

  #     field = @editable.data('field')
  #     key = @editable.data('key')
  #     type = @editable.data('type')
  #     wrapper = @editable.data('wrapper') || 'div'
  #     required = @editable.data('isRequired')
  #     value = @model.attributes[field]

  #     value = value || [] if type == 'staticlist'

  #     do $('#form-value-' + field).hide

  #     @editable.append(_.template(@templates[type])(
  #       wrapper: wrapper
  #       field: field
  #       value: value
  #       required: required
  #       key: key
  #     ))
  #     do $('.update').focus
  #     # $('.update').blur ->
  #     #   newValue = $('.update').val()
  #     #   $('.update').val(value) if newValue == ''
  #     #   $('form').submit()
      

  # getEditable: (el) ->
  #   el = $(el)
  #   if !el.hasClass('form-editable')
  #     @getEditable(el.parent())
  #   else
  #     el