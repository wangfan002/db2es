FROM openjdk:8-jdk-alpine
# 把Arthas安装到基础镜像里 ,方便排查问题
COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas
MAINTAINER "Fan.Wang"
LABEL description="DB2ES"
WORKDIR app
ADD ./target/db2es-1.0.jar /app/db2es.jar
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
# 使 java 程序 pid 不为1
RUN apk add --no-cache tini
ENTRYPOINT ["/sbin/tini", "--"]
EXPOSE 8888
CMD java -jar -Xms9G -Xmx9G -Xmn6G /app/db2es.jar