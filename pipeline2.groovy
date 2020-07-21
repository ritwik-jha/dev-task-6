job('devt6-github') {

    description('this job downloads github repo to local directory')

    scm {
            github('https://github.com/ritwik-jha/devops_task_3.git','master')
        }

    triggers {
                githubPush()
            }

    steps {
            shell('sudo cp -v code.php /root/dev_task_6')
            shell('sudo cd /root/dockerws')
            shell('sudo rm -rf *')
            shell('sudo cp -v Dockerfile /root/dockerws')
        }
    }
        

job('devt6-container-build') {

    description('this job builds a docker image from Dockerfile')

    triggers {
                upstream('devt6-github' , 'SUCCESS')
            }

    steps {
            shell('sudo cd /root/dockerws')
            shell('sudo docker build . -t task6image:v1')
            }
        }
        

job('devt6-container-push') {

    description('this job pushes the new image or new version of image to the docker hub')

    triggers {
                upstream('devt6-container-build' , 'SUCCESS')
            }

    steps {
            shell('sudo docker tag task6image:vi ritwik46/task6image:v1')
            shell('sudo docker push ritwik46/task6image:v1')
            }
        }
        

job('devt6-pvc-launch') {

    description('this job downloads pvc yaml file from github and launches a pvc')

    scm {
        github('https://github.com/ritwik-jha/devops_task_3.git','master')
            }

    triggers {
                upstream('devt6-container-push')
                }

    steps {
            shell('sudo cp -fv pvc.yml /root/task6_kube')
            shell('sudo cd /root/task6_kube')
            shell('sudo if sudo kubectl get pvc | grep task3-pvc;then;sudo echo "pvc running";else;sudo kubectl create -f pvc.yml')
            }

    }
        

job('devt6-deployment-launch') {

                description('this job will download deployment yml file from github and launch a deployment')

                scm {
                    github('https://github.com/ritwik-jha/devops_task_3.git','master')
                }

                triggers {
                    upstream('devt6-pvc-launch')
                }

                steps {
                    shell('sudo cp -v deployment.yml /root/task6_kube')
                    shell('sudo cd /root/task6_kube')
                    shell('sudo if sudo kubectl get deploy | grep task3-web-deploy;then;sudo kubectl replace -f deployment.yml;else; sudo kubectl create -f deployment.yml')
                }
            }
        

job('devt6-service-launch') {

                description('this job will download service yml file from github and launch a service which will expose the deployment')

                scm {
                    github('https://github.com/ritwik-jha/devops_task_3.git','master')
                }

                triggers {
                    upstream('devt6-deployment-launch')
                }

                steps {
                    shell('sudo cp -v service.yml /root/task6_kube')
                    shell('sudo cd /root/task6_kube')
                    shell('sudo if sudo kubectl get svc | grep task3-service;then;sudo echo "service running";else;sudo kubectl create -f service.yml')
                }
            }
        

job('devt6-site-check') {

                description('this job will act as a user and will check the connectivity to the site and sends an e-mail to developer if any error comes up')

                triggers {
                    upstream('devt6-service-launch')
                }

                steps {
                    shell('sudo x=$(curl -o /dev/null -sw "%{http_code}" 192.168.99.100:30001/code.php)')
                    shell('sudo if [echo $x -eq 200];then;sudo echo "site running properly";else;sudo echo "site not running properly and sending e-mail";sudo /root/task_2/email.py')            
                }
            }
        
    
