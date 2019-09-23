# Two Dimensional RLE
A two dimensional RLE (run length encoding) implementation for one dimensional input data, e.g. Strings.

## TODO's

- [x] Implement RLE for bitString
- [x] Implement matrix representation and compression approach
- [x] Create more and different test data ([galgary corpus](http://www.data-compression.info/Corpora/CalgaryCorpus/index.html))
- [x] Create automated tests
- [ ] Evaluate the efficiency of possible chunk sizes, different mapping approaches etc..
- [ ] Evaluate and compare filesizes of each approach 
- [ ] Implement Burrows-Wheeler Transformation
- [ ] Finish ba outline 

- tex citations?
- scope of basic theory?


### Usage
- Clone Project
- build Project and all dependencies with maven `mvn clean install`
- run Test with maven surefire plugin `mvn surefire-report:report` to execute all Test cases and get the results shown as a report in html. 


### Further research needed:
- different approaches for different bitvectors
  - 1st (and maybe 2dn and 3rd)
    - simple dictionary coding (binary RLE)
  - other 5 with more complrex approaches
    - dictionary coding (e.g. LZW, LZ4, Marlin)
    - prediction by partial matching
    - asymmetric numeral system coding

### Research papers
- Marlin
  - https://cvhci.anthropomatik.kit.edu/~manel/publications/2017_dcc.pdf
  - http://www2.ift.ulaval.ca/~dadub100/files/DCC18MSDS.pdf

- LZ4
  - https://ieeexplore.ieee.org/document/7440278
  - https://www.semanticscholar.org/paper/LZ4-compression-algorithm-on-FPGA-Bartik-Ubik/ad97538dca2cfa64c4aa7c87e861bf39ab6edbfc
  
- ASN
  - https://arxiv.org/abs/1311.2540
  - https://courses.cs.ut.ee/MTAT.07.022/2017_fall/uploads/Main/mart-report-f17.pdf
  
- Prediction by partial matching (PPN)
  - https://pdfs.semanticscholar.org/08c7/376cdd64f8bf02364b2f515de7b9a490dc04.pdf
  - http://tcs.rwth-aachen.de/lehre/Komprimierung/SS2012/ausarbeitungen/PPM.pdf
