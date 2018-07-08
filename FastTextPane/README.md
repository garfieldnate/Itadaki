#FastTextPane

FastTextPane is a read/append-only text display widget capable of displaying immense quantities of text almost instantaneously. It is used in Itadaki as a replacement for the snail-like built in Java text components. The display can only scroll by paragraphs (not wrapped paragraph lines). This allows it to be fast but prevents it from being usable with documents with very long paragraphs (it is just fine for the Edict dictionaries).

To see a demo of FastTextPane, run the demo in the ItadakiDictionary project and search for a common word like "a".

FastTextPane is distributed under the LGPL license.

##Features:

* Thread safe
* Essentially O(1) append speed
* Externally supplied paragraph storage (Document). Allows unlimited content size and on-demand realisation/formatting of text only as it becomes visible
* Smooth resize; Does not layout in the Swing event thread while resizing (waits until resize stops)
* Mandatory horizontal word wrap (no horizontal scrollbar)
* Vertical scrollbar with automatic visibility. Avoids "layout flash" when scrollbar materialises or dematerialises
* Scrollbar increments track paragraphs, eliminating the chief cause of append slowness in JTextComponent derivatives (namely, trying to maintain pixel-accurate scrollbar settings)
* Separate paragraph initial/subsequent indents
* Formatted text (anything an AttributedString can represent)
* Individual paragraph background colours
* Mouse text selection
* Copy to clipboard
* Action-based line scrolling
* Action-based page scrolling

##Building

    gradle build

##Design:

* The "main" class (which contains most of this documentation) is `FastTextView`
* The principle state is stored in
    * document
    * layout
    * selection
    * position

##Todo items:

* Keyboard selection
* Unit tests
