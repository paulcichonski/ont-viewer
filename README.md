ont-viewer
==========

Servlet code for generating schema.org like views of ontologies.

--> Ontologies for viewing need to go in "src / main / resources / ontologies"

TODO:
- update static file generation so it places resources/ (i.e., css) into the website storageDir
- tests for StaticPathBuilder and ViewBuilder
- hook in owl:thing view
- Extend sax parser to gather ontology description and annotation properties (OR just integrate OWL API)
- refactor all configuration options into properties file (i.e., servlet-mapping, directory locations).
- documentation!
- possibly add extension that allows app to hook into SPARQL endpoints to generate a view from a specific namespace.