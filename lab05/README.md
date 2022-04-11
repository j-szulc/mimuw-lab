# Application monitoring with graphite and grafana

## Task 1

Write a simple application that sometimes (once every 30 calls) processes the request slower.
Monitor number of requests and processing time.
Create dashboard in grafana that will present: 
- mean, 90-percentile and max of processing time
- number of requests

## Run grafana with graphite in docker

### Run graphite

```bash
docker pull  graphiteapp/graphite-statsd

docker run -d \
 --name graphite \
 --restart=always \
 -p 80:80\
 -p 2003-2004:2003-2004\
 -p 2023-2024:2023-2024\
 -p 8125:8125/udp\
 -p 8126:8126\
 graphiteapp/graphite-statsd
 
```

### Run grafana

```bash
docker pull grafana/grafana-oss

docker run --rm -d -p 3000:3000 --link graphite:graphite --name grafana grafana/grafana-oss
```

(add sudo if user is not in docker group)

### Run app

`
 uvicorn main:app
`

Log into grafana:
http://127.0.0.1:3000/

Add datasource with type: graphite and ip address: http://localhost

### Check

Send simple single metric:

```bash
echo -n "test:20|c" | nc -w 1 -u 127.0.0.1 8125;
```

verify on graphite web:
http://localhost/render?from=-10mins&until=now&target=stats.test

verify on grafana:
http://localhost:3000/explore?orgId=1&left=%7B%22datasource%22:%22Graphite%22,%22queries%22:%5B%7B%22refId%22:%22A%22,%22target%22:%22stats.test%22%7D%5D,%22range%22:%7B%22from%22:%22now-1h%22,%22to%22:%22now%22%7D%7D

## Task 2

- Add /health endpoint. 
- Create LB (similar to lab03) which checks created endpoint
- Verify if /health returns unhealthy state - LB will not forward traffic
- Add HEALTHCHECK in Dockerfile

Check:
- if in one node /health return != 200 than no traffic goes to this node from LB
- verify if service is unhealty docker status is unhealthy as well (checked by docker ps)

