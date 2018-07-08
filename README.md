# Itadaki

The official Itadaki project is located on SourceForge and on Google Code. I created a fork here so that I could add more information and any fixes that I find. This and other readme files are mostly from the now-offline itadaki.org, accessible through [The Wayback Machine](http://web.archive.org/web/20080108030414/http://itadaki.org/wiki/index.php/Itadaki).

##  Introduction

**Itadaki** is a Japanese language toolset integrated with [OpenOffice][3], providing tools intended for students and translators of Japanese.

##  Downloads

The current release is **1.0 alpha 1**.

**Note:** Following these instructions is **strongly recommended** to avoid some pitfalls involving Japanese fonts and the setup of Java.

##  License

Itadaki is made available under the [GNU LGPL][4], and is free to use, copy, and modify under the [terms][4] of that license.

The optional dictionaries provided for use with Itadaki are licensed under various other terms, including the Creative Commons [CC-BY-SA 2.5][5] and the [Ipadic license][6]. A full breakdown of the license terms of Itadaki's components can be found in the [Code Module Overview][7]

##  Feature Tour

###  Dictionary

The **Dictionary** searches one or more standard **EDICT** format dictionaries (packages of the **EDICT** general dictionary and **ENAMDICT** name dictionary are available from the [download area on SourceForge][13]). The dictionary can be set to **search on selection** to allow fast lookups from within OpenOffice documents

###  Furigana Wizard

The **Furigana Wizard** automatically applies **furigana** (reading annotations) to Kanji. For each sentence analysed, the best guess as to the correct readings is automatically presented; alternative or custom readings can be selected interactively.

##  Development Information

Development mailing list is here: [SourceForge mailing list][8]. However, it seems to be no longer maintained.

An overview of the code modules that comprise Itadaki is available.

## Installation Instructions

[Source](http://web.archive.org/web/20080108030408/http://itadaki.org/wiki/index.php/Installation_Instructions "Permalink to Installation Instructions - Itadaki")

###  OpenOffice Installation

  * Itadaki requires OpenOffice 2.0 or later (2.1 or later recommended)

**Windows**: The latest OpenOffice can be downloaded from http://openoffice.org/

**Unix**: If possible, install the OpenOffice packages provided by your distribution. Generic packages are also available from http://openoffice.org/

**Mac OS X**: [NeoOffice 2.0][14] (based on OpenOffice 2.0) should be compatible with Itadaki.

    **Note**: Itadaki has not been tested on Mac OS X; some features may not work, and additional configuration may be necessary.

###  Java Installation

  * Itadaki requires Sun Java 5.0 or later (6.0 or later recommended)

**Windows**: A compatible Java is installed automatically when you install OpenOffice. No further installation should be necessary.

**Unix**: The latest Java can be downloaded from [http://java.sun.com/][3]

    **Note**: GNU GCJ compatible with Java 1.4.2 **will not work**. You must install Sun Java.

####  Checking OpenOffice Java Settings

OpenOffice Java Options

OpenOffice Java Settings can be checked from the **Tools &gt; Options...** menu in an OpenOffice window (see the image to the right). Of the Java installations shown in this example, one of Java 5.0 (1.5.0) or 6.0 (1.6.0) should be selected before Itadaki is installed

    **Note**: If Java 1.4.2 is selected, OpenOffice may **cease to work** after Itadaki is installed. Please check that Sun Java 5.0 or greater is selected before continuing

###  Font And Input Setup

####  Enabling operating system Japanese support

**Windows:** East Asian language support must be installed to use Itadaki. Instructions on setting up East Asian languages in English Windows XP can be found [here][6].

**UNIX:** See your distribution's documentation for instructions on setting up Japanese fonts and input.

####  Japanese font support in Java

**Windows:** By default, when Windows is not set to use a Japanese locale for the desktop, Kanji may be shown in a **Chinese font** within Itadaki's windows. Chinese renderings of some characters are not suitable for the display of Japanese, and Java must first be set up to prefer Japanese fonts.

  1. Locate your Java installation. This is normally installed beneath `**C:\Program Files\Java\**`; for instance, `**C:\Program Files\Java\jre1.5.0_06\**`. If you have several versions of Java installed, choose the exact version that OpenOffice is using (see above).
  2. Save this modified `**fontconfig.properties**` in the `**\lib\**` directory inside the Java installation; for instance, `**C:\Program Files\Java\jre1.5.0_06\lib ontconfig.properties**`

###  Itadaki Installation

  * Download the Itadaki packages from the download site:
  * The packages can be installed within OpenOffice from the **Tools &gt; Package Manager...** menu (OpenOffice 2.1 or earlier) or the **Tools &gt; Extension Manager...** menu (OpenOffice 2.2 or greater).
  * After installing the Itadaki packages, restart OpenOffice

    **Note:** Windows installations of OpenOffice include a quickstarter that docks into the system tray. To fully restart OpenOffice, you must exit the quickstarter as well as closing all OpenOffice windows.

## Code Module Overview

[Source](http://web.archive.org/web/20080108030423/http://itadaki.org/wiki/index.php/Code_Module_Overview "Permalink to Code Module Overview - Itadaki")

###  Introduction

Itadaki is comprised of a number of individual code modules. As far as practical, these modules are designed as independent services with as few interdependencies as possible; all the sub modules linked into the main OpenOffice Interface module are intended to be reusable in other contexts.

###  Top Level Code Modules

####  Dictionary Tool

* **Module name**: ItadakiDictionary
* **Depends on**: Seashell, FastTextPane
* **License**: [LGPL][4]

The Dictionary Tool service that provides the dictionary dialog. Integrates with the OpenOffice Interface module through a minimal Java interface, and contains no OpenOffice specific code.

####  Furigana Wizard

* **Module name**: ItadakiFurigana
* **Depends on**: GoSen
* **License**: [LGPL][4]

The Furigana Wizard service, providing both the furigana wizard dialog and the "add to whole document" command. Integrates with the OpenOffice Interface module through a minimal Java interface, and contains no OpenOffice specific code.

####  OpenOffice Interface

* **Module name**: ItadakiOpenOffice
* **Depends on**: ItadakiDictionary, ItadakiFurigana
* **License**: [LGPL][4]

The front end integration of the OpenOffice extension. All OpenOffice specific code is contained within this module.

###  Supporting Code Modules

####  Fast Text Pane

* **Module name**: FastTextPane
* **License**: [LGPL][4]

A read-only text display widget capable of displaying immense quantities of text almost instantaneously. Used as a replacement for the snail-like built in Java text components.

####  GoSen

* **Module name**: GoSen
* **Depends on**: JISX0213 (for compilation only)
* **License**: [LGPL][4]

A pure Java morphological analysis library for Japanese based on [Sen][11]. GoSen is used by the Furigana Wizard to analyse sentences of Japanese text.

####  JIS X 0213 Support

* **Module name**: JISX0213
* **License**: [LGPL][4]

A Java character set provider for the decoding of EUC-JISX0213 text (encoding is not currently supported). As of version 6, Sun Java does not provide this natively.

####  Seashell

* **Module name**: Seashell
* **License**: [LGPL][4]

Low level dictionary searching and indexing services.

###  Other Modules

####  EDICT Dictionary

* **Module name**: ItadakiDataEdict
* **Depends on**: Seashell
* **License**: [CC-BY-SA 2.5][5]

The EDICT dictionary file for the Dictionary Tool. The module indexes the dictionary using Seashell and creates an OpenOffice extension package from it.

####  ENAMDICT Dictionary

* **Module name**: ItadakiDataEnamdict
* **Depends on**: Seashell
* **License**: [CC-BY-SA 2.5][5]

The ENAMDICT dictionary file for the Dictionary Tool. The module indexes the dictionary using Seashell and creates an OpenOffice extension package from it.

####  Ipadic Dictionary

* **Module name**: ItadakiDataIpadic
* **Depends on**: GoSen
* **License**: [Ipadic license][6]

The Ipadic morphological analysis dictionary for the Furigana Wizard. The module compiles the dictionary using GoSen and creates an OpenOffice extension package from it.

## Building Itadaki From Source

[Source](http://web.archive.org/web/20080108042235/http://itadaki.org/wiki/index.php/Building_Itadaki_From_Source "Permalink to Building Itadaki From Source - Itadaki")

###  Getting the Itadaki source

Itadaki's source can be [downloaded from GitHub][16], and can be browsed through the [web interface][17].

Itadaki is best edited using the [Eclipse][18] IDE.

###  Building the individual modules

Jar files for each individual module can be built by executing the Ant `build.xml` from within Eclipse, or by invoking

    $ ant

within the module's top level directory.

  * First, build the supporting modules:

    FastTextPane/
    GoSen/
    JISX0213/
    Seashell/

    **Note:** A pre-built jar of the JISX0213 module is present in the GoSen source. It can be replaced with a fresh build if desired, but this should not normally be necessary.

  * Second, build the application modules:

    ItadakiDictionary/
    ItadakiFurigana/

  * Third, install the built jar files in the OpenOffice interface module:

    $ cp FastTextPane/fasttextpane-*.jar GoSen/gosen-*.jar JISX0213/jisx0213-*.jar \
    Seashell/seashell-*.jar ItadakiDictionary/itadaki-dictionary-*.jar \
    ItadakiFurigana/itadaki-furigana-*.jar ItadakiOpenOffice/libs/

  * Finally, build the UNO packages for the OpenOffice interface and supporting dictionaries:

    ItadakiOpenOffice/
    ItadakiDataEdict/
    ItadakiDataEnamdict/
    ItadakiDataIpadic/

###  Reinstalling Itadaki

When first installing Itadaki, following the Installation Instructions is **strongly recommended** to ensure you have a compatible Java and Asian font setup.

After rebuilding, the Itadaki OpenOffice package can be re-installed from the command line. On a typical UNIX system, the following command can be used:

    $ /usr/lib/openoffice.org2.0/program/unopkg add -f ItadakiOpenOffice/itadaki-*.uno.pkg

OpenOffice will require a full restart after installing new packages.

    **Note:** On systems where an OpenOffice quickstarter is enabled, the quickstarter may need to be killed to effect a full restart

##  Thanks

Itadaki is built on ideas from a great number of other projects. Special thanks are due in particular to:

  * [JWPce][9] and the venerable JWP for pioneering the concept of a text editor with integrated Japanese tools
  * [JGloss][10] for the idea of using morphological analysis to apply furigana, and indirectly for several ideas concerning dictionary interfaces in Java
  * [Sen][11] for the basis of a pure Java morphological analysis library (see [GoSen][12] for more information about the fork/upgrade of Sen used within Itadaki)

## TODO

* description.xml files should have correct version numbers.
* de-duplicate Gradle builds used for dictionaries
* use NAICT dictionary
* unify ItadakiDataIpadic and the copy in Gosen's source folder
  - where is Gosen getting the .sen files from right now?

   [3]: http://openoffice.org/ (http://openoffice.org/)
   [4]: www.gnu.org/licenses/lgpl.html (http://www.gnu.org/licenses/lgpl.html)
   [5]: creativecommons.org/licenses/by-sa/2.5/ (http://creativecommons.org/licenses/by-sa/2.5/)
   [6]: ItadakiDataIpadic/COPYING (./ItadakiDataIpadic/COPYING)
   [7]: http://web.archive.org/web/20080108030414/http%3A/itadaki.org/wiki/index.php/Code_Module_Overview (Code Module Overview)
   [8]: https://lists.sourceforge.net/lists/listinfo/itadaki-devel (https://lists.sourceforge.net/lists/listinfo/itadaki-devel)
   [9]: http://www.physics.ucla.edu/~grosenth/jwpce.html (http://www.physics.ucla.edu/~grosenth/jwpce.html)
   [10]: http://jgloss.sourceforge.net/ (http://jgloss.sourceforge.net/)
   [11]: https://sen.dev.java.net/ (https://sen.dev.java.net/)
   [12]: http://itadaki.org/wiki/index.php/GoSen (GoSen)
   [13]: http://sourceforge.net/projects/itadaki/files/Itadaki/1.0%20alpha%201/ (http://sourceforge.net/projects/itadaki/files/Itadaki/1.0%20alpha%201/)
   [14]: http://www.neooffice.org/ (http://www.neooffice.org/)
   [15]: http://web.archive.org/web/20080108030408/http://www.coscom.co.jp/help3/jpfont/jpfont.html (http://www.coscom.co.jp/help3/jpfont/jpfont.html)
   [16]: https://github.com/garfieldnate/Itadaki/archive/master.zip (https://github.com/garfieldnate/Itadaki/archive/master.zip)
   [17]: https://github.com/garfieldnate/Itadaki (https://github.com/garfieldnate/Itadaki)
   [18]: http://eclipse.org/ (http://eclipse.org/)
