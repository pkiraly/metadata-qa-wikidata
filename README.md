# Quality of bibliographic data in Wikidata

This research project aims to reveal some quality issues of the bibliographic data inside Wikidata.

Researchers (in alphabetic order): Péter Király and Jakob Voß

## usage

The large files are stored on Github's Large File Storage. In order to work with them, install git-lfs: https://git-lfs.github.com/.

### build the code:

```
mvn clean install
```

### run it

Run the transformation from encoded JSON dump to "human readable" JSON

```{bash}
java -cp target/wikidata-0.1-SNAPSHOT.jar de.gwdg.metadataqa.wikidata.Client \
  --input-file data/wikidata-[version]-publications.ndjson \
  --output-file data/transformed.json \
  --property-file data/properties-12M.csv \
  --entity-file data/entities-12M.csv
  --command TRANSFORMATION
```

Run the entity class resolution

```
java -cp target/wikidata-0.1-SNAPSHOT.jar de.gwdg.metadataqa.wikidata.Client \
  --entity-file data/entities-12M.csv \
  --command ENTITY_CLASS_RESOLUTION
```


Properties and entities files to be located in the `data/` directory (property-file, entity-file parameters) will be provided in the project in the future.

# More details

See [the wiki pages](https://github.com/pkiraly/metadata-qa-wikidata/wiki).

[![Build Status](https://travis-ci.org/pkiraly/metadata-qa-wikidata.svg?branch=master)](https://travis-ci.org/pkiraly/metadata-qa-wikidata)
