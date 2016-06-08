DOCKER  := docker run --rm -v $(CURDIR):$(CURDIR) -w $(CURDIR)
MAVEN   := $(DOCKER) maven:3.2-jdk-8 mvn
COMPOSE := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.7.1

build:
	$(MAVEN) -f java/pom.xml package
	$(COMPOSE) build

test-data:
	sh shell/test-data.sh

bower:
	$(COMPOSE) run grunt npm run bower

develop:
	$(COMPOSE) up

selenium:
	$(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml run ruby_test
