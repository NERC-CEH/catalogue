# CEH Information Gateway - Vagrant Box

This is the CEH development box, it will create an instance of the CEH Catalogue along with it's supporting services (e.g. solr)

## Structure

### catalogue

  This directory contains the application proper, this consists of things like the *solr configuration*, 
  *html/xml templates*, *java server code* and the *web application code*.

  This directory is mapped to */opt/ceh-catalogue* of your vagrant machine. This enabled live changes of
  the *templates* and *web application code*.

### catalogue/java

  Here you will find a maven managed java application, by default the built version of this code is deployed 
  to the vagrant machine at **puppet provision time**. Running the application with the following java system property:

          # You can customise this value in puppet/hieradata/secrets.yaml
          -Dspring.profiles.active=development

  runs the application in development mode. This disables communication with external servers (such as crowd) and 
  initializes light weight development versions instead (such as an in memory user base)

### catalogue/web

  This is the web application side of the project, compiled versions of the code will be committed back to the 
  repository (so will be present the first time you start). More specific details can be found in the directory specific
  README file

### catalogue/templates

  Here you will find the custom templates for rendering any of the configured document schemas in their custom 
  representations. Customisations to this content in this directory will be reflected as live changes on your
  vagrant machine.

## Getting started

To get the project running. Install :

* vagrant
* vagrant_vmware plugin

Then in a bash window run

    vagrant up

### Java development

The vagrant box will run and deploy built versions of the java catalogue code from *catalogue/java/target/ROOT.war*. This happens during a vagrant provision. You can build the application with maven at the command line:

    mvn -f catalogue/java/pom.xml clean install
    vagrant provision --provision-with puppet_server

Or alternatively, you can get the latest built version from [nexus](http://nexus.nerc-lancaster.ac.uk/service/local/artifact/maven/redirect?r=releases&g=uk.ac.ceh.gateway&a=Catalogue&v=LATEST&e=war)

    # At the root of your repository
    mkdir -p catalogue/java/target
    wget 'http://nexus.nerc-lancaster.ac.uk/service/local/artifact/maven/redirect?r=releases&g=uk.ac.ceh.gateway&a=Catalogue&v=LATEST&e=war' -O catalogue/java/target/ROOT.war
    vagrant provision --provision-with puppet_server

### Building in Docker

You can build a docker container which contains the catalogue in. To do this:

    # Build the java application
    mvn -f catalogue/java/pom.xml clean install
    docker build -t "nercceh/catalogue" .

    # Start up the docker container
    docker run -i -p 80:80 -p 7000:7000  -v /opt/datastore:/var/ceh-catalogue/datastore -t nercceh/catalogue 

## Testing

The vagrant box can be functionally tested using capybara and selenium. The tests for this are written in rspec, and drive browsers hosted on the [selenium grid](http://bamboo.ceh.ac.uk:4444/grid/console). To run from a windows box you will need to:

* Make contact with the selenium grid

  You will need to make sure that your machine can be accessed by the Selenium Grid of browsers. By default windows firewall will block this. Add an **Incoming Rule** to the firewall to open port **8080**.

* Install ruby + [bundler](http://bundler.io/)

  If you are using babun, you can install all the prerequisite packages using pact. If you have a traditional ruby windows installation already, make sure you take this off the PATH variable or uninstall it altogether.

        pact install ruby
        pact install libiconv
        pact install patch
        pact install gcc4-g++
        pact install libxml2
        pact install libxml2-devel
        pact install libxslt
        pact install libxslt-devel
        gem install nokogiri -- --use-system-libraries
        gem install bundler

* Install the gem bundle using. Nokogiri will be installed here and may need to build some components. This step may take some time.

        bundle install

* Then you can execute your tests in parallel

        bundle exec rake
  This calls :clean and :parallel_spec

* You can specify a particular test group to test by calling the specific rspec task:

        bundle exec rake --tasks
  Lists the available rake tasks. Not all can be run from workstations

        bundle exec rake chrome_spec

* Sit back and wait for the results of your test suite

### Updating test metadata records
Test metadata records are available in the /fixtures directory. When changes are made to these test documents they need to be loaded into the vagrant datastore.

    sh shell/quick-reload.sh
