import './models/SearchApp'
import './views/SearchAppView.coffee'
import './routers/SearchRouter'

const app    = new SearchApp()
const view   = new SearchAppView({model: app})
const router = new SearchRouter({model: app, location: window.location})
