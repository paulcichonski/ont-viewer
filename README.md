ont-viewer
==========

Servlet code for generating schema.org like views of ontologies.

--> Ontologies for viewing need to go in "src / main / resources / ontologies"

TODO:
- hook in owl thing view
- Extend sax parser to gather ontology description and annotation properties
- tests for propertyBuilder and viewBuilder
- static file generation (may need to create a new propertyBuilder implementation for this).
- Eventually move ontology parsing to OwlAPI to ensure handling of all edge cases.
- documentation!
- possibly add extension that allows app to hook into SPARQL endpoints to generate a view from a specific namespace.