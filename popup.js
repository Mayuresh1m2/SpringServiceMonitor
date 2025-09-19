document.addEventListener('DOMContentLoaded', () => {
  const fileInput = document.getElementById('fileInput');
  const dependencyList = document.getElementById('dependencyList');
  const downloadCompose = document.getElementById('downloadCompose');
  const downloadDiagram = document.getElementById('downloadDiagram');
  const resultsDiv = document.getElementById('results');

  const { ConfigParser, DependencyDetector, ComposeGenerator, DiagramGenerator } = window.SpringServiceMonitor;

  let detectedDependencies = [];
  let serviceName = 'my-app';

  fileInput.addEventListener('change', handleFileSelect);

  downloadCompose.addEventListener('click', () => {
    const composeContent = ComposeGenerator.generate(serviceName, detectedDependencies);
    downloadFile(composeContent, 'docker-compose.yml', 'text/yaml');
  });

  downloadDiagram.addEventListener('click', () => {
    const diagramContent = DiagramGenerator.generateDiagram(serviceName, detectedDependencies);
    downloadFile(diagramContent, 'architecture.drawio', 'application/vnd.jgraph.mxfile');
  });

  function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
      const contents = e.target.result;
      let normalizedConfig;

      if (file.name.endsWith('.yml') || file.name.endsWith('.yaml')) {
        normalizedConfig = ConfigParser.parseYAML(contents);
      } else if (file.name.endsWith('.properties')) {
        normalizedConfig = ConfigParser.parseProperties(contents);
      } else {
        dependencyList.innerHTML = '<li>Unsupported file type.</li>';
        resultsDiv.style.display = 'block';
        return;
      }

      if (normalizedConfig) {
        serviceName = normalizedConfig.serviceName;
        detectedDependencies = DependencyDetector.detectDependencies(normalizedConfig);

        resultsDiv.style.display = 'block';
        if (detectedDependencies.length > 0) {
          dependencyList.innerHTML = detectedDependencies
            .map(dep => `<li>${dep.fromService} → ${dep.toService}</li>`)
            .join('');
        } else {
          dependencyList.innerHTML = '<li>No dependencies detected.</li>';
        }
      } else {
        dependencyList.innerHTML = '<li>Error parsing file.</li>';
        resultsDiv.style.display = 'block';
      }
    };
    reader.readAsText(file);
  }

  function downloadFile(content, fileName, mimeType) {
    const a = document.createElement('a');
    const blob = new Blob([content], { type: mimeType });
    a.href = URL.createObjectURL(blob);
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(a.href);
  }
});
