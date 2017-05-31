# kaan-sein-tool

A Clojure program that outputs a PDF given a csv timesheet table.

## Usage

The csv filename should be in the following format

    <YYYY>-<MM>_<project>_<Employee>.csv

For example

    2017-05_The Company/My Project_Max Mustermann.csv
    
The file should be in csv format with the following mandatory columns:

    - "Date" or "date"
    - "Description" or "description"
    - "Duration" or "duration" or "amount"

The columns can occur in any order, there can be more then those three columns.

"Date" is a date in "YYYY-MM-DD" or "YYYY/MM/DD" format.

"Duration" is a decimal number with "." as delimiter, optionally ending with "h".
Example: "4.25", "4.25" (4 hours 15 minutes).


Example file data:	

    "Date","Duration","Description"
    "2017-05-02","1.00","Kitchen"
    "2017-05-08","2.00","Bathroom"
    "2017-05-15","5.00","Office"
    "2017-05-16","0.75","Garage"
    "2017-05-22","1.50","Basement"

To convert your csv-file to a pdf you run the program like this:

    $ lein run <path-to-csv-file>

or compile+uberjar it, then

    $ java -jar kaan-sein-tool-0.1.0-standalone.jar <path-to-csv-file>

## License

Copyright © 2017 Active Group GmbH

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
