#OpenOffice- Itadaki

This project contains the front-end integration of the OpenOffice extension. All OpenOffice specific code is contained within this module.

To work with this code, you must have the OpenOffice Java libraries available. In the Eclipse project, `juh.jar`, `jurt.jar`, `ridl.jar` and `unoil.jar` are all referenced. To resolve their locations, you must set the `OPENOFFICE_LIBRARY_PATH` path variable. The ant build file assumes that the location is `/usr/lib/openoffice.org2.0`, but this will only work for *nux users.

For building a new version with a different version the top-level build scripts version property has to be changed. After this the version number in nearly all files in directory "unopackage" have to be adjusted! Otherwise the newly build extension will be installed but not work.
