DOCKER   := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
COMPOSE  := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.12.0
SELENIUM := $(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml
GRADLE   := $(CURDIR)/gradlew --build-cache

.PHONY: build clean web java docker test-data develop selenium

all: clean test-data build develop

build: web java docker

clean:
	$(SELENIUM) down

web:
	$(GRADLE) :web:grunt_build

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
