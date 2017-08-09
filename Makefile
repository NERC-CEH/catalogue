DOCKER   := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
COMPOSE  := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.12.0
SELENIUM := $(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml
NPM      := $(COMPOSE) run node npm
GRADLE	 := $(CURDIR)/java/gradlew --project-dir $(CURDIR)/java --build-cache

.PHONY: build clean web java docker test-data develop selenium

all: clean test-data build develop

build: web java docker

clean:
	$(SELENIUM) down

web:
	$(NPM) install
	$(NPM) run bower
	$(NPM) run build

java:
	$(GRADLE) clean build

docker:
	$(COMPOSE) build

test-data:
	sh shell/test-data.sh

develop:
	$(COMPOSE) up

selenium: test-data
	$(SELENIUM) up --force-recreate -d firefox chrome
	$(SELENIUM) run ruby_test
