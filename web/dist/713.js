"use strict";
(self["webpackChunkweb"] = self["webpackChunkweb"] || []).push([[713],{

/***/ 1713:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXTERNAL MODULE: ./node_modules/jquery/dist/jquery.js
var jquery = __webpack_require__(9755);
var jquery_default = /*#__PURE__*/__webpack_require__.n(jquery);
// EXTERNAL MODULE: ./node_modules/backbone/backbone.js
var backbone = __webpack_require__(2316);
var backbone_default = /*#__PURE__*/__webpack_require__.n(backbone);
// EXTERNAL MODULE: ./node_modules/underscore/modules/index-all.js
var index_all = __webpack_require__(8860);
// EXTERNAL MODULE: ./node_modules/leaflet/dist/leaflet-src.js
var leaflet_src = __webpack_require__(5243);
var leaflet_src_default = /*#__PURE__*/__webpack_require__.n(leaflet_src);
;// CONCATENATED MODULE: ./study-area/src/View/StudyAreaView.js






/* harmony default export */ const StudyAreaView = (backbone_default().View.extend({
  initialize: function initialize() {
    (leaflet_src_default()).Icon.Default.imagePath = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/'; // fix for leaflet draw marker bug

    this.render();
  },
  createMap: function createMap() {
    this.overlay = leaflet_src_default().featureGroup();
    var studyArea = JSON.parse(this.getStudyArea()[0]);
    var polygon = leaflet_src_default().geoJson(studyArea);
    var center = polygon.getBounds().getCenter();
    var map = new (leaflet_src_default()).Map(jquery_default()('#studyarea-map')[0], {
      center: center,
      zoom: 4
    });
    var baseMaps = {
      Map: leaflet_src_default().tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(map),
      Satellite: leaflet_src_default().tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    };
    leaflet_src_default().control.layers(baseMaps, {}, {
      position: 'topright',
      collapsed: false
    }).addTo(map);
    polygon.addTo(map);
  },
  getStudyArea: function getStudyArea() {
    var studyArea = this.$('[dataType="geoJson"]');
    return index_all/* default.map */.ZP.map(studyArea, function (el) {
      return jquery_default()(el).attr('content');
    });
  },
  render: function render() {
    this.createMap();
    return this;
  }
}));
;// CONCATENATED MODULE: ./study-area/src/View/index.js

;// CONCATENATED MODULE: ./study-area/src/bootstrap.js


var view = new StudyAreaView({
  el: '#studyarea-map'
});

/***/ })

}]);