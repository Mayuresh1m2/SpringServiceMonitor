(function(app) {
  const DiagramGenerator = {
    generateDiagram: function(serviceName, dependencies) {
      const mainService = serviceName;

      const mainServiceX = 350;
      const mainServiceY = 100;
      const dependencyY = 300;
      const dependencyXStart = 150;
      const dependencyXSpacing = 200;

      let mxfile = `<mxfile host="WebApp" modified="2023-10-27T12:00:00.000Z" agent="5.0" etag="1" version="22.0.8" type="device">
    <diagram name="Architecture" id="diagram-id-1">
      <mxGraphModel dx="1434" dy="782" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="850" pageHeight="1100" math="0" shadow="0">
        <root>
          <mxCell id="0" />
          <mxCell id="1" parent="0" />
          <!-- Main Service -->
          <mxCell id="2" value="${mainService}" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;" vertex="1" parent="1">
            <mxGeometry x="${mainServiceX}" y="${mainServiceY}" width="120" height="60" as="geometry" />
          </mxCell>
          <!-- Dependencies -->
          ${dependencies.map((dep, index) => this.generateServiceAndArrow(dep, index, dependencyXStart, dependencyXSpacing, dependencyY)).join('\\n        ')}
        </root>
      </mxGraphModel>
    </diagram>
  </mxfile>`;
      return mxfile;
    },

    generateServiceAndArrow: function(dep, index, xStart, xSpacing, yPos) {
      const serviceId = 3 + index * 2;
      const arrowId = 4 + index * 2;
      const xPos = xStart + (index * xSpacing);

      return `
          <mxCell id="${serviceId}" value="${dep.toService}" style="ellipse;whiteSpace=wrap;html=1;aspect=fixed;fillColor=#d5e8d4;strokeColor=#82b366;" vertex="1" parent="1">
            <mxGeometry x="${xPos}" y="${yPos}" width="80" height="80" as="geometry" />
          </mxCell>
          <mxCell id="${arrowId}" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;endArrow=classic;endFill=1;" edge="1" parent="1" source="2" target="${serviceId}">
            <mxGeometry relative="1" as="geometry" />
          </mxCell>`;
    }
  };

  app.DiagramGenerator = DiagramGenerator;
})(window.SpringServiceMonitor);
