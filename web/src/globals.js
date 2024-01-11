/* global window */
// Hack to expose jQuery globally
import $ from 'jquery'
// Add jQuery UI widgets to jQuery
import 'jquery-ui/ui/widgets/autocomplete'
import 'jquery-ui/ui/widgets/sortable'
window.jQuery = $
