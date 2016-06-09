DOCKER  := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
MAVEN   := $(DOCKER) -e "MAVEN_OPTS=-Dmaven.repo.local=/cache/mvn" maven:3.2-jdk-8 mvn
COMPOSE := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.7.1
NPM     := $(COMPOSE) run node npm

.PHONY: clean web java build test-data develop selenium

clean:
	$(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml down

web:
	$(NPM) install
	$(NPM) run bower
	$(NPM) run build

java:
	$(MAVEN) -f java/pom.xml package

build:
	$(COMPOSE) build

test-data:
	sh shell/test-data.sh

develop:
	$(COMPOSE) up

selenium:
	$(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml run ruby_test
