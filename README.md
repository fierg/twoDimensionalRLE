# Two Dimensional RLE
A two dimensional RLE (run length encoding) implementation for one dimensional input data, e.g. Strings.

## TODO's

- [x] Implement RLE for bitString
- [x] Implement matrix representation and compression approach
- [ ] Create more and different test data
- [x] Create automated tests to compare filesize
- [ ] Evaluate the efficiency of possible implementations

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
