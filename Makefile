DOCKER   := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
MAVEN    := $(DOCKER) -e "MAVEN_OPTS=-Dmaven.repo.local=/cache/mvn" maven:3.2-jdk-8 mvn
COMPOSE  := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.12.0
SELENIUM := $(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml
NPM      := $(COMPOSE) run node npm

.PHONY: build clean web java java-build maven-test maven-version web-test docker test-data develop develop-min selenium selenium-only

all: clean test-data build develop

build: web java docker

clean:
	$(SELENIUM) down

web:
	$(NPM) install
	$(NPM) run bower
	$(NPM) run build

npm-build:
	$(NPM) run build

java:
	$(MAVEN) -f java/pom.xml clean package

java-build: java docker

maven-test:
	# Run tests for a single test or test class
	$(MAVEN) -f java/pom.xml -Dtest=$(TESTCLASS) clean test-compile surefire:test
	# TESTCLASS=uk.ac.ceh.gateway.catalogue.indexing.SolrIndexLinkDocumentGeneratorTest#testGenerateIndex make maven-test

maven-version:
	# Are there updates for java packages?
	$(MAVEN) -f java/pom.xml versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates

web-test:
	# Just run the javascript tests
	$(NPM) run test

dropbox:
	mkdir -p dropbox

docker: dropbox
	$(COMPOSE) build

test-data:
	sh shell/test-data.sh

develop:
	$(COMPOSE) up

develop-min:
	# Don't start Node
	$(COMPOSE) up web solr

selenium: test-data
	$(SELENIUM) up --force-recreate -d firefox chrome
	$(SELENIUM) run ruby_test

selenium-only:
	$(SELENIUM) up --force-recreate -d firefox chrome
	$(SELENIUM) run ruby_test
