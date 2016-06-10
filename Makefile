DOCKER  := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
MAVEN   := $(DOCKER) -e "MAVEN_OPTS=-Dmaven.repo.local=/cache/mvn" maven:3.2-jdk-8 mvn
COMPOSE := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.7.1

clean:
	$(COMPOSE) down

build:
	$(MAVEN) -f java/pom.xml clean package
	$(COMPOSE) build

build-maven:
	$(MAVEN) -f java/pom.xml clean package

build-web:
	$(MAVEN) -f java/pom.xml clean package
	$(COMPOSE) build web

test-data:
	sh shell/test-data.sh

bower:
	$(COMPOSE) run grunt npm run bower

develop:
	$(COMPOSE) up

develop-min:
	$(COMPOSE) up web solr

selenium:
	$(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml run ruby_test
