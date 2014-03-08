
[Source](http://web.archive.org/web/20071224025014/http://itadaki.org/wiki/index.php/GoSen "Permalink to GoSen - Itadaki")

# GoSen - Itadaki

##  Introduction

**GoSen** is a comprehensive rewrite and upgrade of [Sen][1], a pure Java LGPL morphological analysis library for Japanese which in turn was based on [MeCab][2].

GoSen is at present a de facto fork of Sen. It would be extremely useful if the work performed to create GoSen could be folded back into the base Sen project; unfortunately, the original authors of Sen seem to be uncontactable at the present time.

##  Main Changes From Sen

  * Furigana processing support
  * Source upgraded to Java 5
  * Improved GPL compatibility through removal of the dependency on commons-logging
  * Pure Java dictionary compilation with no dependency on Perl
  * Greatly reduced heap usage during dictionary compilation, allowing compilation with the default Java heap settings
  * EUC-JISX0213 character set support allowing correct compilation of the Ipadic dictionary
  * Significantly improved text analysis speed
  * Support for morphemes within Ipadic with multiple alternative readings
  * Full Javadoc class documentation
  * JUnit test suite

##  Development

GoSen is essentially stable, complete, and fit for general purpose use in its present state. There is currently not, however, a formal public release of the code separate from its role within Itadaki, pending further information on the status of the original Sen project.

###  Source

GoSen's source can be checked out of [project SVN][3] from the **GoSen** module, and can be browsed through the [SVN web interface][4]

###  Compilation

####  Automatic Compilation

Simply check out the source as an [Eclipse][5] project

####  Manual Compilation

To build and package the source as a jar file, execute the following from within the GoSen directory:

    $ ant

####  Dictionary Compilation

To fetch and build Ipadic in the correct configuration to run the built in tests and demos, , execute the following from within the GoSen directory:


    $ cd testdata/dictionary
    $ ant
    $ cd ../..

####  Graphical Demonstration

To run a graphical demonstration of furigana processing, execute the following from within the GoSen directory:


    $ java -cp bin examples.ReadingProcessorDemo testdata/dictionary/dictionary.xml

**Note:** The graphical demo is based on an earlier version of the Dictionary Tool. It may be deprecated in a future release

###  Known Issues

A few minor issues in furigana assignment are known to exist, and will be dealt with in a future release. They are:

  * Treatment of morphemes with mixed kanji and katakana (such as イスラム教)
  * Treatment of morphemes containing the 々 (kurikaeshi) mark
  * Date and other modifiers (年,月,日 etc.) following Japanese numerals (一二三) are assigned their correct readings, but not when following roman numerals

   [1]: https://sen.dev.java.net/ (https://sen.dev.java.net/)
   [2]: http://mecab.sourceforge.net/ (http://mecab.sourceforge.net/)
   [3]: https://sourceforge.net/svn/?group_id=188825 (https://sourceforge.net/svn/?group_id=188825)
   [4]: http://itadaki.svn.sourceforge.net/viewvc/itadaki/GoSen/ (http://itadaki.svn.sourceforge.net/viewvc/itadaki/GoSen/)
   [5]: http://eclipse.org/ (http://eclipse.org/)
