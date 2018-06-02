LayoutLib API is a jar describing the API used to load and interact with layoutlib.jar
It is to be packaged with clients accessing layoutlib.jar

Layoutlib.jar is built from frameworks/base.git and therefore is versioned with the platform.

IMPORTANT NOTE REGARDING CHANGES IN LAYOUTLIB_API:

- The API must stay compatible. This is because while layoutlib.jar compiles against it,
  the client provides the implementation and must be able to load earlier versions of layoutlib.jar.
  This is true for all the classes under com.android.ide.common.rendering.api and
  com.android.layoutlib.api although the latter is obsolete and should not be changed at all.

- Updated version of layoutlib-api should be copied to the current in-dev branch of
  prebuilt/common/layoutlib_api/layoutlib-api-prebuilt.jar
  The PREBUILT file in the same folder must be updated as well to reflect how to rebuild this
  prebuilt jar file.
