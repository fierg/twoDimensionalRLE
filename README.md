# Two Dimensional RLE
A two dimensional RLE (run length encoding) implementation for one dimensional input data, e.g. Strings.

## TODO's

- [x] Implement RLE for bitString
- [x] Implement matrix representation and compression approach
- [x] Create more and different test data ([galgary corpus](http://www.data-compression.info/Corpora/CalgaryCorpus/index.html))
- [x] Create automated tests
- [ ] Evaluate the efficiency of possible chunk sizes, different mapping approaches etc..
- [ ] Evaluate and compare filesizes of each approach 
- [x] Implement Burrows-Wheeler Transformation
- [x] Finish ba outline
- [ ] Add suggested ba changes
- [ ] Persist and read Mapping during decoding
- [ ] Implement Mixed Encoding/Decoding
- [ ] Add intro and basics to ba 


### Usage
- Clone Project
- build Project and all dependencies with maven `mvn clean install`
- run Test with maven surefire plugin `mvn surefire-report:report` to execute all Test cases and get the results shown as a report in html. 


### Further research needed:
- different approaches for different bitvectors
  - 1st (and maybe 2dn and 3rd)
    - simple dictionary coding (binary RLE)
  - other 5 with more complrex approaches
    - dictionary coding (e.g. Huffman, LZW, LZ4, Marlin)
