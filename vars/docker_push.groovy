#!/usr/bin/env groovy

def call(Map config = [:]) {

    def imageName = config.imageName ?: error("Image name is required")
    def imageTag = config.imageTag ?: 'latest'

    echo "Pushing Docker image: ${imageName}:${imageTag}"

    sh """
        docker push ${imageName}:${imageTag}
    """
}
