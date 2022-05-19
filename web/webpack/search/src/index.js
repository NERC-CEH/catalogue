import $ from 'jquery'
import SearchApp from './SearchApp'
import SearchAppView from './SearchAppView'
import SearchRouter from './SearchRouter'
import { MessageView } from './views'

$(document).ready(function () {
  if ($('#search').length) {
    /*
      Initialize the search
      */

    const app = new SearchApp()
    const view = new SearchAppView({ model: app })
    const router = new SearchRouter({ model: app, location: window.location })

    const messageView = new MessageView({ model: app })
  }
})
