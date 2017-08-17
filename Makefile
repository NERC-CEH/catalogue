DOCKER   := docker run --rm -v $(CURDIR):$(CURDIR) -v $(CURDIR)/cache:/cache -w $(CURDIR)
COMPOSE  := $(DOCKER) -v /var/run/docker.sock:/var/run/docker.sock docker/compose:1.12.0
SELENIUM := $(COMPOSE) -f docker-compose.yml -f docker-compose.selenium.yml

.PHONY: selenium

selenium:
	$(SELENIUM) up --force-recreate -d firefox chrome
	$(SELENIUM) run ruby_test
