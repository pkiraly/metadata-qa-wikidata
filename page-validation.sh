#!/usr/bin/env bash

ENTITY_FILE=data/entities-23Mb.csv
PAGE_VALIDATION_FILE=data/pages-per-journals.csv

java -Xmx8g -cp target/wikidata-0.1-SNAPSHOT.jar de.gwdg.metadataqa.wikidata.Client \
    --input-file data/wikidata-20190128-publications.ndjson \
    --entity-file ${ENTITY_FILE} \
    --output-file ${PAGE_VALIDATION_FILE} \
    --command PAGE_VALIDATION
