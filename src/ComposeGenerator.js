(function(app) {
  const SERVICE_TEMPLATES = {
    postgres: {
      image: 'postgres:13',
      environment: {
        POSTGRES_DB: 'mydatabase',
        POSTGRES_USER: 'user',
        POSTGRES_PASSWORD: 'password'
      },
      ports: ['5432:5432']
    },
    redis: {
      image: 'redis:6',
      ports: ['6379:6379']
    },
    kafka: {
      image: 'bitnami/kafka:latest',
      ports: ['9092:9092'],
      environment: {
          KAFKA_BROKER_ID: 1,
          KAFKA_LISTENERS: 'PLAINTEXT://:9092',
          KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:9092',
          KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181',
          ALLOW_PLAINTEXT_LISTENER: 'yes'
      },
      depends_on: ['zookeeper']
    },
    rabbitmq: {
      image: 'rabbitmq:3-management',
      ports: ['5672:5672', '15672:15672']
    },
    zookeeper: {
        image: 'bitnami/zookeeper:latest',
        ports: ['2181:2181'],
        environment: {
            ALLOW_ANONYMOUS_LOGIN: 'yes'
        }
    }
  };

  const ComposeGenerator = {
    generate: function(serviceName, dependencies) {
      let compose = {
        version: '3.8',
        services: {}
      };

      compose.services[serviceName] = {
        image: `com.example/${serviceName}:latest`,
        ports: ['8080:8080'],
        environment: [],
        depends_on: []
      };

      let allDependencies = [...dependencies];

      if (dependencies.some(dep => dep.toService === 'kafka')) {
          if (!dependencies.some(dep => dep.toService === 'zookeeper')) {
              allDependencies.push({ fromService: serviceName, toService: 'zookeeper' });
          }
      }

      for (const dep of allDependencies) {
        const serviceNameDep = dep.toService;
        if (SERVICE_TEMPLATES[serviceNameDep]) {
          compose.services[serviceNameDep] = SERVICE_TEMPLATES[serviceNameDep];
          if (serviceNameDep !== 'zookeeper' || dependencies.some(d => d.toService === 'zookeeper')) {
              if (!compose.services[serviceName].depends_on.includes(serviceNameDep)) {
                  compose.services[serviceName].depends_on.push(serviceNameDep);
              }
          }
        }
      }

      if (compose.services[serviceName].depends_on.length === 0) {
          delete compose.services[serviceName].depends_on;
      }

      return jsyaml.dump(compose);
    }
  };

  app.ComposeGenerator = ComposeGenerator;
})(window.SpringServiceMonitor);
