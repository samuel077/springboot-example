.PHONY: start stop build clean test functional-test

start:
	@set -a; source .env; set +a; \
	./gradlew bootRun --args='--spring.profiles.active=local'

stop:
	@echo "Stopping app..."
	@lsof -ti tcp:8080 | xargs kill -9 || echo "No process found on port 8080"

build:
	./gradlew build

clean:
	./gradlew clean

test:
	./gradlew test

functional-test:
	./gradlew functionalTest

dev-up:
	docker-compose -f docker-compose-local.yml up
