DOCKER   := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
MAVEN    := $(DOCKER) -e "MAVEN_OPTS=-Dmaven.repo.local=/cache/mvn" maven:3.2-jdk-8 mvn
COMPOSE  := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.7.1
SELENIUM := $(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml
NPM      := $(COMPOSE) run node npm

.PHONY: clean web java build docker test-data develop selenium

build: web java docker

clean:
	$(SELENIUM) down

web:
	$(NPM) install
	$(NPM) run bower
	$(NPM) run build

java:
	$(MAVEN) -f java/pom.xml clean package

java-build: java docker

maven-test:
	$(MAVEN) -f java/pom.xml -Dtest=$(TESTCLASS) clean test-compile surefire:test
	# Run a single test or test class
	# TESTCLASS=uk.ac.ceh.gateway.catalogue.indexing.SolrIndexLinkDocumentGeneratorTest#testGenerateIndex make maven-test

maven-version:
	$(MAVEN) -f java/pom.xml versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates

web-test:
	$(NPM) run test

docker:
	$(COMPOSE) build

test-data:
	sh shell/test-data.sh

develop:
	$(COMPOSE) up

develop-min:
	$(COMPOSE) up web solr

selenium: test-data
	$(SELENIUM) up --force-recreate -d firefox chrome
	$(SELENIUM) run ruby_test
