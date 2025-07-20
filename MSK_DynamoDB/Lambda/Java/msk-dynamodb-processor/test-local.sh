#!/bin/bash

# Build the application
echo "Building the application..."
sam build

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo "Build successful!"

# Start DynamoDB Local in a separate terminal if needed
# In a separate terminal, you can run:
# docker run -p 8000:8000 amazon/dynamodb-local

# Invoke the function locally with the sample event
echo "Invoking the function locally..."
sam local invoke MSKProcessorFunction \
    --event events/msk-event.json \
    --env-vars env.json

if [ $? -ne 0 ]; then
    echo "Invocation failed. Please check the errors above."
    exit 1
fi

echo "Local testing completed successfully!"
