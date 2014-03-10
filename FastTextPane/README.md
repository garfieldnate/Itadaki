#FastTextPane

FastTextPane is a read/append-only text display widget capable of displaying immense quantities of text almost instantaneously. It is used in Itadaki as a replacement for the snail-like built in Java text components.

FastTextPane is distributed under the LGPL license.

Features:

* Thread safe
* Essentially O(1) append speed<br>
* Externally supplied paragraph storage (Document). Allows unlimited content size and on-demand realisation/formatting of text only as it becomes visible<br>
* Smooth resize; Does not layout in the Swing event thread while resizing (waits until resize stops)<br>
* Mandatory horizontal word wrap (no horizontal scrollbar)<br>
* Vertical scrollbar with automatic visibility. Avoids "layout flash" when scrollbar materialises or dematerialises<br>
* Scrollbar increments track paragraphs, eliminating the chief cause of append slowness in JTextComponent derivatives (namely, trying to maintain pixel-accurate scrollbar settings)<br>
* Separate paragraph initial/subsequent indents<br>
* Formatted text (anything an AttributedString can represent)<br>
* Individual paragraph background colours<br>
* Mouse text selection<br>
* Copy to clipboard<br>
* Action-based line scrolling<br>
* Action-based page scrolling

Todo items:

* Keyboard selection<br><br>

Design:

* The "main" class (which contains most of this documentation) is FastTextView
* The principle state is stored in
    * document
    * layout
    * selection
    * position

