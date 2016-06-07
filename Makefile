build:
	mvn -f java/pom.xml package
	docker-compose build

test-data:
	sh shell/test-data.sh

bower:
	docker-compose run grunt npm run bower

develop:
	docker-compose up

selenium:
	docker-compose -f docker-compose.yml -f docker-compose.selenium.yml run ruby_test 