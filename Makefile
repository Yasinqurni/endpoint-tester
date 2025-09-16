# Endpoint Tester Makefile
# ========================

# Variables
PROJECT_NAME = endpoint-tester
JAR_FILE = build/libs/$(PROJECT_NAME)-all.jar
PORT = 8080
LOG_DIR = build/logs

# Colors for output
RED = \033[0;31m
GREEN = \033[0;32m
YELLOW = \033[1;33m
BLUE = \033[0;34m
NC = \033[0m # No Color

# Default target
.DEFAULT_GOAL := help

# Help target
.PHONY: help
help: ## Show this help message
	@echo "$(BLUE)Endpoint Tester - Available Commands$(NC)"
	@echo "=================================="
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Development Commands
.PHONY: run
run: ## Run the application in development mode
	@echo "$(BLUE)Starting $(PROJECT_NAME) in development mode...$(NC)"
	./gradlew run

.PHONY: run-bg
run-bg: ## Run the application in background
	@echo "$(BLUE)Starting $(PROJECT_NAME) in background...$(NC)"
	./gradlew run &
	@echo "$(GREEN)Application started in background. PID: $$!$(NC)"

.PHONY: stop
stop: ## Stop the running application
	@echo "$(YELLOW)Stopping $(PROJECT_NAME)...$(NC)"
	@pkill -f "ktor-sample" || echo "$(YELLOW)No running instance found$(NC)"
	@echo "$(GREEN)Application stopped$(NC)"

# Build Commands
.PHONY: build
build: ## Build the application
	@echo "$(BLUE)Building $(PROJECT_NAME)...$(NC)"
	./gradlew build
	@echo "$(GREEN)Build completed$(NC)"

.PHONY: build-fat-jar
build-fat-jar: ## Build fat JAR with all dependencies
	@echo "$(BLUE)Building fat JAR...$(NC)"
	./gradlew shadowJar
	@echo "$(GREEN)Fat JAR created: $(JAR_FILE)$(NC)"

.PHONY: clean
clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	./gradlew clean
	@echo "$(GREEN)Clean completed$(NC)"

# Testing Commands
.PHONY: test
test: ## Run tests
	@echo "$(BLUE)Running tests...$(NC)"
	./gradlew test
	@echo "$(GREEN)Tests completed$(NC)"

.PHONY: test-coverage
test-coverage: ## Run tests with coverage report
	@echo "$(BLUE)Running tests with coverage...$(NC)"
	./gradlew test jacocoTestReport
	@echo "$(GREEN)Coverage report generated in build/reports/jacoco/test/html/index.html$(NC)"

# Code Quality Commands
.PHONY: lint
lint: ## Run code linting
	@echo "$(BLUE)Running code linting...$(NC)"
	./gradlew ktlintCheck
	@echo "$(GREEN)Linting completed$(NC)"

.PHONY: lint-fix
lint-fix: ## Fix linting issues automatically
	@echo "$(BLUE)Fixing linting issues...$(NC)"
	./gradlew ktlintFormat
	@echo "$(GREEN)Linting issues fixed$(NC)"

.PHONY: check
check: ## Run all checks (lint, test, build)
	@echo "$(BLUE)Running all checks...$(NC)"
	./gradlew check
	@echo "$(GREEN)All checks completed$(NC)"

# Docker Commands
.PHONY: docker-build
docker-build: ## Build Docker image
	@echo "$(BLUE)Building Docker image...$(NC)"
	docker build -t $(PROJECT_NAME):latest .
	@echo "$(GREEN)Docker image built: $(PROJECT_NAME):latest$(NC)"

.PHONY: docker-run
docker-run: ## Run application in Docker container
	@echo "$(BLUE)Running $(PROJECT_NAME) in Docker...$(NC)"
	docker run -p $(PORT):$(PORT) $(PROJECT_NAME):latest

.PHONY: docker-stop
docker-stop: ## Stop Docker container
	@echo "$(YELLOW)Stopping Docker container...$(NC)"
	docker stop $$(docker ps -q --filter ancestor=$(PROJECT_NAME):latest) || echo "$(YELLOW)No running container found$(NC)"

# Monitoring Commands
.PHONY: logs
logs: ## Show application logs
	@echo "$(BLUE)Showing application logs...$(NC)"
	@if [ -f "$(LOG_DIR)/endpoint-tester.log" ]; then \
		tail -f $(LOG_DIR)/endpoint-tester.log; \
	else \
		echo "$(YELLOW)No log file found at $(LOG_DIR)/endpoint-tester.log$(NC)"; \
	fi

.PHONY: logs-tail
logs-tail: ## Tail application logs (last 50 lines)
	@echo "$(BLUE)Tailing application logs...$(NC)"
	@if [ -f "$(LOG_DIR)/endpoint-tester.log" ]; then \
		tail -50 $(LOG_DIR)/endpoint-tester.log; \
	else \
		echo "$(YELLOW)No log file found at $(LOG_DIR)/endpoint-tester.log$(NC)"; \
	fi

.PHONY: status
status: ## Check application status
	@echo "$(BLUE)Checking application status...$(NC)"
	@if pgrep -f "ktor-sample" > /dev/null; then \
		echo "$(GREEN)Application is running$(NC)"; \
		echo "PID: $$(pgrep -f "ktor-sample")"; \
		echo "Port: $(PORT)"; \
	else \
		echo "$(RED)Application is not running$(NC)"; \
	fi

# API Testing Commands
.PHONY: test-api
test-api: ## Test the API endpoint
	@echo "$(BLUE)Testing API endpoint...$(NC)"
	@curl -X POST http://localhost:$(PORT)/endpoint-tester \
		-H "Content-Type: application/json" \
		-d '{"data":[{"endpoint":"https://httpbin.org/get","method":"GET"}]}' \
		| jq . || echo "$(YELLOW)API test completed (jq not available for pretty printing)$(NC)"

.PHONY: test-api-post
test-api-post: ## Test POST endpoint
	@echo "$(BLUE)Testing POST endpoint...$(NC)"
	@curl -X POST http://localhost:$(PORT)/endpoint-tester \
		-H "Content-Type: application/json" \
		-d '{"data":[{"endpoint":"https://httpbin.org/post","method":"POST","body":"{\"test\":\"data\"}","headers":{"Content-Type":"application/json"}}]}' \
		| jq . || echo "$(YELLOW)POST test completed$(NC)"

.PHONY: test-api-error
test-api-error: ## Test error handling
	@echo "$(BLUE)Testing error handling...$(NC)"
	@curl -X POST http://localhost:$(PORT)/endpoint-tester \
		-H "Content-Type: application/json" \
		-d '{"data":[{"endpoint":"https://httpbin.org/status/500","method":"GET"}]}' \
		| jq . || echo "$(YELLOW)Error test completed$(NC)"

# Development Setup
.PHONY: setup
setup: ## Setup development environment
	@echo "$(BLUE)Setting up development environment...$(NC)"
	@if [ ! -f "gradlew" ]; then \
		echo "$(YELLOW)Gradle wrapper not found. Please run 'gradle wrapper' first$(NC)"; \
		exit 1; \
	fi
	@chmod +x gradlew
	@mkdir -p $(LOG_DIR)
	@echo "$(GREEN)Development environment setup completed$(NC)"

.PHONY: install-deps
install-deps: ## Install dependencies
	@echo "$(BLUE)Installing dependencies...$(NC)"
	./gradlew build --refresh-dependencies
	@echo "$(GREEN)Dependencies installed$(NC)"

# Utility Commands
.PHONY: format
format: ## Format code
	@echo "$(BLUE)Formatting code...$(NC)"
	./gradlew ktlintFormat
	@echo "$(GREEN)Code formatted$(NC)"

.PHONY: dependencies
dependencies: ## Show project dependencies
	@echo "$(BLUE)Project dependencies:$(NC)"
	./gradlew dependencies

.PHONY: versions
versions: ## Show dependency versions
	@echo "$(BLUE)Dependency versions:$(NC)"
	./gradlew dependencyInsight --configuration compileClasspath

# Cleanup Commands
.PHONY: clean-all
clean-all: clean ## Clean everything including logs
	@echo "$(BLUE)Cleaning everything...$(NC)"
	rm -rf $(LOG_DIR)
	@echo "$(GREEN)Everything cleaned$(NC)"

.PHONY: reset
reset: clean-all setup ## Reset project to clean state
	@echo "$(GREEN)Project reset completed$(NC)"

# Quick Commands
.PHONY: dev
dev: run ## Alias for run

.PHONY: start
start: run-bg ## Alias for run-bg

.PHONY: restart
restart: stop run-bg ## Restart application

# Show project info
.PHONY: info
info: ## Show project information
	@echo "$(BLUE)Project Information$(NC)"
	@echo "==================="
	@echo "Name: $(PROJECT_NAME)"
	@echo "Port: $(PORT)"
	@echo "Log Directory: $(LOG_DIR)"
	@echo "JAR File: $(JAR_FILE)"
	@echo ""
	@echo "$(BLUE)Available Commands:$(NC)"
	@echo "• make run        - Start development server"
	@echo "• make test       - Run tests"
	@echo "• make build      - Build application"
	@echo "• make logs       - View logs"
	@echo "• make test-api   - Test API endpoints"
	@echo "• make help       - Show all commands"
