(function(app) {
  const DEPENDENCY_MAP = {
    "spring.kafka.bootstrap-servers": "kafka",
    "spring.redis.host": "redis",
    "spring.rabbitmq.host": "rabbitmq",
    // Add more mappings here as needed
  };

  app.DEPENDENCY_MAP = DEPENDENCY_MAP;
})(window.SpringServiceMonitor);
