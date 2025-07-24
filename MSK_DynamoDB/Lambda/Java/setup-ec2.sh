#!/bin/bash

# Download the setup script from S3
aws s3 cp s3://msk-dynamodb-scripts-bucket-ohio-5040/lambda/java/setup-msk-environment.sh /home/ec2-user/setup-script.sh

# Make the script executable
chmod +x /home/ec2-user/setup-script.sh

# Replace placeholders with actual parameter values
sed -i 's/STACK_NAME_PLACEHOLDER/msk-lambda-java-dynamodb-stack/g' /home/ec2-user/setup-script.sh
sed -i 's/KAFKA_TOPIC_NAME_PLACEHOLDER/MSKLambdaJavaDynamoDBTopic/g' /home/ec2-user/setup-script.sh
sed -i 's|GITHUB_REPO_URL_PLACEHOLDER|https://github.com/indranil-banerjee-aws/QDeveloperProjects.git|g' /home/ec2-user/setup-script.sh
sed -i 's/AWS_REGION_PLACEHOLDER/us-east-2/g' /home/ec2-user/setup-script.sh
sed -i 's|GITHUB_PROJECT_PATH_PLACEHOLDER|MSK_DynamoDB/Lambda/Java|g' /home/ec2-user/setup-script.sh

# Run the setup script
/home/ec2-user/setup-script.sh
