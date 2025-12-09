docker network create backend-net
cd ms-docker
docker-compose -f docker-compose-rabbit.yml up -d
docker-compose -f docker-compose-zipin.yml up -d
docker-compose -f docker-compose-graylog.yml up -d 
docker-compose -f docker-compose-perf.yml up -d 
cd ..
cd dan-eureka-srv
docker-compose up -d 
cd ..
cd ms-clientes
docker-compose up -d 
cd ..
cd ms-productos
docker-compose up -d 
cd ..
cd ms-pedidos
docker-compose up -d 
cd ..
cd dan-gateway
docker-compose up -d