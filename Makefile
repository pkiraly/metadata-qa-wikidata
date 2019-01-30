
# check required executables
JQ = $(shell which jq)
ifeq ($(JQ),)
    JQ = $(error jq is required but not installed)
endif
WD = $(shell which wd)
ifeq ($(WD),)
    WD = $(error wikidata-cli is required but not installed)
endif
MLR = $(shell which mlr)
ifeq ($(MLR),)
    MLR = $(error miller is required but not installed)
endif
CSVJOIN = $(shell which csvjoin)
ifeq ($(CSVJOIN),)
    CSVJOIN = $(error csvjoin from csvkit is required but not installed)
endif
CSVCUT = $(shell which csvcut)
ifeq ($(CSVCUT),)
    CSVCUT = $(error csvcut from csvkit is required but not installed)
endif

default: data/properties.csv data/identifier-properties.csv

# make sure data directory exist for output
data:
	mkdir -p $@

# list of all properties
data/properties.json: data
	$(WD) props --type --lang en > $@

data/properties.csv: data/properties.json
	echo '"id","label","datatype"' > $@
	$(JQ) -r 'to_entries[]|[.key,.value.label,.value.type]|@csv' $< >> $@

# properties to identify works
data/identifier-properties.json: data
	$(WD) sparql src/main/resources/identifier-properties.sparql --index prop \
		| $(JQ) 'with_entries(.value |= .[0])' > $@

data/identifier-properties.csv: data/identifier-properties.json
	echo '"id","label","category"' > $@
	$(JQ) -r 'to_entries[]|[.key,.value.label,"identifier"]|@csv' $< >> $@

# merge property lists
data/property-overview.csv: data/properties.csv data/identifier-properties.csv data/properties-by-publication.csv
	$(CSVJOIN) --left -c id $^ | $(CSVCUT) -C label2 > $@
