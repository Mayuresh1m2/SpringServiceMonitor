(function(app) {
  const ConfigParser = {
    parseYAML: function(content) {
      try {
        const data = jsyaml.load(content);
        return this.normalize(data);
      } catch (e) {
        console.error("Error parsing YAML:", e);
        return null;
      }
    },

    parseProperties: function(content) {
      try {
        const data = {};
        const lines = content.split('\n');
        for (const line of lines) {
          if (line.trim() === '' || line.startsWith('#')) {
            continue;
          }
          const index = line.indexOf('=');
          if (index !== -1) {
            const key = line.slice(0, index).trim();
            const value = line.slice(index + 1).trim();

            const keys = key.split('.');
            let current = data;
            for (let i = 0; i < keys.length - 1; i++) {
              current = current[keys[i]] = current[keys[i]] || {};
            }
            current[keys[keys.length - 1]] = value;
          }
        }
        return this.normalize(data);
      } catch (e) {
        console.error("Error parsing properties:", e);
        return null;
      }
    },

    normalize: function(data) {
      const serviceName = data['spring.application.name'] || 'unknown-service';
      return {
        serviceName: serviceName,
        dependencies: data,
      };
    }
  };

  app.ConfigParser = ConfigParser;
})(window.SpringServiceMonitor);
