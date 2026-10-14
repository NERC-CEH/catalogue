// The micro-apps' code. Each is a chunk of its own, fetched by import() only on a page that has the app:
// a public record page doesn't download the editor, the maps or the upload tables (NERC-CEH/dri-one#364).
// They're one object so that the start-up specs can replace a loader with spyOn rather than load the
// chunk (dri-one #473). Each resolves to the classes its app uses.
export const load = {
  editor: () => Promise.all([
    import(/* webpackChunkName: "editor" */ '../editor/src/editors'),
    import(/* webpackChunkName: "editor" */ '../editor/src'),
    import(/* webpackChunkName: "editor" */ '../editor/src/models')
  ]).then(([editors, src, models]) => ({ ...editors, ...src, ...models })),

  catalogueApp: () => import(/* webpackChunkName: "catalogue" */ '../catalogue/src/CatalogueApp'),

  studyArea: () => import(/* webpackChunkName: "study-area" */ '../study-area/src/View'),

  messageView: () => import(/* webpackChunkName: "message" */ '../search/src/views/MessageView')
    .then(module => module.default),

  swal: () => import(/* webpackChunkName: "sweetalert" */ 'sweetalert2').then(module => module.default),

  permission: () => import(/* webpackChunkName: "permission" */ '../permission/src/PermissionApp'),

  search: () => Promise.all([
    import(/* webpackChunkName: "search" */ '../search/src/SearchApp'),
    import(/* webpackChunkName: "search" */ '../search/src/SearchAppView'),
    import(/* webpackChunkName: "search" */ '../search/src/SearchRouter')
  ]).then(([app, view, router]) => ({ SearchApp: app.default, SearchAppView: view.default, SearchRouter: router.default })),

  searchRouter: () => import(/* webpackChunkName: "search" */ '../search/src/SearchRouter').then(module => module.default),

  simpleUpload: () => import(/* webpackChunkName: "simple-upload" */ '../simple-upload/src/App'),

  hubbub: () => import(/* webpackChunkName: "hubbub" */ '../hubbub/src/Upload'),

  metrics: () => import(/* webpackChunkName: "metrics" */ '../metrics/src'),

  depositRequest: () => Promise.all([
    import(/* webpackChunkName: "deposit-request" */ '../deposit-request/src/model/DepositRequestApp'),
    import(/* webpackChunkName: "deposit-request" */ '../deposit-request/src/view/DepositRequestFormView')
  ]).then(([app, view]) => ({ DepositRequestApp: app.default, DepositRequestFormView: view.default }))
}
