# Python Service Moderation

This project is a Python service that performs moderation of images and videos using a machine learning model. It utilizes Kafka for message processing and supports both image and video moderation.

## Project Structure

```
python-service
├── main.py          # Main logic for the moderation service
├── Dockerfile       # Dockerfile to build the Docker image
├── requirements.txt # Python packages required for the project
├── README.md        # Documentation for the project
└── .env             # Contains information about environment variables
```

## Requirements

Before building the Docker image, ensure you have the following installed:

- Docker

## Building the Docker Image

To build the Docker image, navigate to the project directory and run the following command:

```
docker build -t python-service .
```

## Running the Docker Container

After building the image, you can run the container using:

```
docker run -p 8080:8080 python-service
```

This will start the service and expose it on port 8080.

## Usage

Once the service is running, you can send moderation requests to it via Kafka. The service listens for messages on the specified Kafka topic and processes images and videos accordingly.

## License

This project is licensed under the MIT License.