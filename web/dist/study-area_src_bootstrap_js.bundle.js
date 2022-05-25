"use strict";
/*
 * ATTENTION: The "eval" devtool has been used (maybe by default in mode: "development").
 * This devtool is neither made for production nor for readable output files.
 * It uses "eval()" calls to create a separate source file in the browser devtools.
 * If you are trying to read the output file, select a different devtool (https://webpack.js.org/configuration/devtool/)
 * or disable the default devtool with "devtool: false".
 * If you are looking for production-ready output files, see mode: "production" (https://webpack.js.org/configuration/mode/).
 */
(self["webpackChunkwebpack"] = self["webpackChunkwebpack"] || []).push([["study-area_src_bootstrap_js"],{

/***/ "./study-area/src/View/StudyAreaView.js":
/*!**********************************************!*\
  !*** ./study-area/src/View/StudyAreaView.js ***!
  \**********************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

eval("__webpack_require__.r(__webpack_exports__);\n/* harmony export */ __webpack_require__.d(__webpack_exports__, {\n/* harmony export */   \"default\": () => (__WEBPACK_DEFAULT_EXPORT__)\n/* harmony export */ });\n/* harmony import */ var backbone__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! backbone */ \"./node_modules/backbone/backbone.js\");\n/* harmony import */ var backbone__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(backbone__WEBPACK_IMPORTED_MODULE_0__);\n/* harmony import */ var underscore__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! underscore */ \"./node_modules/underscore/modules/index-all.js\");\n/* harmony import */ var leaflet_draw_dist_leaflet_draw_src_css__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! leaflet-draw/dist/leaflet.draw-src.css */ \"./node_modules/leaflet-draw/dist/leaflet.draw-src.css\");\n/* harmony import */ var leaflet_dist_leaflet_css__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! leaflet/dist/leaflet.css */ \"./node_modules/leaflet/dist/leaflet.css\");\n/* harmony import */ var leaflet__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! leaflet */ \"webpack/sharing/consume/default/leaflet/leaflet\");\n/* harmony import */ var leaflet__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(leaflet__WEBPACK_IMPORTED_MODULE_4__);\n/* harmony import */ var jquery__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! jquery */ \"./node_modules/jquery/dist/jquery.js\");\n/* harmony import */ var jquery__WEBPACK_IMPORTED_MODULE_5___default = /*#__PURE__*/__webpack_require__.n(jquery__WEBPACK_IMPORTED_MODULE_5__);\n\n\n\n\n\n\n/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (backbone__WEBPACK_IMPORTED_MODULE_0___default().View.extend({\n  initialize: function initialize() {\n    (leaflet__WEBPACK_IMPORTED_MODULE_4___default().Icon.Default.imagePath) = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/'; // fix for leaflet draw marker bug\n\n    this.render();\n  },\n  createMap: function createMap() {\n    this.overlay = leaflet__WEBPACK_IMPORTED_MODULE_4___default().featureGroup();\n    var studyArea = JSON.parse(this.getStudyArea()[0]);\n    var polygon = leaflet__WEBPACK_IMPORTED_MODULE_4___default().geoJson(studyArea);\n    var center = polygon.getBounds().getCenter();\n    var map = new (leaflet__WEBPACK_IMPORTED_MODULE_4___default().Map)(jquery__WEBPACK_IMPORTED_MODULE_5___default()('#studyarea-map')[0], {\n      center: center,\n      zoom: 4\n    });\n    var baseMaps = {\n      Map: leaflet__WEBPACK_IMPORTED_MODULE_4___default().tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n        maxZoom: 18,\n        attribution: '&copy; <a href=\"https://openstreetmap.org/copyright\">OpenStreetMap contributors</a>'\n      }).addTo(map),\n      Satellite: leaflet__WEBPACK_IMPORTED_MODULE_4___default().tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {\n        attribution: 'google'\n      })\n    };\n    leaflet__WEBPACK_IMPORTED_MODULE_4___default().control.layers(baseMaps, {}, {\n      position: 'topright',\n      collapsed: false\n    }).addTo(map);\n    polygon.addTo(map);\n  },\n  getStudyArea: function getStudyArea() {\n    var studyArea = this.$('[dataType=\"geoJson\"]');\n    return underscore__WEBPACK_IMPORTED_MODULE_1__[\"default\"].map(studyArea, function (el) {\n      return jquery__WEBPACK_IMPORTED_MODULE_5___default()(el).attr('content');\n    });\n  },\n  render: function render() {\n    this.createMap();\n    return this;\n  }\n}));\n\n//# sourceURL=webpack://webpack/./study-area/src/View/StudyAreaView.js?");

/***/ }),

/***/ "./study-area/src/View/index.js":
/*!**************************************!*\
  !*** ./study-area/src/View/index.js ***!
  \**************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

eval("__webpack_require__.r(__webpack_exports__);\n/* harmony export */ __webpack_require__.d(__webpack_exports__, {\n/* harmony export */   \"StudyAreaView\": () => (/* reexport safe */ _StudyAreaView__WEBPACK_IMPORTED_MODULE_0__[\"default\"])\n/* harmony export */ });\n/* harmony import */ var _StudyAreaView__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./StudyAreaView */ \"./study-area/src/View/StudyAreaView.js\");\n\n\n//# sourceURL=webpack://webpack/./study-area/src/View/index.js?");

/***/ }),

/***/ "./study-area/src/bootstrap.js":
/*!*************************************!*\
  !*** ./study-area/src/bootstrap.js ***!
  \*************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var jquery__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! jquery */ \"./node_modules/jquery/dist/jquery.js\");\n/* harmony import */ var jquery__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(jquery__WEBPACK_IMPORTED_MODULE_0__);\n/* harmony import */ var _View_index__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./View/index */ \"./study-area/src/View/index.js\");\n\n\nvar view = new _View_index__WEBPACK_IMPORTED_MODULE_1__.StudyAreaView({\n  el: '#studyarea-map'\n});\n\n//# sourceURL=webpack://webpack/./study-area/src/bootstrap.js?");

/***/ })

}]);