#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags
 */

def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'Github'
    def gitUserName = config.gitUserName ?: 'rk4027-N'
    def gitUserEmail = config.gitUserEmail ?: 'gudalarajkumar4444@gmail.com'

    // AWS ECR configuration
    def awsRegion = config.awsRegion ?: "us-east-1"
    def awsAccountId = config.awsAccountId ?: "984912522187"

    def appImage = "${awsAccountId}.dkr.ecr.${awsRegion}.amazonaws.com/easyshop-app"
    def migrationImage = "${awsAccountId}.dkr.ecr.${awsRegion}.amazonaws.com/easyshop-migration"

    echo "Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {

        // Configure Git
        sh """
        git config user.name "${gitUserName}"
        git config user.email "${gitUserEmail}"
        """

        // Update manifests
        sh """
        echo "Updating Kubernetes manifests..."

        # Update main deployment
        sed -i "s|image: .*easyshop-app:.*|image: ${appImage}:${imageTag}|g" ${manifestsPath}/08-easyshop-deployment.yaml

        # Update migration job if present
        if [ -f "${manifestsPath}/12-migration-job.yaml" ]; then
            sed -i "s|image: .*easyshop-migration:.*|image: ${migrationImage}:${imageTag}|g" ${manifestsPath}/12-migration-job.yaml
        fi

        # Update ingress host if present
        if [ -f "${manifestsPath}/10-ingress.yaml" ]; then
            sed -i "s|host: .*|host: easyshop.letsdeployit.com|g" ${manifestsPath}/10-ingress.yaml
        fi

        echo "Checking for changes..."

        if git diff --quiet; then
            echo "No changes to commit"
        else
            git add ${manifestsPath}/*.yaml
            git commit -m "Update image tag to ${imageTag} [ci skip]"

            git remote set-url origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/rk4027-N/tws-e-commerce-app_hackathon.git

            git push origin HEAD:master
        fi
        """
    }
}
