module.exports = (grunt) ->
  #Load grunt tasks
  grunt.loadNpmTasks 'grunt-concurrent'
  grunt.loadNpmTasks 'grunt-combine-harvester'
  grunt.loadNpmTasks 'grunt-contrib-requirejs'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-coffee'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-copy'
  grunt.loadNpmTasks 'grunt-contrib-less'
  grunt.loadNpmTasks 'grunt-contrib-cssmin'
  grunt.loadNpmTasks 'grunt-contrib-watch'

  #Configure tasks
  grunt.initConfig
    bowerDirectory: require('bower').config.directory

    jasmine :
      test:
        options :
          specs : 'test-compiled/**/*spec.js'
          template: require 'grunt-template-jasmine-requirejs'
          templateOptions:
            requireConfigFile: 'src/scripts/main.js'
            requireConfig: baseUrl: 'src/scripts'

    coffee:
      test :
        expand: true
        cwd: 'test'
        src: ['**/*.coffee']
        dest: 'test-compiled'
        ext: '.spec.js'

    less:
      options:
        compress: false
        paths: ['less', '<%= bowerDirectory %>/bootstrap/less']
      default:
        files: 'src/css/style-ceh.css' : 'src/less/style-ceh.less'
      cmp:
        files: 'src/css/style-cmp.css' : 'src/less/style-cmp.less'
      eidc:
        files: 'src/css/style-eidc.css' : 'src/less/style-eidc.less'

    requirejs:
      compile:
        options:
          exclude:['coffee-script']
          baseUrl: 'src/scripts'
          out: 'src/scripts/main-out.js'
          name: 'main'
          mainConfigFile: 'src/scripts/main.js'

    combine_harvester:
      openlayers:
        options:
          root: 'src/vendor/openlayers/lib/'
        files:
          'src/vendor/OpenLayers-custom.js': [
            'src/vendor/openlayers/lib/OpenLayers/Map.js'
            'src/vendor/openlayers/lib/OpenLayers/Rule.js'
            'src/vendor/openlayers/lib/OpenLayers/Marker.js'
            'src/vendor/openlayers/lib/OpenLayers/Kinetic.js'
            'src/vendor/openlayers/lib/OpenLayers/Projection.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/OSM.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/WMS.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/TouchNavigation.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/Navigation.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/Zoom.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/Attribution.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/DrawFeature.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/TransformFeature.js'
            'src/vendor/openlayers/lib/OpenLayers/Handler/RegularPolygon.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/TMS.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/Vector.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/Markers.js'
            'src/vendor/openlayers/lib/OpenLayers/Filter/Function.js'
            'src/vendor/openlayers/lib/OpenLayers/Renderer/SVG.js'
            'src/vendor/openlayers/lib/OpenLayers/Renderer/VML.js'
            'src/vendor/openlayers/lib/OpenLayers/Renderer/Canvas.js'
            'src/vendor/openlayers/lib/OpenLayers/Protocol/HTTP.js'
            'src/vendor/openlayers/lib/OpenLayers/Strategy/Fixed.js'
            'src/vendor/openlayers/lib/OpenLayers/TileManager.js'
            'src/vendor/openlayers/lib/OpenLayers/Format/WKT.js'
            'src/vendor/ol-loadingPanel/index.js'
          ]

    watch:
      less:
        files: "src/less/*"
        tasks: ["less"]

      requirejs:
        files: 'src/scripts/main.js'
        tasks: ["copy:requirejs"]

    concurrent:
      watch:
        tasks: ['watch:less', 'watch:requirejs']
        options:
          logConcurrentOutput: true

    copy:
      requirejs:
        src: 'src/scripts/main.js'
        dest: 'src/scripts/main-out.js'

    clean:
      test:['test-compiled']
      prep: ['src/css']

    cssmin:
      default:
        files: 'src/css/style-ceh.css' : 'src/less/style-ceh.less'
      cmp:
        files: 'src/css/style-cmp.css' : 'src/css/style-cmp.css'
      eidc:
        files: 'src/css/style-eidc.css' : 'src/css/style-eidc.css'

  grunt.registerTask 'prep', ['clean', 'combine_harvester:openlayers']
  grunt.registerTask 'test', ['clean:test', 'coffee', 'jasmine']
  grunt.registerTask 'develop', ['less', 'copy:requirejs', 'concurrent:watch']
  grunt.registerTask 'build', ['clean', 'less', 'cssmin', 'requirejs']
  grunt.registerTask 'default', ['prep', 'build', 'test']
