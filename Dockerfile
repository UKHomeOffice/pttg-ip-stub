FROM quay.io/ukhomeofficedigital/openjdk8:v1.0.0


ENV USER pttg
ENV USER_ID 1000
ENV GROUP pttg
ENV NAME pttg-ip-stub
ENV JAR_PATH build/libs

ARG VERSION

WORKDIR /app

RUN groupadd -r ${GROUP} && \
    useradd -r -u ${USER_ID} -g ${GROUP} ${USER}  -d /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY ${JAR_PATH}/${NAME}*.jar /app
COPY run.sh /app

RUN chmod a+x /app/run.sh

USER ${USER_ID}

ENTRYPOINT /app/run.sh
