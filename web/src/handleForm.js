import $ from 'jquery';

$(document).ready(function() {
  $('#metricsForm').on('submit', function(event) {
    event.preventDefault();

    var formData = $(this).serialize();
    console.log($(this).attr('action'));

    $.ajax({
      type:'POST',
      url: $(this).attr('action'),
      data: formData,
      dataType: 'json',
      success: function(response) {

        $('#metricsTable tbody').empty();

        if(response.metricsReport && response.metricsReport.length > 0) {
          response.metricsReport.forEach(function(record) {
            var row = '<tr>' +
               '<td>' + record.document + '</td>' +
               '<td>' + record.doc_title + '</td>' +
               '<td>' + record.record_type + '</td>' +
               '<td>' + record.views + '</td>' +
               '<td>' + record.downloads + '</td>' +
              '</tr>';
            $('#metricsTable tbody').append(row);
          })

            $('#metricsResultsContainer').show();
          } else {
            $('#metricsResultsContainer').hide();
          }
      },
      error: function (error) {
        console.log("Error", error);
      }
    });
  });
});
