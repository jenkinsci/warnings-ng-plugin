#
# Container for building gcc/make build processes
#

FROM ubuntu:18.10

USER root
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        apt-utils \
        openssh-server \
        build-essential \
        software-properties-common \
        openjdk-8-jre-headless \
        openjdk-8-jdk-headless

RUN mkdir -p /var/run/sshd && \
    mkdir /home/test && \
    useradd test && \
    chown -R test:test /home/test && \
    echo "test:test" | chpasswd


ENTRYPOINT ["/usr/sbin/sshd", "-D", "-e"]
