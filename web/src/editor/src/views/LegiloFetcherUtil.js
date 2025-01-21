import $ from 'jquery'

export function fetchStartDisplay (classId) {
  $('.' + classId + '-btn').prop('disabled', true)
  $('.' + classId + '-loader').show()
  $('.' + classId + '-loader-msg')
    .removeClass('text-primary')
    .text('It may take a while.')
}

export function fetchSuccessDisplay (classId) {
  $('.' + classId + '-loader').hide()
  $('.' + classId + '-loader-msg').text('')
  $('.' + classId + '-btn').prop('disabled', false)
}

export function fetchFailDisplay (classId, error) {
  let errText = 'Something went wrong while fetching suggestions.'
  if (error.status === 500) {
    errText = 'Service not reachable, please try again later.'
  } else if (error.status === 422) {
    errText = 'No documents available for this dataset extraction.'
  } else if (error.status === 404) {
    errText = 'Suggestion service not available.'
  } else {
    if (error.responseJSON) {
      const response = error.responseJSON
      if (response.error) {
        errText = response.error
      } else if (response.message) {
        errText = response.message
      }
    }
  }
  $('.' + classId + '-loader').hide()
  $('.' + classId + '-loader-msg')
    .addClass('text-primary')
    .text(errText)
  $('.' + classId + '-btn').prop('disabled', false)
}
