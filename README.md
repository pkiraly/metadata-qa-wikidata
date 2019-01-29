# Quality of bibliographic data in Wikidata

This research project aims to reveal some quality issues of the bibliographic data inside Wikidata.

Researchers (in alphabetic order): Péter Király and Jakob Voß

## usage

### build the code:

```
mvn clean install
```

### run it

```
java -cp target/wikidata-0.1-SNAPSHOT.jar de.gwdg.metadataqa.wikidata.Client \
  --input-file data/wikidata-[version]-publications.ndjson \
  --output-file data/transformed.json \
  --property-file data/properties-12M.csv \
  --entity-file data/entities-12M.csv
```

Properties and entities files to be located in the `data/` directory (property-file, entity-file parameters) will be provided in the project in the future.

# More details

See [the wiki pages](https://github.com/pkiraly/metadata-qa-wikidata/wiki).
