define [], () -> (view, model) ->
  showOther = (name) ->
    $('.other-' + name).removeClass('is-hidden')
    $('.other-' + name).addClass('is-visible')
    $('.other-' + name + ' input').val('')
    $('.other-' + name + ' input').prop('disabled', no)
    $('.other-' + name + ' input').focus()

  hideOther = (name) ->
    $('.other-' + name).addClass('is-hidden')
    $('.other-' + name).removeClass('is-visible')
    $('.other-' + name + ' input').val('')
    $('.other-' + name + ' input').prop('disabled', yes)
    $('.other-' + name + ' input').blur()

  initOtherable = (name) ->
    $('#' + name).unbind 'change'
    $('#' + name).change ->
      hideOther  name
      value = $('#' + name).val()
      model.set(name, value)
      if value == 'other'
        showOther name
      else
        hideOther name
        do view.save
  
  updateOtherable = (name, url, renderValue, renderOther) ->
    if $('#' + name).length
      $.ajax
        type: 'GET'
        url: url
        headers:
          Accept: "application/json"
        success: (values) ->
          hideOther name
          $('#' + name).html('')
          for index, value of values
            renderValue(value)
          renderOther()
          $('#' + name).val(model.get(name))

  updateManufacturer = ->
    hideOther 'manufacturer'
    updateOtherable 'manufacturer',
      '/elter/manufacturers',
      (manufacturer) -> $('#manufacturer').append('<option value="' + manufacturer.id + '">' + manufacturer.title + '</option>'),
      -> $('#manufacturer').append('<option id="other-manufacturer" value="other"">Other</option>')
    initOtherable 'manufacturer'

  updateFoiType = ->
    $('#foi-type').unbind 'change'
    $('#foi-type').change ->
        $('#blank-option').remove()
        value = $('#foi-type').val()
        model.set('foiType', value)
        do view.save
        $('.foi-data').removeClass('is-notype')
        $('.foi-data').removeClass('is-monitoring')
        $('.foi-data').removeClass('is-verticalmonitoring')
        $('.foi-data').removeClass('is-composite')
        $('.foi-data').addClass('is-' + value.toLowerCase())

  updateReplacedBy = ->
    hideOther 'replaceBy'
    initOtherable 'replaceBy'

  ->
    do updateManufacturer
    do updateFoiType
    do updateReplacedBy
