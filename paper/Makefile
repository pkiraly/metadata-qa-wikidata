main.pdf: main.tex bibliography.bib
	xelatex $<
	bibtex main.aux
	xelatex $<
	xelatex $<

bibliography.json: citekeys.yaml
	wcite update citekeys.yaml

bibliography.bib: bibliography.json
	wcite citekeys.yaml -f bibtex > $@
