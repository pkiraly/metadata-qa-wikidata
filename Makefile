
# check required executables
JQ = $(shell which jq)
WD = $(shell which wd)
ifeq ($(JQ),)
    JQ = $(error jq is required but not installed)
endif
ifeq ($(WD),)
    WD = $(error wikidata-cli is required but not installed)
endif

default: data/properties.csv data/identifier-properties.csv

# make sure data directory exist for output
data:
	mkdir -p $@

# list of all properties
data/properties.json: data
	$(WD) props --type --lang en > $@

data/properties.csv: data/properties.json
	jq -r 'to_entries[]|[.key,.value.label,.value.type]|@csv' $< > $@

# properties to identify works
data/identifier-properties.json: data
	$(WD) sparql src/main/resources/identifier-properties.sparql --index prop \
		| $(JQ) 'with_entries(.value |= .[0])' > $@

data/identifier-properties.csv: data/identifier-properties.json
	jq -r 'to_entries[]|[.key,.value.label]|@csv' $< > $@
