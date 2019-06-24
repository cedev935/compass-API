Compass Backend API
==================

Compass is a peer-to-peer loan platform that brings ease of use and improvements
to an industry that has been largely untouched by automation.

This is the backend REST API application built in Java 8 EE for rollout on the
Google Cloud platform. It uses MySQL for data persistance and Storage on GCP
for large file storage

See the [Google App Engine standard environment documentation][ae-docs] for
detailed configuration instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/


* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)

## Setup

    gcloud init
    gcloud auth application-default login

## Maven
### Running locally

    mvn appengine:run

### Deploying

    mvn appengine:deploy
