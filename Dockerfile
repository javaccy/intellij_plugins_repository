FROM ubuntu:16.04
MAINTAINER javaccy <javaccy@gmail.com>
# now add java and tomcat support in the container
#ADD jdk-8u121-linux-x64.tar.gz /usr/local/
# configuration of java and tomcat ENV
#ENV PATH $PATH:$JAVA_HOME/bin:$CATALINA_HOME/lib:$CATALINA_HOME/bin
# container listener port
RUN apt-get update -y && apt-get install localepurge -y && apt-get install language-pack-zh-hans -y && apt-get install openjdk-8-jdk -y && apt-get install unzip -y && apt-get install wget -y && wget https://github.com/javaccy/intellij_plugins_repository/archive/master.zip && unzip master.zip && mv intellij_plugins_repository-master repo
RUN locale-gen zh_CN.UTF-8 en_US.UTF-8
ENV LANG="en_US.UTF-8"
ENV LC_ALL="en_US.UTF-8"
ENV LANGUAGE="en_US.UTF-8"
EXPOSE 6868
# startup web application services by self
#CMD /usr/local/apache-tomcat-8.5.16/bin/catalina.sh run
CMD cd /repo && ./gradlew run

#构建
#sudo docker build -t javaccy/intellij_repository .
#启动
#sudo docker run -i --name intellij_repo --restart always -p 6868:6868 -v /home/yl/Desktop/intellij_repository:/repo/upload javaccy/intellij_repository