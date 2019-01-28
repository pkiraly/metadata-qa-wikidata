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
java -cp target/wikidata-0.1-SNAPSHOT.jar de.gwdg.metadataqa.wikidata.json.ReaderClient \
  --input-file path/to/wikidata-[version]-publications.ndjson \
  --output-file path/to/transformed.json \
  --property-file path/to/properties-12M.csv \
  --entity-file path/to/entities-12M.csv
```

# More details
See [the wiki pages](https://github.com/pkiraly/metadata-qa-wikidata/wiki).
