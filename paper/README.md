This directory contains the paper source files. The paper should better be
edited via ShareLaTex and only copied to the git repository for archiving. 

Local creation of the PDF file can be triggered via `make`.
Required software:

* `pandoc`
* `xelatex`
* npm module `wcite`

The references are stored in `bibliography.bib` but this file is generated
from Wikidata items referenced in `citekeys.yaml`.
