#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Run the application with loaded environment variables
java -jar target/sshop-0.0.1-SNAPSHOT.jar