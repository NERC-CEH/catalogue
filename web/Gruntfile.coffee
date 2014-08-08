module.exports = (grunt) ->
  #Load grunt tasks
  grunt.loadNpmTasks 'grunt-exec'
  grunt.loadNpmTasks 'grunt-concurrent'
  grunt.loadNpmTasks 'grunt-combine-harvester'
  grunt.loadNpmTasks 'grunt-contrib-requirejs'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-copy'
  grunt.loadNpmTasks 'grunt-contrib-less'
  grunt.loadNpmTasks 'grunt-contrib-cssmin'
  grunt.loadNpmTasks 'grunt-contrib-watch'

  #Configure tasks
  grunt.initConfig
    exec:
      git_status: 'git diff --exit-code'

    less: 
      development:
        files: 'src/css/style.css' : 'src/less/style.less'

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
            'src/vendor/openlayers/lib/OpenLayers/Kinetic.js'
            'src/vendor/openlayers/lib/OpenLayers/Projection.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/OSM.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/WMS.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/TouchNavigation.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/Navigation.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/Zoom.js'
            'src/vendor/openlayers/lib/OpenLayers/Control/Attribution.js'
            'src/vendor/openlayers/lib/OpenLayers/Layer/TMS.js'
            'src/vendor/openlayers/lib/OpenLayers/Renderer/VML.js'
            'src/vendor/openlayers/lib/OpenLayers/Renderer/Canvas.js'
            'src/vendor/openlayers/lib/OpenLayers/Protocol/HTTP.js'
            'src/vendor/openlayers/lib/OpenLayers/Strategy/Fixed.js'
            'src/vendor/openlayers/lib/OpenLayers/TileManager.js'
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
      prep: ['src/css']

    cssmin:
      build:
        src: 'src/css/style.css'
        dest: 'src/css/style.css'

  grunt.registerTask 'prep', ['clean', 'combine_harvester:openlayers']
  grunt.registerTask 'develop', ['less', 'copy:requirejs', 'concurrent:watch']
  grunt.registerTask 'build', ['clean', 'less', 'cssmin', 'requirejs']
  grunt.registerTask 'default', ['prep', 'build', 'exec:git_status']