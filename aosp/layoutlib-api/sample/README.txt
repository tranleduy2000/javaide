Sample code to use the layout rendering library.

This is very basic sample code showing how to render
a layout.

This explains how to load the resources, create Folderconfig,
ResourceResolver, and how to call the LayoutLibrary to do an
actual render.

There are some big limitations:
- can't render custom views because there's nothing
compiling them and generating the compiled R.class
file.
- not all features of ADT are present because there
are things that don't make sense outside of an editor
(render in context, expand empty layouts, etc...)