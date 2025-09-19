(function(app) {
  const DependencyDetector = {
    detectDependencies: function(normalizedConfig) {
      const dependencies = [];
      const config = normalizedConfig.dependencies; // The raw config data

      for (const key in config) {
        if (app.DEPENDENCY_MAP[key]) {
          const toService = app.DEPENDENCY_MAP[key];
          this.addUniqueDependency(dependencies, normalizedConfig.serviceName, toService);
        }
      }

      // Special case for JDBC URLs, which can be more complex
      if (config['spring.datasource.url']) {
          const url = config['spring.datasource.url'];
          if (url.includes('postgresql')) {
              this.addUniqueDependency(dependencies, normalizedConfig.serviceName, 'postgres');
          }
          if (url.includes('mysql')) {
              this.addUniqueDependency(dependencies, normalizedConfig.serviceName, 'mysql');
          }
          if (url.includes('mariadb')) {
              this.addUniqueDependency(dependencies, normalizedConfig.serviceName, 'mariadb');
          }
          // Add other DB types here if needed
      }

      return dependencies;
    },

    addUniqueDependency: function(dependencies, fromService, toService) {
      if (!dependencies.some(dep => dep.toService === toService)) {
        dependencies.push({ fromService, toService });
      }
    }
  };

  app.DependencyDetector = DependencyDetector;
})(window.SpringServiceMonitor);
