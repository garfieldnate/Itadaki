
[Source](http://web.archive.org/web/20080108030404/http://itadaki.org/wiki/index.php/Dictionary_Feature_Tour "Permalink to Dictionary Feature Tour - Itadaki")

# Dictionary Feature Tour

## The Dictionary Interface

### Dictionary Window

The dictionary window is designed to allow fast, efficient searching within the full text of a dictionary. Its main features are as follows:

  * All possible match positions (exact word matches, start-of-word matches, end-of-word matches, any-position matches) are searched for at once, with the results shown divided into sections.
  * Links to each result section are provided above the results.
  * Matches within each line of the results are highlighted in blue text.
  * Results in different dictionaries are given different background highlights.
  * The dictionary window can be linked to open Openoffice Writer windows such that selected text is automatically searched for. This is extremely fast on a reasonably modern machine, and does not take a noticeable amount of CPU power.

## Context Menu

Context Menu

  * **Search for "..."**: If text is selected within a single line of the search results, starts a new search with the selected text.
  * **Cut**: Cuts selected text within the search entry field.
  * **Copy**: Copies selected text within the search entry field or search results.
  * **Paste**: Pastes text within the search entry field.
  * **Always On Top**: If permitted by the windowing environment, make the dictionary be always-on-top while it is open.
  * **Search On Select**: Search immediately for text selected within an OpenOffice Writer document.

    **Note:** This function is disabled while the Furigana Wizard window is open.

  * **Options**: Opens the dictionary options window

## Options Window

Options Window

The Options Window allows configuration of the available dictionaries.

  * Local dictionaries in EDICT EUC-JP or UTF8 formats can be added and removed.

    **Note**: Dictionaries installed as OpenOffice packages are displayed here, but can only be added or removed through the OpenOffice Package Manager

  * The order of the dictionaries displayed in search results can be customised using the "Move Up" and "Move Down" buttons
  * The highlight colour used for each dictionary in search results can be customised by clicking on the colour box for each dictionary

##  Key And Mouse Shortcuts

  * **Enter**: Searches for the currently entered text.
  * **Esc** (when a search has been performed): Clears the current search.
  * **Esc** (when already cleared): Closes the window.
  * **Up/Down/PgUp/PgDn**: Moves through the results.
  * **Alt%2B1**: Scrolls directly to "Exact" results.
  * **Alt%2B2**: Scrolls directly to "Start" results.
  * **Alt%2B3**: Scrolls directly to "End" results.
  * **Alt%2B4**: Scrolls directly to "Other" results.
  * **Mouse Wheel**: Scrolls through the results by lines
  * **Ctrl%2BMouse Wheel**: Scrolls through the results by pages

##  Known Issues

**UNIX:** Users of the SCIM input method may find that the search entry field can become unresponsive. In this state, searches started from an OpenOffice document by menu command, key shortcut, or through search-on-select will work, but typing searches directly into the dictionary becomes impossible; this is caused by bugs in Sun Java's integration with SCIM. Sometimes, closing the dictionary window and re-opening it will cure this issue. If it does not, the only solution is to restart OpenOffice.
