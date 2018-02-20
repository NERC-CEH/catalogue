define [
    'tpl!templates/ManufacturerSelect.tpl'
], (
  Manufacturer
) -> (view, model) ->
  initManufacturer = ->
    $('#manufacturer').unbind 'change'
    $('#manufacturer').change ->
      $('.other-manufacturer').remove()
      value = $('#manufacturer').val()
      model.set('manufacturer', value)
      if value == 'other'
        $('#manufacturer').parent().after(Manufacturer())
        do view.initInputs
        do initManufacturer
      else
        do view.save

  update = ->
    $('.other-manufacturer').remove()

    if $('#manufacturer').length
      $.ajax
        type: 'GET'
        url: '/elter/manufacturers'
        headers:
          Accept: "application/json"
        success: (manufacturers) ->
          $('#manufacturer').html('')
          for index, manufacturer of manufacturers
            $('#manufacturer').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>')
          $('#manufacturer').append('<option id="other-manufacturer" value="other"">Other</option>')
          $('#manufacturer').val(model.get('manufacturer'))
    
    do initManufacturer

  update