
# check required executables
JQ = $(shell which jq)
WD = $(shell which wd)
ifeq ($(JQ),)
    JQ = $(error jq is required but not installed)
endif
ifeq ($(WD),)
    WD = $(error wikidata-cli is required but not installed)
endif

# make sure data directory exist for output
data:
	mkdir -p $@

# retrieve list of properties to identify works
data/identifier-properties.json: data
	$(WD) sparql src/main/resources/identifier-properties.sparql --index prop \
		| $(JQ) 'with_entries(.value |= .[0].label)' > $@
